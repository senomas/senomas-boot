package com.senomas.common;

import java.nio.charset.Charset;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

public class Digester {
	private static final Charset UTF8 = Charset.forName("UTF8");

	protected final MessageDigest digest;

	public Digester() {
		try {
			digest = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public void update(byte v[]) {
		if (v == null)
			return;
		digest.update(v);
	}

	public void update(byte v[], int offset, int len) {
		digest.update(v, offset, len);
	}

	public void update(byte v[][]) {
		if (v == null)
			return;
		for (byte vi[] : v) {
			digest.update(vi);
		}
	}

	public void update(Long ov) {
		if (ov == null)
			return;
		long v = ov;
		digest.update((byte) (v & 0xFF));
		v >>= 8;
		digest.update((byte) (v & 0xFF));
		v >>= 8;
		digest.update((byte) (v & 0xFF));
		v >>= 8;
		digest.update((byte) (v & 0xFF));
		v >>= 8;
		digest.update((byte) (v & 0xFF));
		v >>= 8;
		digest.update((byte) (v & 0xFF));
		v >>= 8;
		digest.update((byte) (v & 0xFF));
		v >>= 8;
		digest.update((byte) (v & 0xFF));
	}


	public void update(Integer ov) {
		if (ov == null)
			return;
		int v = ov;
		digest.update((byte) (v & 0xFF));
		v >>= 8;
		digest.update((byte) (v & 0xFF));
		v >>= 8;
		digest.update((byte) (v & 0xFF));
		v >>= 8;
		digest.update((byte) (v & 0xFF));
	}
	public void update(Date ov) {
		if (ov == null)
			return;
		long v = ov.getTime();
		digest.update((byte) (v & 0xFF));
		v >>= 8;
		digest.update((byte) (v & 0xFF));
		v >>= 8;
		digest.update((byte) (v & 0xFF));
		v >>= 8;
		digest.update((byte) (v & 0xFF));
		v >>= 8;
		digest.update((byte) (v & 0xFF));
		v >>= 8;
		digest.update((byte) (v & 0xFF));
		v >>= 8;
		digest.update((byte) (v & 0xFF));
		v >>= 8;
		digest.update((byte) (v & 0xFF));
	}

	public void update(String v) {
		if (v == null)
			return;
		digest.update(v.getBytes(UTF8));
	}

	public void update(Key v) {
		digest.update(v.getEncoded());
	}

	public byte[] digest() {
		return digest.digest();
	}
}
