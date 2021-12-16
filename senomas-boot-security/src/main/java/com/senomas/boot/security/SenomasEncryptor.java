package com.senomas.boot.security;

import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

public abstract class SenomasEncryptor {
	private final static String kyz = "dkFWQ1ZFZFJZWFpWRGRSVEVSQw==";
	private final static String kxz = "shFG8b8KzqkD/vzLHKdMsg==";

	public static String encrypt(String text) {
		byte xbx[] = Base64.decodeBase64(kyz);
		for (int i = 0, il = xbx.length; i < il; i++)
			xbx[i] = (byte) (xbx[i] ^ 0x37);

		byte bx[] = Base64.decodeBase64(kxz);
		for (int i = 0, il = bx.length, j = 0, jl = xbx.length; i < il; i++) {
			bx[i] = (byte) (bx[i] ^ xbx[j]);
			if (++j >= jl)
				j = 0;
		}
		SecretKey key = new SecretKeySpec(bx, 0, bx.length, "AES");
		for (int i = 0, il = bx.length; i < il; i++) {
			bx[i] = 0;
		}
		try {
		    SecureRandom randomSecureRandom = SecureRandom.getInstance("SHA1PRNG");
		    byte[] ivb = new byte[16];
		    randomSecureRandom.nextBytes(ivb);
		    IvParameterSpec iv = new IvParameterSpec(ivb);
		    
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, key, iv);
			
			bx = cipher.doFinal(text.getBytes("UTF-8"));
			byte ex[] = new byte[bx.length+ivb.length];
			System.arraycopy(bx, 0, ex, 0, bx.length);
			System.arraycopy(ivb, 0, ex, bx.length, ivb.length);

			return Base64.encodeBase64String(ex);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public static String decrypt(String text) {
		byte xbx[] = Base64.decodeBase64(kyz);
		for (int i = 0, il = xbx.length; i < il; i++)
			xbx[i] = (byte) (xbx[i] ^ 0x37);

		byte bx[] = Base64.decodeBase64(kxz);
		for (int i = 0, il = bx.length, j = 0, jl = xbx.length; i < il; i++) {
			bx[i] = (byte) (bx[i] ^ xbx[j]);
			if (++j >= jl)
				j = 0;
		}
		SecretKey key = new SecretKeySpec(bx, 0, bx.length, "AES");
		for (int i = 0, il = bx.length; i < il; i++) {
			bx[i] = 0;
		}
		try {
			bx = Base64.decodeBase64(text);
		    byte[] ivb = new byte[16];
		    
		    byte ex[] = new byte[bx.length-ivb.length];
		    
		    System.arraycopy(bx, 0, ex, 0, ex.length);
		    System.arraycopy(bx, ex.length, ivb, 0, ivb.length);
		    IvParameterSpec iv = new IvParameterSpec(ivb);
			
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipher.init(Cipher.DECRYPT_MODE, key, iv);
			
			return new String(cipher.doFinal(ex), "UTF-8");
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}
	
	public static void main(String args[]) {
		for (;;) {
			System.out.print("Enter plain text: ");
			char plain[] = System.console().readPassword();
			if (plain.length == 0) return;
			System.out.println("\nENC("+encrypt(new String(plain))+")\n");
		}
	}
}
