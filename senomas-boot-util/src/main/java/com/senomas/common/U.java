package com.senomas.common;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.interfaces.PBEKey;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.module.SimpleModule;

public abstract class U {
	protected static final char[] hexArray = "0123456789abcdef".toCharArray();
	private static final char CHARZ[] = "qwertyuiopasdfghjklzxcvbnm1234567890QWERTYUIOPASDFGHJKLZXCVBNM".toCharArray();
	private static final char DIGITZ[] = "0123456789".toCharArray();

	private static final int MAX_DUMP_DEPTH = 10;
	public static SecretKey cryptKey;
	public static final Charset UTF8 = Charset.forName("UTF-8");
	private static final SecureRandom rnd = new SecureRandom();
//	public static final byte crix[];

//	static {
//		byte[] iv = new byte[] { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0a, 0x0b, 0x0c, 0x0d,
//				0x0e, 0x0f };
//		for (int i = 0, il = iv.length; i < il; i++) {
//			iv[i] = (byte) (iv[i] ^ 27);
//		}
//		try {
//			PBEKeySpec password = new PBEKeySpec("agus@senomas.com---dodoldurenbakwan".toCharArray(), iv, 1000, 128);
//			SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
//			PBEKey pkey = (PBEKey) factory.generateSecret(password);
//			cryptKey = new SecretKeySpec(pkey.getEncoded(), "AES");
//		} catch (Exception e) {
//			throw new RuntimeException(e.getMessage(), e);
//		}
//		crix = cryptKey.getEncoded();
//	}

	public static void initLog4J() {
		if (!org.apache.log4j.Logger.getRootLogger().getAllAppenders().hasMoreElements()) {
			org.apache.log4j.BasicConfigurator.configure();
			// for (Enumeration<org.apache.log4j.Appender> itrApp =
			// org.apache.log4j.Logger.getRootLogger().getAllAppenders();
			// itrApp.hasMoreElements(); ) {
			// org.apache.log4j.Appender ap = itrApp.nextElement();
			// ap.setLayout(new org.apache.log4j.PatternLayout("%d{HH:mm:ss,SSS}
			// [%t] %-5p %c %x %X - %m%n"));
			// }
		}
	}

	// public static void setLogLevel(String name, String level) {
	// Logger log = LoggerFactory.getLogger(name);
	// }

	private static final ObjectWriter jsonObjectWriter;

	static {
		ObjectMapper om = new ObjectMapper();
		SimpleModule module = new SimpleModule("senomas",
				new Version(1, 0, 0, null, "com.senomas.common", "senomas-common"));
		module.addSerializer(PublicKey.class, new JsonPublicKeySerializer());
		module.addDeserializer(PublicKey.class, new JsonPublicKeyDeserializer());
		om.registerModule(module);
		jsonObjectWriter = om.writerWithDefaultPrettyPrinter();
	}

	public static String dumpJson(Object obj) {
		try {
			return jsonObjectWriter.writeValueAsString(obj);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public static void LAZY_DEBUG(Object... msgs) {
		StackTraceElement[] st = Thread.currentThread().getStackTrace();
		if (st.length >= 2) {
			Logger log = LoggerFactory.getLogger(st[2].getClassName());
			if (log.isDebugEnabled()) {
				StringBuilder sb = new StringBuilder();
				sb.append("MARK at ").append(st[2].getClassName()).append(".").append(st[2].getMethodName()).append("(")
						.append(st[2].getFileName()).append(":").append(st[2].getLineNumber()).append(")");
				for (int i = 0, il = msgs.length; i < il; i++) {
					sb.append(' ');
					if (msgs[i] instanceof String || msgs[i] instanceof Number) {
						sb.append(msgs[i]);
					} else {
						sb.append(U.dump(msgs[i]));
					}
				}
				log.debug(sb.toString());
			}
		} else {
			Logger log = LoggerFactory.getLogger(U.class);
			log.debug("MARK");
		}
	}
	
	public static void deleteTree(File file) {
		if (file.isFile()) {
			file.delete();
		} else if (file.isDirectory()) {
			for (File f : file.listFiles()) {
				deleteTree(f);
			}
			file.delete();
		}
	}

	public static Date getToday() {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
			return sdf.parse(sdf.format(new Date()));
		} catch (ParseException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public static String formatDateTime(Date date) {
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSSS");
		return sdf.format(date);
	}

	public static String formatTime(Date date) {
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSSS");
		return sdf.format(date);
	}

	public static String formatDate(Date date) {
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		return sdf.format(date);
	}

	public static <T> T getOne(Collection<T> list) {
		Iterator<T> itr = list.iterator();
		if (itr.hasNext())
			return itr.next();
		return null;
	}

	public static PublicKey getPublicKey(byte key[]) {
		if (key == null)
			return null;
		try {
			return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(key));
		} catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public static PrivateKey getPrivateKey(byte key[]) {
		if (key == null)
			return null;
		try {
			return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(key));
		} catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public static byte[] toByte(KeyPair keyPair) {
		byte bk[] = keyPair.getPublic().getEncoded();
		byte bp[] = keyPair.getPrivate().getEncoded();
		byte bx[] = new byte[bk.length + bp.length + 2];
		bx[0] = (byte) (0xFF & (bk.length / 256));
		bx[1] = (byte) (0xFF & bk.length);
		System.arraycopy(bk, 0, bx, 2, bk.length);
		System.arraycopy(bp, 0, bx, 2 + bk.length, bp.length);
		return bx;
	}

	public static KeyPair genKeyPair(int length) {
		try {
			KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
			keyGen.initialize(length);
			return keyGen.generateKeyPair();
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public static SecretKey genSecretKey(int length) {
		try {
			KeyGenerator keyGen = KeyGenerator.getInstance("AES");
			keyGen.init(length);
			return keyGen.generateKey();
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public static KeyPair getKeyPair(byte key[]) {
		if (key == null)
			return null;
		int bl = ((int) (0xFF & key[0])) * 256 + (0xFF & key[1]);
		byte bk[] = new byte[bl];
		byte bp[] = new byte[key.length - bl - 2];
		System.arraycopy(key, 2, bk, 0, bk.length);
		System.arraycopy(key, 2 + bk.length, bp, 0, bp.length);
		return new KeyPair(getPublicKey(bk), getPrivateKey(bp));
	}

	public static SecretKey getSecretKey(byte key[]) {
		if (key == null)
			return null;
		return new SecretKeySpec(key, 0, key.length, "AES");
	}

	public static byte[] encrypt(Key key, byte bb[]) {
		try {
			Cipher cipher;
			if (key instanceof SecretKey) {
				cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
				cipher.init(Cipher.ENCRYPT_MODE, key);
				return cipher.doFinal(bb);
			} else {
				cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
				cipher.init(Cipher.ENCRYPT_MODE, key);
				int bz = cipher.getOutputSize(1) - 20;
				int pos = 0, rem = bb.length, wo;
				byte bo[] = cipher.doFinal(bb, pos, wo = Math.min(rem, bz));
				pos += wo;
				rem -= wo;
				byte res[] = new byte[((bb.length + bz - 1) / bz) * bo.length];
				System.arraycopy(bo, 0, res, 0, bo.length);
				int rp = bo.length;
				while (rem > 0) {
					bo = cipher.doFinal(bb, pos, wo = Math.min(rem, bz));
					pos += wo;
					rem -= wo;
					System.arraycopy(bo, 0, res, rp, bo.length);
					rp += bo.length;
				}
				return res;
			}
		} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException
				| BadPaddingException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public static byte[] decrypt(Key key, byte bb[]) {
		try {
			Cipher cipher;
			if (key instanceof SecretKey) {
				cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
				cipher.init(Cipher.DECRYPT_MODE, key);
				return cipher.doFinal(bb);
			} else if (key instanceof PrivateKey) {
				cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
				cipher.init(Cipher.DECRYPT_MODE, key);
				int bz = cipher.getOutputSize(1);
				ByteArrayOutputStream bout = new ByteArrayOutputStream();
				int rem = bb.length;
				int pos = 0;
				int ro;
				while (rem > 0) {
					try {
						bout.write(cipher.doFinal(bb, pos, ro = Math.min(rem, bz)));
					} catch (IOException e) {
						throw new RuntimeException(e.getMessage(), e);
					}
					pos += ro;
					rem -= ro;
				}
				return bout.toByteArray();
			} else if (key == null) {
				return null;
			}
			throw new RuntimeException("Not supported key " + key);
		} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException
				| BadPaddingException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public static <T> boolean contain(T[] arrays, T v) {
		for (T t : arrays) {
			if (t.equals(v))
				return true;
		}
		return false;
	}

	public static String randomText(int len) {
		char cz[] = new char[len];
		int CZL = CHARZ.length;
		for (int i = 0; i < len; i++) {
			cz[i] = CHARZ[rnd.nextInt(CZL)];
		}
		return new String(cz);
	}

	public static String randomDigit(int len) {
		char cz[] = new char[len];
		int CZL = DIGITZ.length;
		for (int i = 0; i < len; i++) {
			cz[i] = DIGITZ[rnd.nextInt(CZL)];
		}
		return new String(cz);
	}

	public static String randomHex(int len) {
		char cz[] = new char[len];
		int CZL = hexArray.length;
		for (int i = 0; i < len; i++) {
			cz[i] = hexArray[rnd.nextInt(CZL)];
		}
		return new String(cz);
	}

	public static boolean testPANCheckDigit(String pan) {
		long val = 0;
		for (int p = 0, i = pan.length() - 1; i >= 0; i--, p++) {
			if (p % 2 == 0) {
				val += pan.charAt(i) - '0';
				// log.info("VAL "+i+" "+val+" "+pan.charAt(i));
			} else {
				int d = (pan.charAt(i) - '0') * 2;
				if (d >= 10) {
					val += 1 + (d % 10);
				} else {
					val += d;
				}
				// log.info("VAL "+i+" "+val+" "+pan.charAt(i)+" "+d);
			}
		}
		// log.info("VAL "+val);
		val = val % 10;
		// log.info("RES "+val);
		return val == 0;
	}

	public static byte[] getBytes(String str) {
		return str.getBytes(UTF8);
	}

	public static byte[] getBytes(long value) {
		return new byte[] { (byte) (value >> 56), (byte) (value >> 48), (byte) (value >> 40), (byte) (value >> 32),
				(byte) (value >> 24), (byte) (value >> 16), (byte) (value >> 8), (byte) value };
	}

	public static String toString(byte bb[]) {
		return new String(bb, UTF8);
	}

	public static String encode64(byte b[]) {
		return Base64.encodeBase64String(b);
	}

	public static String encode64(byte b[], int offset, int len) {
		if (offset == 0 && len == b.length) {
			return Base64.encodeBase64String(b);
		} else {
			byte bb[] = new byte[len];
			System.arraycopy(b, offset, bb, 0, len);
			return Base64.encodeBase64String(bb);
		}
	}

	public static byte[] decode64(String s) {
		return Base64.decodeBase64(s);
	}

	public static String encode64URL(byte b[]) {
		char[] chz = Base64.encodeBase64String(b).toCharArray();
		for (int i = 0, il = chz.length; i < il; i++) {
			switch (chz[i]) {
			case '+':
				chz[i] = '_';
				break;
			case '/':
				chz[i] = ',';
				break;
			case '=':
				chz[i] = '-';
			}
		}
		return new String(chz);
	}

	public static byte[] decode64URL(String s) {
		char[] chz = s.toCharArray();
		for (int i = 0, il = chz.length; i < il; i++) {
			switch (chz[i]) {
			case '_':
				chz[i] = '+';
				break;
			case ',':
				chz[i] = '/';
				break;
			case '-':
				chz[i] = '=';
			}
		}
		s = new String(chz);
		return Base64.decodeBase64(s);
	}

	public static String encodeURL(String url) {
		try {
			return URLEncoder.encode(url, "utf-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public static String decodeURL(String url) {
		try {
			return URLDecoder.decode(url, "utf-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public static String escape(byte bb[]) {
		try {
			return URLEncoder.encode(new String(bb), "UTF8").replaceAll("\\+", " ");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public static String toHex(byte b[]) {
		if (b == null)
			return null;
		return toHex(b, 0, b.length);
	}

	public static String toHex(byte b[], int offset, int length) {
		if (b == null)
			return null;
		StringBuilder sb = new StringBuilder();
		for (int i = offset, il = Math.min(b.length, offset + length); i < il; i++) {
			// if (i != offset) sb.append(' ');
			int x = b[i];
			if (x < 0)
				x += 256;
			int d = (x / 16) & 0xF;
			sb.append(hexArray[d]);
			d = x & 0xF;
			sb.append(hexArray[d]);
		}
		return sb.toString();
	}

	public static byte[] fromHex(String str) {
		int bl = 0;
		for (int i = 0, il = str.length(); i < il; i++) {
			char ch = str.charAt(i);
			if ((ch >= '0' && ch <= '9') || (ch >= 'a' && ch <= 'f') || (ch >= 'A' && ch <= 'F')) {
				bl++;
			} else if (Character.isWhitespace(ch)) {
				// ignore
			} else {
				throw new RuntimeException("Invalid char '" + ch + "' at " + i + " [" + str + "]");
			}
		}
		if (bl % 2 == 1)
			throw new RuntimeException("Invalid hex length " + bl + " [" + str + "]");
		bl /= 2;
		byte b[] = new byte[bl];
		for (int i = 0, il = str.length(), bi = 0; i < il; i++) {
			char ch = str.charAt(i);
			if (!Character.isWhitespace(ch)) {
				int bx = 0;
				if (ch >= '0' && ch <= '9') {
					bx = (ch - '0') * 16;
				} else if (ch >= 'a' && ch <= 'f') {
					bx = (ch - 'a' + 10) * 16;
				} else if (ch >= 'A' && ch <= 'F') {
					bx = (ch - 'A' + 10) * 16;
				}
				ch = str.charAt(++i);
				if (ch >= '0' && ch <= '9') {
					bx += (ch - '0');
				} else if (ch >= 'a' && ch <= 'f') {
					bx += (ch - 'a' + 10);
				} else if (ch >= 'A' && ch <= 'F') {
					bx += (ch - 'A' + 10);
				}
				b[bi++] = (byte) bx;
			}
		}
		return b;
	}

	public static String digest(String... text) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			for (String t : text) {
				md.update(getBytes(t));
			}
			return encode64(md.digest());
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public static byte[] read(InputStream in) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte bb[] = new byte[1024];
		int len;
		while ((len = in.read(bb)) >= 0) {
			out.write(bb, 0, len);
		}
		return out.toByteArray();
	}

	static ThreadLocal<Integer> dumpTab = new ThreadLocal<Integer>();

	public static String dump(byte bb[], int offset, int length) {
		boolean ascii = true;
		for (int i = offset, il = offset + length; i < il && ascii; i++) {
			byte b = bb[i];
			ascii = (b >= 10 && b < 128);
		}
		if (ascii) {
			try {
				return length + " [" + URLEncoder.encode(new String(bb, offset, length), "UTF8").replaceAll("\\+", " ")
						+ "]";
			} catch (UnsupportedEncodingException e) {
				return "ERROR<<:" + e.getMessage() + ">>";
			}
		} else {
			return length + " HEX [" + toHex(bb, offset, length) + "]";
		}
	}

	public static String dump(Object obj) {
		if (dumpTab.get() != null)
			throw new RuntimeException("RECURSIVE CALL");
		dumpTab.set(0);
		try {
			DumpOutputStream dos = new DumpOutputStream();
			PrintWriter out = new PrintWriter(dos);

			dump(out, null, obj, new IdentityHashMap<Object, String>(), 0);

			out.close();
			return new String(dos.toByteArray());
		} finally {
			dumpTab.remove();
		}
	}

	public static String dumpName(Class<?> cz) {
		if (cz.isArray()) {
			return dumpName(cz.getComponentType()) + "[]";
		}
		return cz.getName();
	}

	public static void dump(PrintWriter out, Object ref, Object o, IdentityHashMap<Object, String> rec, int depth) {
		String recID = rec.get(o);
		if (o != null && recID == null) {
			rec.put(o, dumpName(o.getClass()) + "@" + Integer.toHexString(System.identityHashCode(o)).toUpperCase());
		}
		if (o == null) {
			if (ref instanceof Method) {
				out.println(dumpName(((Method) ref).getReturnType()) + " NULL");
			} else {
				out.println("NULL");
			}
		} else if (o instanceof Number) {
			if (ref instanceof Method) {
				Format f = (Format) ((Method) ref).getAnnotation(Format.class);
				if (f != null) {
					out.println(dumpName(o.getClass()) + " [" + new DecimalFormat(f.value()).format(o) + "]");
				} else {
					out.println(dumpName(o.getClass()) + " ["
							+ new DecimalFormat("#,##0.#####################").format(o) + "]");
				}
			} else {
				out.println(dumpName(o.getClass()) + " [" + new DecimalFormat("#,##0.#####################").format(o)
						+ "]");
			}
		} else if (o instanceof String) {
			out.println(dumpName(o.getClass()) + " [" + o + "]");
		} else if (o instanceof Date || o instanceof Calendar) {
			if (ref instanceof Method) {
				Format f = (Format) ((Method) ref).getAnnotation(Format.class);
				if (f != null) {
					out.println(dumpName(o.getClass()) + " [" + new SimpleDateFormat(f.value()).format(o) + "]");
				} else {
					out.println(dumpName(o.getClass()) + " ["
							+ new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS").format(o) + "]");
				}
			} else {
				out.println(dumpName(o.getClass()) + " [" + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS").format(o)
						+ "]");
			}
		} else if (recID != null) {
			out.println(dumpName(o.getClass()) + " RECURSIVE " + recID);
		} else if (depth > MAX_DUMP_DEPTH) {
			out.println("TOO DEEP " + dumpName(o.getClass()) + "@"
					+ Integer.toHexString(System.identityHashCode(o)).toUpperCase());
		} else if (o instanceof byte[]) {
			byte bb[] = (byte[]) o;
			boolean ascii = true;
			for (int i = 0, il = bb.length; i < il && ascii; i++) {
				byte b = bb[i];
				ascii = (b >= 10 && b < 128);
			}
			if (ascii) {
				try {
					out.println(dumpName(o.getClass()) + " " + bb.length + " ["
							+ URLEncoder.encode(new String(bb), "UTF8").replaceAll("\\+", " ") + "]");
				} catch (UnsupportedEncodingException e) {
				}
			} else {
				out.println(dumpName(o.getClass()) + " " + bb.length + " HEX [" + toHex(bb) + "]");
			}
		} else if (o instanceof Key) {
			out.println(dumpName(o.getClass()) + " HEX [" + toHex(((Key) o).getEncoded()) + "]");
		} else if (o instanceof Source) {
			try {
				out.flush();
				out.println();
				TransformerFactory tf = TransformerFactory.newInstance();
				Transformer transformer = tf.newTransformer();
				transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
				transformer.setOutputProperty(OutputKeys.METHOD, "xml");
				transformer.setOutputProperty(OutputKeys.INDENT, "yes");
				transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
				transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
				transformer.transform((Source) o, new StreamResult(out));
				out.flush();
			} catch (Exception e) {
				out.print(" {exception:" + e.getMessage() + "} ");
			}
		} else if (o instanceof Node) {
			try {
				out.flush();
				out.println();
				TransformerFactory tf = TransformerFactory.newInstance();
				Transformer transformer = tf.newTransformer();
				transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
				transformer.setOutputProperty(OutputKeys.METHOD, "xml");
				transformer.setOutputProperty(OutputKeys.INDENT, "yes");
				transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
				transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
				transformer.transform(new DOMSource((Node) o), new StreamResult(out));
				out.flush();
			} catch (Exception e) {
				out.print(" {exception:" + e.getMessage() + "} ");
			}
		} else if (o.getClass().isArray()) {
			int len = Array.getLength(o);
			if (len == 0) {
				out.println(dumpName(o.getClass()) + " [size:" + len + "]");
			} else {
				out.flush();
				dumpTab.set(dumpTab.get() + 1);
				out.println(dumpName(o.getClass()) + " [size:" + len);
				for (int i = 0, idx = 1; i < len; i++, idx++) {
					out.print(idx + " = ");
					dump(out, null, Array.get(o, i), rec, depth + 1);
				}
				out.flush();
				dumpTab.set(dumpTab.get() - 1);
				out.println("]");
			}
			// } else if (o.getClass().getName().startsWith("org.apache.")) {
			// out.println(dumpName(o.getClass())+" ["+o+"]");
		} else if (o instanceof Enumeration<?>) {
			Enumeration<?> en = (Enumeration<?>) o;
			out.flush();
			dumpTab.set(dumpTab.get() + 1);
			out.println(dumpName(o.getClass()) + " [");
			int idx = 1;
			while (en.hasMoreElements()) {
				Object co = en.nextElement();
				out.print(idx + " = ");
				dump(out, null, co, rec, depth + 1);
				idx++;
			}
			out.flush();
			dumpTab.set(dumpTab.get() - 1);
			out.println("]");
		} else if (o instanceof Collection<?>) {
			Collection<?> col = (Collection<?>) o;
			out.flush();
			dumpTab.set(dumpTab.get() + 1);
			out.println(dumpName(o.getClass()) + " [size:" + col.size());
			int idx = 1;
			for (Object co : col) {
				out.print(idx + " = ");
				dump(out, null, co, rec, depth + 1);
				idx++;
			}
			out.flush();
			dumpTab.set(dumpTab.get() - 1);
			out.println("]");
		} else if (o instanceof Map<?, ?>) {
			Map<?, ?> map = (Map<?, ?>) o;
			out.flush();
			dumpTab.set(dumpTab.get() + 1);
			out.println(dumpName(o.getClass()) + " [size:" + map.size());
			for (Map.Entry<?, ?> me : map.entrySet()) {
				out.print("[" + me.getKey().toString() + "] = ");
				dump(out, null, me.getValue(), rec, depth + 1);
			}
			out.flush();
			dumpTab.set(dumpTab.get() - 1);
			out.println("]:MAP(" + o.getClass().getName() + ")");
		} else if (o instanceof Annotation) {
			Class<? extends Annotation> type = ((Annotation) o).annotationType();
			out.flush();
			dumpTab.set(dumpTab.get() + 1);
			out.println(type.getName() + " {");
			for (Method m : o.getClass().getMethods()) {
				if (m.getParameterTypes().length == 0) {
					try {
						Object result = m.invoke(o);
						out.print(m.getName());
						out.print(" = ");
						if (result != null) {
							out.println(dumpName(result.getClass()) + "@"
									+ Integer.toHexString(System.identityHashCode(result)).toUpperCase());
						} else {
							out.println("NULL");
						}
					} catch (Exception e) {
						// IGNORE
					}
				}
			}
			out.flush();
			dumpTab.set(dumpTab.get() - 1);
			out.println("}");
		} else if (o.getClass().getName().startsWith("com.senomas.")
				|| o.getClass().getName().startsWith("id.co.hanoman.")) {
			try {
				Method m = o.getClass().getMethod("dump", PrintWriter.class);
				m.invoke(o, out);
				return;
			} catch (NoSuchMethodException e) {
				// IGNORE
			} catch (Exception e) {
				out.print(" {exception:" + e.getMessage() + "} ");
			}
			out.flush();
			dumpTab.set(dumpTab.get() + 1);
			out.println(dumpName(o.getClass()) + "@" + Integer.toHexString(System.identityHashCode(o)).toUpperCase()
					+ " {");
			boolean notIgnoreNull = o.getClass().getAnnotation(IgnoreNull.class) == null;
			for (Method m : o.getClass().getMethods()) {
				String mn = m.getName();
				if (m.getDeclaringClass().getName().startsWith("java")) {
					// SKIP
				} else if (m.getAnnotation(Ignore.class) != null) {
					// Ignore
				} else if (m.getAnnotation(SystemID.class) != null) {
					try {
						Object result = m.invoke(o);
						if (notIgnoreNull || result != null) {
							if (m.getName().startsWith("get")) {
								out.print(Character.toLowerCase(mn.charAt(3)) + mn.substring(4));
							} else {
								out.print(Character.toLowerCase(mn.charAt(2)) + mn.substring(3));
							}
							out.print(" = ");
							if (result != null) {
								out.println(dumpName(result.getClass()) + "@"
										+ Integer.toHexString(System.identityHashCode(result)).toUpperCase());
							} else {
								out.println("NULL");
							}
						}
					} catch (Exception e) {
						// IGNORE
					}
				} else if (!m.getReturnType().equals(Void.class) && m.getParameterTypes().length == 0
						&& ((mn.startsWith("get") && mn.length() > 3) || (mn.startsWith("is") && mn.length() > 2))) {
					try {
						Object result = m.invoke(o);
						if (notIgnoreNull || result != null) {
							if (m.getName().startsWith("get")) {
								out.print(Character.toLowerCase(mn.charAt(3)) + mn.substring(4));
							} else {
								out.print(Character.toLowerCase(mn.charAt(2)) + mn.substring(3));
							}
							out.print(" = ");
							dump(out, m, result, rec, depth + 1);
						}
					} catch (Exception e) {
						// IGNORE
					}
				}
			}
			out.flush();
			dumpTab.set(dumpTab.get() - 1);
			out.println("}");
			// out.println(o.getClass().getName()+" ["+o+"]");
		} else {
			out.println(dumpName(o.getClass()) + " [" + o + "]");
		}
	}

	public static class DumpOutputStream extends OutputStream {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		boolean nl = false;
		int p = 0;
		byte tabz[] = "                                                                                                            "
				.getBytes();

		@Override
		public void write(byte[] bb, int off, int len) throws IOException {
			int tab = dumpTab.get();
			for (int i = 0, p = off; i < len; i++, p++) {
				byte b = bb[p];
				if (nl) {
					if (b == '\n' || b == '\r') {
						out.write(b);
					} else {
						out.write(tabz, 0, Math.min(tab * 3, tabz.length));
						out.write(b);
						nl = false;
					}
				} else if (b == '\n') {
					out.write(b);
					nl = true;
				} else {
					out.write(b);
				}
			}
		}

		@Override
		public void write(int b) throws IOException {
			int tab = dumpTab.get();
			if (nl) {
				if (b == '\n' || b == '\r') {
					out.write(b);
				} else {
					out.write(tabz, 0, Math.min(tab * 3, tabz.length));
					out.write(b);
					nl = false;
				}
			} else if (b == '\n') {
				out.write(b);
				nl = true;
			} else {
				out.write(b);
			}
		}

		@Override
		public void close() throws IOException {
			out.close();
			super.close();
		}

		public byte[] toByteArray() {
			return out.toByteArray();
		}
	}

	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.RUNTIME)
	public @interface Ignore {
	}

	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.RUNTIME)
	public @interface Format {
		String value();
	}

	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.RUNTIME)
	public @interface SystemID {

	}

	@Target(ElementType.TYPE)
	@Retention(RetentionPolicy.RUNTIME)
	public @interface IgnoreNull {
	}

	public interface Streamer {
		void stream(InputStream in);
	}

	public static void open(File file, Streamer streamer) {
		InputStream in = null;
		try {
			in = new FileInputStream(file);
			streamer.stream(in);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (Exception e) {
				}
			}
		}
	}

	public static void open(String name, Streamer streamer) {
		InputStream in = null;
		try {
			in = new FileInputStream(name);
			streamer.stream(in);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (Exception e) {
				}
			}
		}
	}

	public static int nvl(Integer v, int nvl) {
		return v != null ? v : nvl;
	}

	public static String nvl(String v, String nvl) {
		return v != null ? v : nvl;
	}

	public static boolean nvl(Boolean v, boolean nvl) {
		return v != null ? v : nvl;
	}

	public static BigDecimal nvl(BigDecimal v, BigDecimal nvl) {
		return v != null ? v : nvl;
	}

	public static String getNetworkInterface() throws SocketException {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		for (NetworkInterface iface : Collections.list(NetworkInterface.getNetworkInterfaces())) {
			pw.println("IF [" + iface.getDisplayName() + "]");
			byte hwa[] = iface.getHardwareAddress();
			if (hwa != null && hwa.length > 0) {
				pw.print("\t[HW-ADDR:");
				for (int i = 0, il = hwa.length; i < il; i++) {
					int iv = hwa[i] > 0 ? hwa[i] : 256 + hwa[i];
					String s = "0" + Integer.toHexString(iv).toUpperCase();
					if (i > 0) {
						pw.print('.');
					}
					pw.print(s.substring(s.length() - 2));
				}
				pw.println("]");
			}
			for (NetworkInterface vIface : Collections.list(iface.getSubInterfaces())) {
				pw.println("\t[VIRT:" + vIface.getDisplayName() + "]");
				for (InetAddress vAddr : Collections.list(vIface.getInetAddresses())) {
					pw.println("\t[VHOST-ADDR:" + vAddr.getHostAddress() + "]");
				}
			}
			for (InetAddress rAddr : Collections.list(iface.getInetAddresses())) {
				pw.println("\t[HOST-ADDR:" + rAddr.getHostAddress() + "]");
			}
		}
		pw.flush();
		return sw.toString();
	}
}
