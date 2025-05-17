package com.procesy.procesy.security.Encription;


import com.procesy.procesy.security.Encription.KeyConverterUtil;
import com.procesy.procesy.security.Encription.PrivateKeyHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import java.security.PrivateKey;

@Component
public class PrivateKeyInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String privateKeyHeader = request.getHeader("X-Private-Key");

        if (privateKeyHeader != null && !privateKeyHeader.isEmpty()) {
            if (!privateKeyHeader.matches("^[A-Za-z0-9+/]+={0,2}$")) {
                throw new IllegalArgumentException("Chave privada em formato inválido.");
            }
            PrivateKey privateKey = KeyConverterUtil.convertPrivateKey(privateKeyHeader);
            PrivateKeyHolder.setPrivateKey(privateKey);
        }

        return true;
    }
// cauasebastian222207@gmail.com

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        PrivateKeyHolder.clear();
    }
}