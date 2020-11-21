package com.sioeye.youle.common.zuul.util;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.Signature;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import javax.crypto.Cipher;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

/**
 *  签名帮助类，本类完成以下功能：
 * <ul>
 * <li>非对称秘钥的生成</li>
 * <li>公钥验证签名</li>
 * <li>私钥签名</li>
 * </ul>
 * 
 * @author jinze.yuan
 * @email jinze.yuan@sioeye.com
 * 
 */
public class SignatureUtils {

	static {
		Security.addProvider(new BouncyCastleProvider());
	}

	/**
	 * 非对程算法
	 */
	public enum AsymmetricAlgorithm {
		RSA;
	}

	/**
	 * 签名算法
	 */
	public enum SignatureAlgorithm {
		/*
		 * 用SHA算法进行签名，用RSA算法进行加密 在对进行SHA1算法进行签名后，要求对签名后的数据进行处理，而不是直接进行RSA算法进行加密
		 */
		SHA1WithRSA,
		/*
		 * 这个算法其实是两个算法的叠加：MD5算法和RSA算法。 MD5算法是不可逆的，RSA算法是非对称加密算法
		 */
		MD5WithRSA
	}

	/**
	 * 生成公钥和私钥,默认采用RSA算法
	 * 
	 * @param algorithm 非对称算法名称：如：RSA
	 * @param keySize   密钥长度 1024/2048
	 * @throws NoSuchAlgorithmException
	 *
	 */
	public static KeyPair generateKeys(AsymmetricAlgorithm algorithm, int keySize) {
		try {
			KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(algorithm.name());
			keyPairGen.initialize(keySize);
			KeyPair keyPair = keyPairGen.generateKeyPair();
			return keyPair;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 使用模和指数生成公钥
	 * 注意：【此代码用了默认补位方式，为RSA/None/PKCS1Padding，不同JDK默认的补位方式可能不同，如Android默认是RSA/
	 * None/NoPadding】
	 *
	 * @param modulus  模
	 * @param exponent 指数
	 * @return
	 */
	public static RSAPublicKey generatePublicKey(AsymmetricAlgorithm algorithm, String modulus, String exponent) {
		try {
			BigInteger b1 = new BigInteger(modulus);
			BigInteger b2 = new BigInteger(exponent);
			KeyFactory keyFactory = KeyFactory.getInstance(algorithm.name());
			RSAPublicKeySpec keySpec = new RSAPublicKeySpec(b1, b2);
			return (RSAPublicKey) keyFactory.generatePublic(keySpec);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 使用模和指数生成私钥
	 * 注意：【此代码用了默认补位方式，为RSA/None/PKCS1Padding，不同JDK默认的补位方式可能不同，如Android默认是RSA/
	 * None/NoPadding]
	 *
	 * @param modulus  模
	 * @param exponent 指数
	 * @return
	 */
	public static RSAPrivateKey generateRSAPrivateKey(AsymmetricAlgorithm algorithm, String modulus, String exponent) {
		try {
			BigInteger b1 = new BigInteger(modulus);
			BigInteger b2 = new BigInteger(exponent);
			KeyFactory keyFactory = KeyFactory.getInstance(algorithm.name());
			RSAPrivateKeySpec keySpec = new RSAPrivateKeySpec(b1, b2);
			return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 利用公钥对象进行加密
	 * 
	 * @param algorithm
	 * @param data
	 * @param publicKey
	 * @return
	 * @throws Exception
	 */
	public static String encryptByPublicKey(AsymmetricAlgorithm algorithm, String data, RSAPublicKey publicKey)
			throws Exception {
		Cipher cipher = Cipher.getInstance(algorithm.name());
		cipher.init(Cipher.ENCRYPT_MODE, publicKey);
		// 模长
		int key_len = publicKey.getModulus().bitLength() / 8;
		// 加密数据长度 <= 模长-11
		String[] datas = splitString(data, key_len - 11);
		String mi = "";
		// 如果明文长度大于模长-11则要分组加密
		for (String s : datas) {
			mi += bcd2Str(cipher.doFinal(s.getBytes()));
		}
		return mi;
	}

	/**
	 * 利用公钥字符串进行加密
	 * 
	 * @param data
	 * @param publicKeyString
	 * @return
	 */
	public static String encryptByPublicKey(AsymmetricAlgorithm algorithm, String data, String publicKeyString) {
		try {
			byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyString);

			X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
			KeyFactory keyFactory = KeyFactory.getInstance(algorithm.name());
			PublicKey publickey = keyFactory.generatePublic(keySpec);

			return encryptByPublicKey(algorithm, data, (RSAPublicKey) publickey);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 采用私钥对象解密
	 *
	 * @param data
	 * @param privateKey
	 * @return
	 * @throws Exception
	 */
	public static String decryptByPrivateKey(AsymmetricAlgorithm algorithm, String data, RSAPrivateKey privateKey)
			throws Exception {
		Cipher cipher = Cipher.getInstance(algorithm.name());
		cipher.init(Cipher.DECRYPT_MODE, privateKey);
		// 模长
		int key_len = privateKey.getModulus().bitLength() / 8;
		byte[] bytes = data.getBytes();
		byte[] bcd = ASCII_To_BCD(bytes, bytes.length);
		System.err.println(bcd.length);
		// 如果密文长度大于模长则要分组解密
		String ming = "";
		byte[][] arrays = splitArray(bcd, key_len);
		for (byte[] arr : arrays) {
			ming += new String(cipher.doFinal(arr));
		}
		return ming;
	}

	/**
	 * 采用私钥字符串解密数据
	 * 
	 * @param data
	 * @param privateKeyString
	 * @return
	 */
	public static String decryptByPrivateKey(AsymmetricAlgorithm algorithm, String data, String privateKeyString) {
		try {
			byte[] privateKeyBytes = Base64.getDecoder().decode(privateKeyString);
			X509EncodedKeySpec keySpec = new X509EncodedKeySpec(privateKeyBytes);
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			PrivateKey privateKey = keyFactory.generatePrivate(keySpec);

			return decryptByPrivateKey(algorithm, data, (RSAPrivateKey) privateKey);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 采用给定的私钥对象对数据进行指定的算法进行签名
	 * 
	 * @param data
	 * @param rsaPrivateKey
	 * @return
	 */
	public static String signByPrivateKey(SignatureAlgorithm algorithm, byte[] data, PrivateKey rsaPrivateKey) {
		try {
			Signature signature = Signature.getInstance(algorithm.name());
			signature.initSign(rsaPrivateKey);
			signature.update(data);
			return Base64.getEncoder().encodeToString(signature.sign());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 采用给定的私钥字符串对数据进行指定的算法进行签名
	 * 
	 * @param data
	 * @param privateKeyString Base64编码的秘钥字符串
	 * @return
	 */
	public static String signByPrivateKey(AsymmetricAlgorithm asymmetricAlgorithm,
			SignatureAlgorithm signatureAlgorithm, byte[] data, String privateKeyString) {
		try {
			byte[] privateKeyBytes = Base64.getDecoder().decode(privateKeyString);
			PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
			KeyFactory keyFactory = KeyFactory.getInstance(asymmetricAlgorithm.name());
			PrivateKey privateKey = keyFactory.generatePrivate(keySpec);

			return signByPrivateKey(signatureAlgorithm, data, privateKey);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 采用公钥验证数字签名
	 * 
	 * @param publicKeyString
	 * @param signatureAlgorithm
	 * @param signatureString    签名base64编码的字符串
	 * @param plainString        被签名的原文字符串
	 * @return
	 */
	public static boolean verifyByPublicKey(AsymmetricAlgorithm algorithm, SignatureAlgorithm signatureAlgorithm,
			String publicKeyString, String signatureString, String plainString) {
		boolean result = false;
		try {
			byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyString);

			X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
			KeyFactory keyFactory = KeyFactory.getInstance(algorithm.name());
			PublicKey publickey = keyFactory.generatePublic(keySpec);

			Signature signature = Signature.getInstance(signatureAlgorithm.name());
			signature.initVerify(publickey);
			signature.update(plainString.getBytes());
			// 验证签名是否正常
			result = signature.verify(Base64.getDecoder().decode(signatureString));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * ASCII码转BCD码
	 *
	 */
	public static byte[] ASCII_To_BCD(byte[] ascii, int asc_len) {
		byte[] bcd = new byte[asc_len / 2];
		int j = 0;
		for (int i = 0; i < (asc_len + 1) / 2; i++) {
			bcd[i] = asc_to_bcd(ascii[j++]);
			bcd[i] = (byte) (((j >= asc_len) ? 0x00 : asc_to_bcd(ascii[j++])) + (bcd[i] << 4));
		}
		return bcd;
	}

	public static byte asc_to_bcd(byte asc) {
		byte bcd;

		if ((asc >= '0') && (asc <= '9'))
			bcd = (byte) (asc - '0');
		else if ((asc >= 'A') && (asc <= 'F'))
			bcd = (byte) (asc - 'A' + 10);
		else if ((asc >= 'a') && (asc <= 'f'))
			bcd = (byte) (asc - 'a' + 10);
		else
			bcd = (byte) (asc - 48);
		return bcd;
	}

	/**
	 * BCD转字符串
	 */
	public static String bcd2Str(byte[] bytes) {
		char temp[] = new char[bytes.length * 2], val;

		for (int i = 0; i < bytes.length; i++) {
			val = (char) (((bytes[i] & 0xf0) >> 4) & 0x0f);
			temp[i * 2] = (char) (val > 9 ? val + 'A' - 10 : val + '0');

			val = (char) (bytes[i] & 0x0f);
			temp[i * 2 + 1] = (char) (val > 9 ? val + 'A' - 10 : val + '0');
		}
		return new String(temp);
	}

	/**
	 * 拆分字符串
	 */
	public static String[] splitString(String string, int len) {
		int x = string.length() / len;
		int y = string.length() % len;
		int z = 0;
		if (y != 0) {
			z = 1;
		}
		String[] strings = new String[x + z];
		String str = "";
		for (int i = 0; i < x + z; i++) {
			if (i == x + z - 1 && y != 0) {
				str = string.substring(i * len, i * len + y);
			} else {
				str = string.substring(i * len, i * len + len);
			}
			strings[i] = str;
		}
		return strings;
	}

	/**
	 * 拆分数组
	 */
	public static byte[][] splitArray(byte[] data, int len) {
		int x = data.length / len;
		int y = data.length % len;
		int z = 0;
		if (y != 0) {
			z = 1;
		}
		byte[][] arrays = new byte[x + z][];
		byte[] arr;
		for (int i = 0; i < x + z; i++) {
			arr = new byte[len];
			if (i == x + z - 1 && y != 0) {
				System.arraycopy(data, i * len, arr, 0, y);
			} else {
				System.arraycopy(data, i * len, arr, 0, len);
			}
			arrays[i] = arr;
		}
		return arrays;
	}
}