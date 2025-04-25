package com.procesy.procesy.security;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.procesy.procesy.security.Encription.FileCryptoUtil;
import com.procesy.procesy.service.AdvogadoDetailsService;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final AdvogadoDetailsService advogadoDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private static final Logger LOGGER = LoggerFactory.getLogger(FileCryptoUtil.class);

    public SecurityConfig(AdvogadoDetailsService advogadoDetailsService, JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.advogadoDetailsService = advogadoDetailsService;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    // SecurityConfig.java - Filtro DDoS
    @Bean
    public OncePerRequestFilter ddosRateLimitFilter() {
        return new OncePerRequestFilter() {
            private final Cache<String, Bucket> ddosBuckets = Caffeine.newBuilder()
                    .expireAfterAccess(30, TimeUnit.MINUTES)
                    .build();

            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                    throws ServletException, IOException {
                String ipAddress = request.getRemoteAddr();

                Bucket bucket = ddosBuckets.get(ipAddress, k ->
                        Bucket4j.builder()
                                .addLimit(Bandwidth.classic(100, Refill.intervally(100, Duration.ofMinutes(1))))
                                .build()
                );

                if (!bucket.tryConsume(1)) {
                    response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                    response.getWriter().write("Limite global de requisições excedido.");
                    return;
                }
                filterChain.doFilter(request, response);
            }
        };
    }

    // Filtro de Login com Rate Limit Global
    @Bean
    public OncePerRequestFilter loginRateLimitFilter() {
        return new OncePerRequestFilter() {
            private final Bucket bucket = Bucket4j.builder()
                    .addLimit(Bandwidth.classic(10, Refill.greedy(10, Duration.ofMinutes(1))))
                    .build();

            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                    throws ServletException, IOException {
                if ("/auth/login".equals(request.getRequestURI()) && !bucket.tryConsume(1)) {
                    response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                    LOGGER.error("---------------------------------------------------------");
                    LOGGER.warn("Muitas tentativas de login."+ request.getRemoteAddr());
                    response.getWriter().write("Muitas tentativas de login.");
                    LOGGER.error("---------------------------------------------------------");
                    return;
                }
                filterChain.doFilter(request, response);
            }
        };
    }

    // SecurityConfig.java - Filtro de Registro
    @Bean
    public OncePerRequestFilter registrationRateLimitFilter() {
        return new OncePerRequestFilter() {
            private final Cache<String, Bucket> registrationBuckets = Caffeine.newBuilder()
                    .expireAfterAccess(1, TimeUnit.HOURS)
                    .build();

            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                    throws ServletException, IOException {
                if ("/auth/register".equals(request.getRequestURI()) && "POST".equalsIgnoreCase(request.getMethod())) {
                    String ipAddress = request.getRemoteAddr();
                    Bucket bucket = registrationBuckets.get(ipAddress, k ->
                            Bucket4j.builder()
                                    .addLimit(Bandwidth.classic(20, Refill.intervally(20, Duration.ofMinutes(1))))
                                    .build()
                    );
                    if (!bucket.tryConsume(1)) {
                        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                        LOGGER.warn("Muitos registros para o IP: {}", ipAddress);
                        response.getWriter().write("Limite de registros excedido.");
                        return;
                    }
                }
                filterChain.doFilter(request, response);
            }
        };
    }

    // Restante do código mantido igual...
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
        configuration.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "X-Requested-With",
                "X-Private-Key",
                "Accept",
                "Origin",
                "Access-Control-Request-Method",
                "Access-Control-Request-Headers"
        ));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}