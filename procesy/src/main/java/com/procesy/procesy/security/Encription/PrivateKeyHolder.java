package com.procesy.procesy.security.Encription;
import java.security.PrivateKey;

public class PrivateKeyHolder {
    private static final ThreadLocal<PrivateKey> privateKeyThreadLocal = new ThreadLocal<>();

    public static void setPrivateKey(PrivateKey privateKey) {
        privateKeyThreadLocal.set(privateKey);
    }

    public static PrivateKey getPrivateKey() {
        return privateKeyThreadLocal.get();
    }

    public static void clear() {
        privateKeyThreadLocal.remove();
    }
}