package com.procesy.procesy.security;

import com.procesy.procesy.service.AdvogadoDetailsService;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.OncePerRequestFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final AdvogadoDetailsService advogadoDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(AdvogadoDetailsService advogadoDetailsService, JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.advogadoDetailsService = advogadoDetailsService;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    // Filtro global para limitar requisições (proteção DDOS) para todos os endpoints
    @Bean
    public OncePerRequestFilter ddosRateLimitFilter() {
        return new OncePerRequestFilter() {
            // Mapeia cada IP para seu Bucket individual
            private final Map<String, Bucket> ddosBuckets = new ConcurrentHashMap<>();
            // Mapeia cada IP para o tempo (em milissegundos) até o qual ele estará na blacklist
            private final Map<String, Long> blacklist = new ConcurrentHashMap<>();
            // Define duração da blacklist (10 minutos)
            private final long BLACKLIST_DURATION_MS = Duration.ofMinutes(10).toMillis();

            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                    throws ServletException, IOException {
                String ipAddress = request.getRemoteAddr();
                long now = System.currentTimeMillis();

                // Verifica se o IP está na blacklist e se o tempo de bloqueio ainda não expirou
                Long blacklistedUntil = blacklist.get(ipAddress);
                if (blacklistedUntil != null) {
                    if (now < blacklistedUntil) {
                        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                        response.getWriter().write("Seu IP foi bloqueado por excesso de requisições. Tente novamente mais tarde.");
                        return;
                    } else {
                        // Remove da blacklist se o tempo expirou
                        blacklist.remove(ipAddress);
                    }
                }

                // Recupera ou cria o Bucket para esse IP (limite: 100 requisições por minuto)
                Bucket bucket = ddosBuckets.computeIfAbsent(ipAddress, ip ->
                        Bucket4j.builder()
                                .addLimit(Bandwidth.classic(100, Refill.greedy(100, Duration.ofMinutes(1))))
                                .build()
                );

                if (bucket.tryConsume(1)) {
                    filterChain.doFilter(request, response);
                } else {
                    // Se exceder o limite, adiciona o IP à blacklist
                    blacklist.put(ipAddress, now + BLACKLIST_DURATION_MS);
                    response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                    response.getWriter().write("Muitas requisições. Seu IP foi bloqueado temporariamente.");
                    return;
                }
            }
        };
    }

    // Filtro de rate limiting para /auth/login
    @Bean
    public OncePerRequestFilter loginRateLimitFilter() {
        return new OncePerRequestFilter() {
            private final Bucket bucket = Bucket4j.builder()
                    .addLimit(Bandwidth.classic(10, Refill.greedy(3, Duration.ofMinutes(1))))
                    .build();

            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                    throws ServletException, IOException {
                if ("/auth/login".equals(request.getRequestURI())) {
                    if (bucket.tryConsume(1)) {
                        filterChain.doFilter(request, response);
                    } else {
                        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                        response.getWriter().write("Muitas tentativas de login. Tente novamente mais tarde.");
                        return;
                    }
                } else {
                    filterChain.doFilter(request, response);
                }
            }
        };
    }

    // Filtro de rate limiting para /auth/register, limitando registros por IP
    @Bean
    public OncePerRequestFilter registrationRateLimitFilter() {
        return new OncePerRequestFilter() {
            private final Map<String, Bucket> registrationBuckets = new ConcurrentHashMap<>();
            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                    throws ServletException, IOException {
                if ("/auth/register".equals(request.getRequestURI()) && "POST".equalsIgnoreCase(request.getMethod())) {
                    String ipAddress = request.getRemoteAddr();
                    Bucket bucket = registrationBuckets.computeIfAbsent(ipAddress, ip ->
                            Bucket4j.builder()
                                    // Aumentado para permitir 20 registros por minuto por IP
                                    .addLimit(Bandwidth.classic(20, Refill.greedy(20, Duration.ofMinutes(1))))
                                    .build()
                    );
                    if (bucket.tryConsume(1)) {
                        filterChain.doFilter(request, response);
                    } else {
                        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                        response.getWriter().write("Muitas tentativas de registro para o IP: " + ipAddress + ". Tente novamente mais tarde.");
                        return;
                    }
                } else {
                    filterChain.doFilter(request, response);
                }
            }
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/auth/**", "/api/**", "/swagger-ui/**",
                                "/v3/api-docs/**", "/swagger-resources/**",
                                "/webjars/**", "/v2/api-docs/**").permitAll()
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                // Adiciona os filtros de rate limiting: primeiro o DDOS, depois registro e login, depois o JWT.
                .addFilterBefore(ddosRateLimitFilter(), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(registrationRateLimitFilter(), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(loginRateLimitFilter(), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Value("${cors.allowedOrigins}")
    private String allowedOrigin;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(allowedOrigin));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}