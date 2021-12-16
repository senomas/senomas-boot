package com.senomas.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import javax.el.MethodNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class FileDataLoader {
	private static Logger log = LoggerFactory.getLogger(FileDataLoader.class);

	protected abstract void save(Object obj);
	
	protected abstract Object find(Class<?> cl, String key, Object data);
	
	public void loadData(URLConnection conn) throws IOException {
		String url = conn.getURL().toExternalForm();
		if (url.startsWith("file:")) {
			File file = new File(url.substring(5));
			if (file.isDirectory()) {
				List<File> list = Arrays.asList(file.listFiles());
				Collections.sort(list, new Comparator<File>() {
					@Override
					public int compare(File o1, File o2) {
						return o1.getPath().compareTo(o2.getPath());
					}
				});
				for (File f : file.listFiles()) {
					if (f.isFile() && f.getName().toLowerCase().endsWith(".csv")) {
						BufferedReader in = null;
						try {
							in = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
							loadData(in);
						} finally {
							if (in != null) {
								try {
									in.close();
								} catch (Exception e) {
									log.warn(e.getMessage(), e);
								}
							}
						}
					}
				}
			} else {
				BufferedReader in = null;
				try {
					in = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
					loadData(in);
				} finally {
					if (in != null) {
						try {
							in.close();
						} catch (Exception e) {
							log.warn(e.getMessage(), e);
						}
					}
				}
			}
		}
	}
	
	protected String[] split(String txt) {
		List<String> data = new LinkedList<>();
		StringBuilder sb = new StringBuilder();
		char ct = ' ', cz = ' ';
		int cc = 0, cp = 0, state = 0;
		for (int i=0, il=txt.length(); i<=il; i++) {
			char ch = i<il ? txt.charAt(i) : ',';
			switch (state) {
			case 0:
				if (ch == '\'' || ch == '"') {
					ct = ch;
					state = 200;
				} else if (ch == '[') {
					ct = ch;
					cz = ']';
					cc = 0;
					sb.append(ch);
					state = 300;
				} else if (ch == '(') {
					ct = ch;
					cz = ')';
					cc = 0;
					sb.append(ch);
					state = 300;
				} else if (ch == '{') {
					ct = ch;
					cz = '}';
					cc = 0;
					sb.append(ch);
					state = 300;
				} else if (ch == ',') {
					data.add("");
				} else if (ch == '-') {
					state = 50;
				} else {
					sb.append(ch);
					state = 100;
				}
				break;
			case 50:
				if (ch == 'N') {
					state = 51;
				} else {
					sb.append('-');
					sb.append(ch);
					state = 100;
				}
				break;
			case 51:
				if (ch == 'U') {
					state = 52;
				} else {
					sb.append("-N");
					sb.append(ch);
					state = 100;
				}
				break;
			case 52:
				if (ch == 'L') {
					state = 53;
				} else {
					sb.append("-NU");
					sb.append(ch);
					state = 100;
				}
				break;
			case 53:
				if (ch == 'L') {
					state = 54;
				} else {
					sb.append("-NUL");
					sb.append(ch);
					state = 100;
				}
				break;
			case 54:
				if (ch == '-') {
					state = 55;
				} else {
					sb.append("-NULL");
					sb.append(ch);
					state = 100;
				}
				break;
			case 55:
				if (ch == ',') {
					data.add(null);
					state = 0;
				} else {
					sb.append("-NULL-");
					sb.append(ch);
					state = 100;
				}
				break;
			case 90:
				if (ch == 'n') {
					sb.append('\n');
				} else if (ch == 't') {
					sb.append('\t');
				} else if (ch == ',') {
					sb.append(',');
				} else if (ch == '\\') {
					sb.append('\\');
				} else if (ch == '\'') {
					sb.append('\'');
				} else if (ch == '"') {
					sb.append('"');
				} else {
					sb.append(ch);
				}
				state = cp;
				break;
			case 100:
				if (ch == ',') {
					data.add(sb.toString());
					sb.setLength(0);
					state = 0;
				} else if (ch == '\'') {
					state = 90;
					cp = 100;
				} else {
					sb.append(ch);
				}
				break;
			case 200:
				if (ch == ct) {
					state = 240;
				} else if (ch == '\\') {
					state = 90;
					cp = 200;
				} else {
					sb.append(ch);
				}
				break;
			case 240:
				if (ch == ',') {
					data.add(sb.toString());
					sb.setLength(0);
					state = 0;
				} else {
					sb.append('\'');
					sb.append(ch);
					state = 200;
				}
				break;
			case 300:
				if (ch == ct) {
					sb.append(ch);
					cc ++;
				} else if (ch == cz) {
					sb.append(ch);
					if (cc > 0) {
						cc --;
					} else {
						state = 310;
					}
				} else if (ch == '\\') {
					state = 90;
					cp = 300;
				} else {
					sb.append(ch);
				}
				break;
			case 310:
				if (ch == ',') {
					data.add(sb.toString());
					sb.setLength(0);
					state = 0;
				} else {
					throw new RuntimeException("Invalid match block "+i);
				}
				break;
			default:
				throw new RuntimeException("Invalid state "+state+" "+i);
			}
		}
		if (state != 0) throw new RuntimeException("Invalid state "+state+"  ct "+ct+" cz "+cz+" cc "+cc+" <"+sb.toString()+">");
		String a[] = new String[data.size()];
		return data.toArray(a);
	}
	
	protected Method getGetter(Class<?> cl, String field) {
		if (field.startsWith("*") || field.startsWith("+")) field = field.substring(1);
		int ix = field.indexOf(':');
		String mn = "get"+field.substring(0, 1).toUpperCase()+(ix > 0 ? field.substring(1, ix) : field.substring(1));
		for (Method mx : cl.getMethods()) {
			if (mx.getParameterTypes().length == 0 && mx.getName().equals(mn)) {
				return mx;
			}
		}
		throw new MethodNotFoundException("Method "+cl.getName()+"."+mn+" does not exists");
	}
	
	protected Method getSetter(Class<?> cl, String field) {
		if (field.startsWith("*") || field.startsWith("+")) field = field.substring(1);
		int ix = field.indexOf(':');
		String mn = "set"+field.substring(0, 1).toUpperCase()+(ix > 0 ? field.substring(1, ix) : field.substring(1));
		for (Method mx : cl.getMethods()) {
			if (mx.getParameterTypes().length == 1 && mx.getName().equals(mn)) {
				return mx;
			}
		}
		throw new MethodNotFoundException("Method "+cl.getName()+"."+mn+" does not exists");
	}
	
	@SuppressWarnings("unchecked")
	protected <T> T convert(String v, Type type) throws JsonParseException, IOException, ClassNotFoundException {
		Class<?> cl;
		if (type instanceof ParameterizedType) {
			cl = (Class<?>) ((ParameterizedType) type).getRawType();
		} else {
			cl = (Class<?>) type;
		}
		if (cl.isAssignableFrom(String.class)) {
			return (T) v;
		} else if (cl.equals(int.class) || cl.equals(Integer.class)) {
			return (T) Integer.valueOf(v);
		} else if (cl.equals(long.class) || cl.equals(Long.class)) {
			return (T) Long.valueOf(v);
		} else if (cl.equals(BigInteger.class)) {
			return (T) new BigInteger(v);
		} else if (cl.equals(BigDecimal.class)) {
			return (T) new BigDecimal(v);
		} else if (cl.isAssignableFrom(ArrayList.class)) {
			Class<?> ct = (Class<?>) ((ParameterizedType) type).getActualTypeArguments()[0];
			ArrayList<Object> lst = new ArrayList<>();
			ObjectMapper om = new ObjectMapper();
			for (JsonNode n : (JsonNode) om.getFactory().createParser(v).readValueAsTree()) {
				if (log.isTraceEnabled()) log.trace("DATA ["+n.toString()+"]");
				lst.add(om.getFactory().createParser(n.toString()).readValueAs(ct));
			}
			return (T) lst;
		}
		throw new RuntimeException("Not supported ["+type+"]");
	}
	
	protected void loadData(BufferedReader in) throws IOException {
		try {
			String className = in.readLine().trim();
			if (log.isTraceEnabled()) log.trace("CLASS "+className);
			Class<?> cl = Class.forName(className);
			String fields[] = split(in.readLine().trim());
			int key = 0;
			for (int i=0, il=fields.length; i<il; i++) {
				if (fields[i].startsWith("*")) {
					key = i;
					fields[i] = fields[i].substring(1);
				}
			}
			if (log.isTraceEnabled()) log.trace("KEY "+fields[key]);
			if (log.isTraceEnabled()) log.trace("FIELDS "+U.dump(fields));
			for (String line; (line = in.readLine()) != null; ) {
				line = line.trim();
				if (line.length() > 0) {
					String data[] = split(line);
					if (log.isTraceEnabled()) log.trace("DATA "+U.dump(data));
					Method m = getSetter(cl, fields[key]);
					Object obj = find(cl, fields[key], convert(data[key], m.getGenericParameterTypes()[0]));
					if (obj == null) {
						obj = cl.newInstance();
						setData(cl, obj, "", fields, data);
						if (log.isTraceEnabled()) log.trace("SAVE "+U.dump(obj));
						save(obj);
					} else {
						if (log.isTraceEnabled()) log.trace("EXIST "+U.dump(obj));
					}
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}
	
	protected Object setData(Class<?> cl, Object obj, String base, String fields[], String data[]) throws Exception {
		if (log.isTraceEnabled()) log.trace("SET-DATA ["+cl.getName()+"] ["+base+"] "+U.dump(obj));
		for (int i=0, il=data.length; i<il; i++) {
			String f = fields[i];
			if (f.startsWith(base)) {
				f = f.substring(base.length());
				int fi;
				if (log.isTraceEnabled()) log.trace("   ATTR ["+f+"]");
				if ((fi = f.indexOf('.')) == -1) {
					Method m = getSetter(cl, f);
					if (obj != null) {
						if (log.isTraceEnabled()) log.trace("   SET ["+f+"] ["+data[i]+"]");
						String v = data[i];
						if (v == null) {
							m.invoke(obj, v);
						} else {
							m.invoke(obj, convert(v, m.getGenericParameterTypes()[0]));
						}
					} else if (f.startsWith("*")) {
						if (log.isTraceEnabled()) log.trace("   FIND "+cl.getName()+" ["+f+"]  ["+convert(data[i], m.getParameterTypes()[0])+"]");
						obj = find(cl, f.substring(1), convert(data[i], m.getParameterTypes()[0]));
					} else if (f.startsWith("+")) {
						obj = cl.newInstance();
					} else {
						throw new RuntimeException("NOT SUPPORTED "+cl.getName()+" ["+f+"]");
					}
				} else {
					Method m = getGetter(cl, f.substring(0, fi));
					Object o = m.invoke(obj);
					if (log.isTraceEnabled()) log.trace("   INIT ["+f.substring(0, fi)+"] "+U.dump(o));
					o = setData(m.getReturnType(), o, base+f.substring(0, fi+1), fields, data);
					if (log.isTraceEnabled()) log.trace("   INIT-OUT ["+f.substring(0, fi)+"] "+U.dump(o));
					m = getSetter(cl, f.substring(0, fi));
					m.invoke(obj, o);
				}
			}
		}
		return obj;
	}
}
