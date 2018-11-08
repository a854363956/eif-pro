package org.eif.models.utils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.DigestException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/***
 * CryptoJS 加密解密类
 * 
 * @author zhangj
 * @date 2018年9月10日 下午8:48:12
 * @email zhangjin0908@hotmail.com
 */
public class CryptoJS {
	public static class AES {
		/***
		 * 对字符串进行AES解码
		 * 
		 * @param body 要解码的内容
		 * @param key  要解码的key
		 * @return
		 */
		public static String decrypt(String cipherText, String sKey)
				throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
				InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, IOException {
			byte[] cipherData = CryptoJS.RSA.base64Decode(cipherText);
			byte[] saltData = Arrays.copyOfRange(cipherData, 8, 16);
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			final byte[][] keyAndIV = GenerateKeyAndIV(32, 16, 1, saltData, sKey.getBytes(StandardCharsets.UTF_8),
					md5);
			SecretKeySpec key = new SecretKeySpec(keyAndIV[0], "AES");
			IvParameterSpec iv = new IvParameterSpec(keyAndIV[1]);

			byte[] encrypted = Arrays.copyOfRange(cipherData, 16, cipherData.length);
			Cipher aesCBC = Cipher.getInstance("AES/CBC/PKCS5Padding");
			aesCBC.init(Cipher.DECRYPT_MODE, key, iv);
			byte[] decryptedData = aesCBC.doFinal(encrypted);
			return new String(decryptedData, StandardCharsets.UTF_8);
		}

		private static byte[][] GenerateKeyAndIV(int keyLength, int ivLength, int iterations, byte[] salt,
				byte[] password, MessageDigest md) {
			int digestLength = md.getDigestLength();
			int requiredLength = (keyLength + ivLength + digestLength - 1) / digestLength * digestLength;
			byte[] generatedData = new byte[requiredLength];
			int generatedLength = 0;

			try {
				md.reset();
				while (generatedLength < keyLength + ivLength) {
					if (generatedLength > 0)
						md.update(generatedData, generatedLength - digestLength, digestLength);
					md.update(password);
					if (salt != null)
						md.update(salt, 0, 8);
					md.digest(generatedData, generatedLength, digestLength);
					for (int i = 1; i < iterations; i++) {
						md.update(generatedData, generatedLength, digestLength);
						md.digest(generatedData, generatedLength, digestLength);
					}
					generatedLength += digestLength;
				}
				byte[][] result = new byte[2][];
				result[0] = Arrays.copyOfRange(generatedData, 0, keyLength);
				if (ivLength > 0)
					result[1] = Arrays.copyOfRange(generatedData, keyLength, keyLength + ivLength);
				return result;
			} catch (DigestException e) {
				throw new RuntimeException(e);
			} finally {
				Arrays.fill(generatedData, (byte) 0);
			}
		}
	}

	public static class RSA {
		public static final String RSA_ALGORITHM = "RSA";
		public static final Charset UTF8 = Charset.forName("UTF-8");

		public static KeyPair buildKeyPair() throws NoSuchAlgorithmException {
			final int keySize = 2048;
			KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(RSA_ALGORITHM);
			keyPairGenerator.initialize(keySize);
			return keyPairGenerator.genKeyPair();
		}

		public static byte[] encrypt(PrivateKey privateKey, String message) throws Exception {
			Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
			cipher.init(Cipher.ENCRYPT_MODE, privateKey);

			return cipher.doFinal(message.getBytes(UTF8));
		}

		public static byte[] encrypt(PublicKey publicKey, String message) throws Exception {
			Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
			cipher.init(Cipher.ENCRYPT_MODE, publicKey);
			return cipher.doFinal(message.getBytes(UTF8));
		}

		public static byte[] decrypt(PublicKey publicKey, byte[] encrypted) throws Exception {
			Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
			cipher.init(Cipher.DECRYPT_MODE, publicKey);

			return cipher.doFinal(encrypted);
		}

		public static byte[] decrypt(PrivateKey privateKey, byte[] encrypted) throws Exception {
			Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
			cipher.init(Cipher.DECRYPT_MODE, privateKey);
			return cipher.doFinal(encrypted);
		}

		public static String base64Encode(byte[] data) {
			return Base64.encode(data);
		}

		public static byte[] base64Decode(String data) throws IOException {
			return Base64.decode(data);
		}

		public static PublicKey getPublicKey(String key) throws Exception {
			byte[] keyBytes;
			keyBytes = base64Decode(key);

			X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			PublicKey publicKey = keyFactory.generatePublic(keySpec);
			return publicKey;
		}

		public static PrivateKey getPrivateKey(String key) throws Exception {
			byte[] keyBytes;
			keyBytes = base64Decode(key);

			PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
			return privateKey;
		}
	}
}