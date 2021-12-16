package com.senomas.common;

import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Date;

public class Signer {
	private static final Charset UTF8 = Charset.forName("UTF8");
	
	protected final Signature sig;

	public Signer(PublicKey key) {
		try {
			sig = Signature.getInstance("SHA256withRSA");
			sig.initVerify(key);
		} catch (NoSuchAlgorithmException | InvalidKeyException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public Signer(PrivateKey key) {
		try {
			sig = Signature.getInstance("SHA256withRSA");
			sig.initSign(key);
		} catch (NoSuchAlgorithmException | InvalidKeyException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public void update(byte v[]) {
		try {
			if (v == null)
				return;
			sig.update(v);
		} catch (SignatureException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public void update(byte v[], int offset, int len) {
		try {
			sig.update(v, offset, len);
		} catch (SignatureException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public void update(byte v[][]) {
		try {
			if (v == null)
				return;
			for (byte vi[] : v) {
				sig.update(vi);
			}
		} catch (SignatureException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public void update(Long ov) {
		try {
			if (ov == null)
				return;
			long v = ov;
			sig.update((byte) (v & 0xFF));
			v >>= 8;
			sig.update((byte) (v & 0xFF));
			v >>= 8;
			sig.update((byte) (v & 0xFF));
			v >>= 8;
			sig.update((byte) (v & 0xFF));
			v >>= 8;
			sig.update((byte) (v & 0xFF));
			v >>= 8;
			sig.update((byte) (v & 0xFF));
			v >>= 8;
			sig.update((byte) (v & 0xFF));
			v >>= 8;
			sig.update((byte) (v & 0xFF));
		} catch (SignatureException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public void update(Integer ov) {
		try {
			if (ov == null)
				return;
			int v = ov;
			sig.update((byte) (v & 0xFF));
			v >>= 8;
			sig.update((byte) (v & 0xFF));
			v >>= 8;
			sig.update((byte) (v & 0xFF));
			v >>= 8;
			sig.update((byte) (v & 0xFF));
		} catch (SignatureException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public void update(Date ov) {
		try {
			if (ov == null)
				return;
			long v = ov.getTime();
			sig.update((byte) (v & 0xFF));
			v >>= 8;
			sig.update((byte) (v & 0xFF));
			v >>= 8;
			sig.update((byte) (v & 0xFF));
			v >>= 8;
			sig.update((byte) (v & 0xFF));
			v >>= 8;
			sig.update((byte) (v & 0xFF));
			v >>= 8;
			sig.update((byte) (v & 0xFF));
			v >>= 8;
			sig.update((byte) (v & 0xFF));
			v >>= 8;
			sig.update((byte) (v & 0xFF));
		} catch (SignatureException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public void update(String v) {
		try {
			if (v == null)
				return;
			sig.update(v.getBytes(UTF8));
		} catch (SignatureException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public void update(Key v) {
		try {
			sig.update(v.getEncoded());
		} catch (SignatureException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}
	
	public byte[] sign() {
		try {
			return sig.sign();
		} catch (SignatureException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}
	
	public boolean verify(byte signature[]) {
		try {
			return sig.verify(signature);
		} catch (SignatureException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}
}
