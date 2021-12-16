package com.senomas.test;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class Util {
	public static final char GEN_IDZ[] = "qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM1234567890-_".toCharArray();
	public static final int GEN_IDZL = GEN_IDZ.length;
	static final byte compressBZ[][] = {
		Util.getBytes(" 0123456789.,|-/"),
		Util.getBytes(" 0123456789.qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM"),
		Util.getBytes(" 0123456789.,|-/\\qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM<>{}();:~!@#$%^&*_+")
	};
	static AtomicInteger uid = new AtomicInteger();
	static final Random rnd = new Random(System.currentTimeMillis());
	static byte ipid[];
	
	public static byte[] getBytes(String str) {
		byte b[] = new byte[str.length()];
		for (int i = 0, il = b.length; i < il; i++) {
			int c = str.charAt(i);
			b[i] = (byte) c;
		}
		return b;
	}
	
	public static boolean compressAllIn(byte b[], byte bz[]) {
		for (int i=0, il=b.length; i<il; i++) {
			boolean neq = true;
			for (int j=0, jl=bz.length; neq && j<jl; j++) {
				if (b[i] == bz[j]) neq = false;
			}
			if (neq) return false;
		}
		return true;
	}

	public static String genID(byte b[]) {
		StringBuilder sb = new StringBuilder();
		long v = 0, vx = 1;
		int ix = 0, ixl = b.length;
		int bx = b[ix++] & 0xFF;
		v = bx;
		vx *= 256;
		bx = b[ix++] & 0xFF;
		v = v * 256 + bx;
		vx *= 256;
		while (vx > 0) {
			if (vx < 256 && ix < ixl) {
				bx = b[ix++] & 0xFF;
				v = v * 256 + bx;
				vx *= 256;
			}
			sb.append(GEN_IDZ[(int) (v % GEN_IDZL)]);
			v /= GEN_IDZL;
			vx /= GEN_IDZL;
		}
		return sb.toString();
	}
	
	public static String getUID() {
		if (ipid == null) {
			try {
				String str = InetAddress.getLocalHost().getHostAddress() + "|" + InetAddress.getLocalHost().getHostName();
				ipid = MessageDigest.getInstance("SHA-1").digest(str.getBytes());
			} catch (Exception ex) {
				ipid = "localhost".getBytes();
			}
		}
		ByteBuffer bb = ByteBuffer.allocate(64);
		bb.putInt((int) (uid.addAndGet(1 + rnd.nextInt(16)) % Integer.MAX_VALUE));
		bb.putLong(System.currentTimeMillis());
		bb.flip();
		byte b[] = new byte[bb.remaining()];
		bb.get(b);
		bb.compact();
		for (int i = b.length - 1; i >= 0; i--) {
			bb.put(b[i]);
		}
		bb.put(ipid);
		bb.flip();
		b = new byte[bb.remaining()];
		bb.get(b);
		String str = genID(b);
		return str.substring(0, 24);
	}

	public static void main(String args[]) {
		Set<String> ids = new HashSet<>();
		Thread t[] = new Thread[1000];
		for (int i=0, il=t.length; i<il; i++) {
			t[i] = new Thread(new Runnable() {
				@Override
				public void run() {
					for (int i=0; i<1000; i++) {
						String id = getUID();
						synchronized (ids) {
							if (ids.contains(id)) throw new RuntimeException("DUPLICATE");
							ids.add(id);
						}
						System.out.println("ID("+i+") = "+id);
					}
				}
			});
		}
		for (int i=0, il=t.length; i<il; i++) t[i].start();
		for (int i=0, il=t.length; i<il; i++) {
			try {
				t[i].join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.out.println("TOTAL: "+ids.size());
	}
}
