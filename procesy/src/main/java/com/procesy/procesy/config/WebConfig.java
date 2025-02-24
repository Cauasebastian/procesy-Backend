package com.procesy.procesy.config;

import com.procesy.procesy.security.Encription.PrivateKeyInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final PrivateKeyInterceptor privateKeyInterceptor;

    public WebConfig(PrivateKeyInterceptor privateKeyInterceptor) {
        this.privateKeyInterceptor = privateKeyInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(privateKeyInterceptor)
                .addPathPatterns("/api/documento-processo/**"); // Ajuste os paths conforme necessário
    }
}