package com.procesy.procesy.security.Encription;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

public class FileCryptoUtil {

    private static final String AES_ALGORITHM = "AES";
    private static final String AES_TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int AES_KEY_SIZE = 256;
    private static final int IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;
    private static final Logger LOGGER = LoggerFactory.getLogger(FileCryptoUtil.class);

    // Cache de IVs com Caffeine (expira após 5 minutos)
    private static final Cache<String, Boolean> ivCache = Caffeine.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build();

    public static class EncryptedFileData {
        private final byte[] encryptedData;
        private final byte[] encryptedKey;
        private final byte[] iv;

        public EncryptedFileData(byte[] encryptedData, byte[] encryptedKey, byte[] iv) {
            this.encryptedData = encryptedData;
            this.encryptedKey = encryptedKey;
            this.iv = iv;
        }

        public byte[] getEncryptedData() { return encryptedData; }
        public byte[] getEncryptedKey() { return encryptedKey; }
        public byte[] getIv() { return iv; }
    }

    public static EncryptedFileData encryptFile(byte[] fileData, PublicKey publicKey) throws Exception {
        // Geração da chave AES
        KeyGenerator keyGen = KeyGenerator.getInstance(AES_ALGORITHM);
        keyGen.init(AES_KEY_SIZE);
        SecretKey aesKey = keyGen.generateKey();

        // Geração do IV
        byte[] iv = new byte[IV_LENGTH];
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);

        // Verificação de IV único
        String ivB64 = Base64.getEncoder().encodeToString(iv);
        if (ivCache.getIfPresent(ivB64) != null) {
            LOGGER.warn("Tentativa de reutilização de IV detectada: {}", ivB64);
            throw new SecurityException("IV já utilizado");
        }
        ivCache.put(ivB64, true);

        // Criptografia AES
        Cipher aesCipher = Cipher.getInstance(AES_TRANSFORMATION);
        aesCipher.init(Cipher.ENCRYPT_MODE, aesKey, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
        byte[] encryptedData = aesCipher.doFinal(fileData);

        // Criptografia RSA
        Cipher rsaCipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
        rsaCipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] encryptedKey = rsaCipher.doFinal(aesKey.getEncoded());

        return new EncryptedFileData(encryptedData, encryptedKey, iv);
    }

    public static byte[] decryptFile(EncryptedFileData encryptedData, PrivateKey privateKey) throws Exception {
        // Descriptografia RSA
        Cipher rsaCipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
        rsaCipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] aesKeyBytes = rsaCipher.doFinal(encryptedData.getEncryptedKey());
        SecretKey aesKey = new SecretKeySpec(aesKeyBytes, AES_ALGORITHM);

        // Descriptografia AES
        Cipher aesCipher = Cipher.getInstance(AES_TRANSFORMATION);
        aesCipher.init(Cipher.DECRYPT_MODE, aesKey, new GCMParameterSpec(GCM_TAG_LENGTH, encryptedData.getIv()));
        return aesCipher.doFinal(encryptedData.getEncryptedData());
    }
}