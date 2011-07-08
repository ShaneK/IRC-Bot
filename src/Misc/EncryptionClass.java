package Misc;

import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.spec.PKCS8EncodedKeySpec;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;

public class EncryptionClass {

	public String decryptRSA(byte[] encoded, Key privateKey) throws Exception {
		Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		cipher.init(Cipher.DECRYPT_MODE, privateKey);
		byte[] original = cipher.doFinal(encoded);
		String originalString = new String(original);
		return originalString;
	}

	public byte[] encryptRSA(String plainText, Key publicKey) throws Exception {
		Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		cipher.init(Cipher.ENCRYPT_MODE, publicKey);
		byte[] encrypted = cipher.doFinal(plainText.getBytes());
		return encrypted;
	}

	public Key stringToKeyAES(String key) throws Exception {
		MessageDigest digester = MessageDigest.getInstance("MD5");
		char[] password = key.toCharArray();
		for (int i = 0; i < password.length; i++) {
			digester.update((byte) password[i]);
		}
		byte[] passwordData = digester.digest();
		return new SecretKeySpec(passwordData, "AES");
	}

	public byte[] encryptAES(String plainText, Key secretKey) throws Exception {
		Cipher cipher = Cipher.getInstance("AES");
		cipher.init(Cipher.ENCRYPT_MODE, secretKey);
		byte[] encrypted = cipher.doFinal(plainText.getBytes());
		return encrypted;
	}

	public byte[] encryptAES(String plainText, String key) throws Exception {
		Key secretKey = stringToKeyAES(key);

		Cipher cipher = Cipher.getInstance("AES");
		cipher.init(Cipher.ENCRYPT_MODE, secretKey);
		byte[] encrypted = cipher.doFinal(plainText.getBytes());
		return encrypted;
	}

	public String decryptAES(byte[] encoded, Key secretKey) throws Exception {
		Cipher cipher = Cipher.getInstance("AES");
		cipher.init(Cipher.DECRYPT_MODE, secretKey);
		byte[] original = cipher.doFinal(encoded);
		String originalString = new String(original);
		return originalString;
	}

	public String decryptAES(byte[] encoded, String key) throws Exception {
		Key secretKey = stringToKeyAES(key);

		Cipher cipher = Cipher.getInstance("AES");
		cipher.init(Cipher.DECRYPT_MODE, secretKey);
		byte[] original = cipher.doFinal(encoded);
		String originalString = new String(original);
		return originalString;
	}
	
	public Key getPrivateKeyFromEncodedKey(byte[] encodedKey) throws Exception{
		PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encodedKey);
	    KeyFactory keyFactory = KeyFactory.getInstance("RSA");
	    return keyFactory.generatePrivate(keySpec);
	}
	
	public Key getPublicKeyFromEncodedKey(byte[] encodedKey) throws Exception{
		PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encodedKey);
	    KeyFactory keyFactory = KeyFactory.getInstance("RSA");
	    return keyFactory.generatePublic(keySpec);
	}
	
	public KeyPair generateKeyPairRSA() throws Exception{
		return KeyPairGenerator.getInstance("RSA").generateKeyPair();
	}
}