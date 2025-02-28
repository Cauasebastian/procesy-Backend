package com.procesy.procesy.security.Encription;

import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class KeyConverterUtil {

    public static PublicKey convertPublicKey(byte[] publicKeyBytes) {
        try {
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePublic(keySpec);
        } catch (Exception e) {
            throw new RuntimeException("Falha na conversão da chave pública", e);
        }
    }

    public static PrivateKey convertPrivateKey(String privateKeyBase64) {
        try {
            byte[] privateKeyBytes = Base64.getDecoder().decode(privateKeyBase64);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PrivateKey privateKey = keyFactory.generatePrivate(keySpec);

            // Validação do tamanho da chave (mínimo 2048 bits)
            int keyLengthInBits = privateKey.getEncoded().length * 8;
            if (keyLengthInBits < 2048) {
                throw new IllegalArgumentException("Chave RSA muito fraca. Tamanho mínimo necessário: 2048 bits.");
            }
            return privateKey;
        } catch (Exception e) {
            throw new RuntimeException("Falha na conversão da chave privada", e);
        }
    }
}