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

    // Filtro de rate limiting para /auth/login (limita tentativas de login)
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

    // Filtro de rate limiting para /auth/register (limita registros por IP)
    @Bean
    public OncePerRequestFilter registrationRateLimitFilter() {
        return new OncePerRequestFilter() {
            // Mapeia IPs para buckets individuais
            private final Map<String, Bucket> registrationBuckets = new ConcurrentHashMap<>();

            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                    throws ServletException, IOException {
                if ("/auth/register".equals(request.getRequestURI()) && "POST".equalsIgnoreCase(request.getMethod())) {
                    String ipAddress = request.getRemoteAddr();
                    // Cria ou recupera o bucket para esse IP
                    Bucket bucket = registrationBuckets.computeIfAbsent(ipAddress, ip ->
                            Bucket4j.builder()
                                    // Permite, por exemplo, 5 registros por minuto para cada IP
                                    .addLimit(Bandwidth.classic(5, Refill.greedy(5, Duration.ofMinutes(1))))
                                    .build()
                    );
                    if (bucket.tryConsume(1)) {
                        filterChain.doFilter(request, response);
                    } else {
                        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                        response.getWriter().write("Muitas tentativas de registro. Tente novamente mais tarde.");
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
                .csrf(csrf -> csrf.disable()) // Desativa CSRF
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // Habilita CORS com a configuração definida
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/auth/**", "/api/**", "/swagger-ui/**",
                                "/v3/api-docs/**", "/swagger-resources/**",
                                "/webjars/**", "/v2/api-docs/**").permitAll()
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // Define sessão como stateless para JWT
                )
                // Adiciona os filtros de rate limiting antes do filtro de autenticação JWT
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
        configuration.setAllowedOrigins(Arrays.asList(allowedOrigin)); // Permite apenas o frontend específico
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS")); // Métodos HTTP permitidos
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type")); // Cabeçalhos permitidos
        configuration.setAllowCredentials(true); // Permite envio de credenciais (cookies, headers)
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // Aplica a configuração para todas as rotas
        return source;
    }
}
