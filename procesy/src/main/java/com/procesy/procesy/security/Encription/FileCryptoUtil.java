package com.procesy.procesy.security.Encription;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.util.Base64;

public class FileCryptoUtil {

    // Configurações AES
    private static final String AES_ALGORITHM = "AES";
    private static final String AES_TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int AES_KEY_SIZE = 256;
    private static final int IV_LENGTH = 12; // 96 bits
    private static final int GCM_TAG_LENGTH = 128;

    //lOGGER
    private static final Logger LOGGER = LoggerFactory.getLogger(FileCryptoUtil.class);

    // Configurações RSA
    private static final String RSA_ALGORITHM = "RSA";
    private static final String RSA_TRANSFORMATION = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding";

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
        System.out.println("FileCryptoUtil.encryptFile");
        // Geração da chave AES
        KeyGenerator keyGen = KeyGenerator.getInstance(AES_ALGORITHM);
        keyGen.init(AES_KEY_SIZE);
        SecretKey aesKey = keyGen.generateKey();

        // Geração do IV
        System.out.println("Gerando IV...");
        byte[] iv = new byte[IV_LENGTH];
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);

        // Criptografia AES
        System.out.println("Criptografando dados...");
        Cipher aesCipher = Cipher.getInstance(AES_TRANSFORMATION);
        aesCipher.init(Cipher.ENCRYPT_MODE, aesKey, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
        byte[] encryptedData = aesCipher.doFinal(fileData);

        // Criptografia RSA da chave AES
        System.out.println("Criptografando chave AES...");
        Cipher rsaCipher = Cipher.getInstance(RSA_TRANSFORMATION);
        rsaCipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] encryptedKey = rsaCipher.doFinal(aesKey.getEncoded());

        return new EncryptedFileData(encryptedData, encryptedKey, iv);
    }

    public static byte[] decryptFile(EncryptedFileData encryptedData, PrivateKey privateKey) throws Exception {
        // Descriptografia da chave AES
        System.out.println("Descriptografando chave AES...");
        LOGGER.error("Descriptografando chave AES...");
        Cipher rsaCipher = Cipher.getInstance(RSA_TRANSFORMATION);
        rsaCipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] aesKeyBytes = rsaCipher.doFinal(encryptedData.getEncryptedKey());
        SecretKey aesKey = new SecretKeySpec(aesKeyBytes, AES_ALGORITHM);

        System.out.println("Descriptografando dados...");
        LOGGER.error("Descriptografando dados...");
        // Descriptografia dos dados
        Cipher aesCipher = Cipher.getInstance(AES_TRANSFORMATION);
        aesCipher.init(Cipher.DECRYPT_MODE, aesKey, new GCMParameterSpec(GCM_TAG_LENGTH, encryptedData.getIv()));
        return aesCipher.doFinal(encryptedData.getEncryptedData());
    }
}