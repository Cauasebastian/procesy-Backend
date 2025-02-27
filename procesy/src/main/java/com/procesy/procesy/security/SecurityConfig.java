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

    // Filtro de Rate Limiting usando Bucket4j para o endpoint /auth/login
    @Bean
    public OncePerRequestFilter rateLimitFilter() {
        return new OncePerRequestFilter() {

            /*
            Como funciona essa configuração?
            No início, o bucket pode armazenar até 10 tokens.
            Para cada requisição, 1 token é consumido.
            Se o bucket esvaziar, o usuário precisa esperar para que novos tokens sejam adicionados.
            A cada minuto, o bucket recebe 3 novos tokens, até atingir no máximo 10 tokens.
             Isso significa que, se o usuário tentar fazer login mais de 10 vezes em um minuto, ele receberá um erro 429.
             */
            private final Bucket bucket = Bucket4j.builder()
                    .addLimit(Bandwidth.classic(10, Refill.greedy(3, Duration.ofMinutes(1))))
                    .build();

            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                    throws ServletException, IOException {
                // Verifica se o endpoint é /auth/login
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
                // Adiciona o filtro de rate limiting antes do filtro de autenticação JWT
                .addFilterBefore(rateLimitFilter(), UsernamePasswordAuthenticationFilter.class)
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
