package de.tiwa.snclient;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class CryptoHelper {

    static public byte[] calcHmacSha256(byte[] secretKey, byte[] message) {
        byte[] hmacSha256 = null;
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey, "HmacSHA256");
            mac.init(secretKeySpec);
            hmacSha256 = mac.doFinal(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return hmacSha256;
    }

    public static String decrypt(byte[] strToDecrypt, byte[] secret, byte[] iv) {
        try {
            SecretKey secretKey = new SecretKeySpec(secret, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec);
            return new String(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt)), StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String encrypt(byte[] strToEncrypt, byte[] secret, byte[] iv) {
        try {
            SecretKey secretKey = new SecretKeySpec(secret, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec);
            return Base64.getEncoder().encodeToString(cipher.doFinal(strToEncrypt));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String decryptString(String string_to_decrypt, String itemUuid, String encryption_key,
            String auth_key) throws DecoderException {

        String[] components = string_to_decrypt.split(":");
        String version = components[0];
        String auth_hash = components[1];
        String uuid = components[2];
        String IV = components[3];
        String ciphertext = components[4];
        if (!itemUuid.equals(uuid)) {
            throw new InternalError("decryption went wrong");
        }
        String string_to_auth = String.join(":", version, uuid, IV, ciphertext);
        byte[] hmacSha256 = CryptoHelper.calcHmacSha256(Hex.decodeHex(auth_key),
                string_to_auth.getBytes(StandardCharsets.UTF_8));
        byte[] hmac = Hex.decodeHex(auth_hash);

        if (!Arrays.equals(hmacSha256, hmac)) {
            throw new InternalError("wrong hmac");
        }
        return CryptoHelper.decrypt(ciphertext.getBytes(), Hex.decodeHex(encryption_key), Hex.decodeHex(IV));
    }

    public static String encryptString(String string_to_encrypt, String encryption_key, String auth_key, String uuid)
            throws DecoderException {
        SecureRandom random = new SecureRandom();
        byte IV[] = new byte[16];
        random.nextBytes(IV);
        String cyphertext = encrypt(string_to_encrypt.getBytes(), Hex.decodeHex(encryption_key), IV);
        String string_to_auth = String.join(":", "003", uuid, Hex.encodeHexString(IV), cyphertext);
        byte[] auth_hash = CryptoHelper.calcHmacSha256(Hex.decodeHex(auth_key),
                string_to_auth.getBytes(StandardCharsets.UTF_8));
        return String.join(":", "003", Hex.encodeHexString(auth_hash), uuid, Hex.encodeHexString(IV), cyphertext);
    }
}