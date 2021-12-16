package com.senomas.common.loggerfilter;

import java.io.IOException;
import java.security.Principal;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import com.senomas.common.SenomasConfiguration;

import net.logstash.logback.argument.StructuredArguments;

@Component
@EnableConfigurationProperties(SenomasConfiguration.class)
public class LoggerFilter implements Filter {
	private static final Logger LOG = LoggerFactory.getLogger(LoggerFilter.class);

	public static final int FLAG_OFF = 0;
	public static final int FLAG_BASIC = 1;
	public static final int FLAG_REQUEST = 8;
	public static final int FLAG_RESPONSE = 16;
	public static final int FLAG_REQUEST_PRETTY = 32;
	public static final int FLAG_RESPONSE_PRETTY = 64;
	public static final int FLAG_ALL = 0xFFFF;

	private static long counter = 1;

	FilterConfig cfg;
	List<Matcher> matchers = new LinkedList<Matcher>();

	@Autowired
	private SenomasConfiguration config;

	@Bean
	public FilterRegistrationBean loggerFilterRegistrationBean() {
		FilterRegistrationBean registrationBean = new FilterRegistrationBean();
		if (config != null && config.getHttpLogger() != null && config.getHttpLogger().getPath() != null) {
			LOG.debug("init LoggerFilter");
			for (Entry<String, Object> me : config.getHttpLogger().getPath().entrySet()) {
				Object ov = me.getValue();
				int flag = 0;
				long delay = 0;
				if (ov instanceof Boolean) {
					if ((Boolean) ov) {
						flag = FLAG_ALL;
					} else {
						flag = FLAG_OFF;
					}
				} else {
					String vs[] = ((String) ov).split(",");
					for (String v : vs) {
						v = v.toUpperCase().trim();
						if ("OFF".equals(v)) {
							flag = FLAG_OFF;
						} else if ("BASIC".equals(v)) {
							flag |= FLAG_BASIC;
						} else if ("REQUEST".equals(v)) {
							flag |= FLAG_REQUEST;
						} else if ("RESPONSE".equals(v)) {
							flag |= FLAG_RESPONSE;
						} else if ("REQUEST_PRETTY".equals(v)) {
							flag |= FLAG_REQUEST_PRETTY;
						} else if ("RESPONSE_PRETTY".equals(v)) {
							flag |= FLAG_RESPONSE_PRETTY;
						} else if ("ALL".equals(v)) {
							flag |= FLAG_ALL;
						} else if (v.startsWith("DELAY:")) {
							delay = Long.parseLong(v.substring(6));
						} else {
							throw new RuntimeException(
									"Not supported flag '" + v + "' in " + me.getKey() + " = " + me.getValue());
						}
					}
				}
				matchers.add(new Matcher(me.getKey(), flag, delay));
				if (LOG.isDebugEnabled())
					LOG.debug("Logging '" + me.getKey() + "' " + me.getValue() + "    "
							+ Integer.toHexString(flag).toUpperCase());
			}
			Collections.sort(matchers, new Comparator<Matcher>() {

				public int compare(Matcher o1, Matcher o2) {
					return Integer.compare(o2.path.length(), o1.path.length());
				}

			});
			registrationBean.setOrder(config.getHttpLogger().getOrder());
		}
		registrationBean.setFilter(this);
		return registrationBean;
	}

	public void init(FilterConfig filterConfig) throws ServletException {
		this.cfg = filterConfig;
	}

	public void destroy() {
	}

	public Matcher getMatcher(String path) {
		for (Matcher m : matchers) {
			if (path.startsWith(m.path)) {
				return m;
			}
		}
		return null;
	}

	protected void putMDC(String key, String value) {
		if (value != null) {
			MDC.put(key, value);
		}
	}

	public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
			throws IOException, ServletException {
		if (req instanceof HttpServletRequest) {
			HttpServletRequest hreq = (HttpServletRequest) req;
			HttpServletResponse hresp = (HttpServletResponse) resp;
			String addr = hreq.getHeader("X-Forwarded-Host");
			String port = hreq.getHeader("X-Forwarded-Port");
			Principal user = hreq.getUserPrincipal();
			if (addr == null || addr.trim().length() == 0) {
				addr = hreq.getRemoteAddr();
			}
			if (port == null || port.trim().length() == 0) {
				port = String.valueOf(hreq.getRemotePort());
			}
			String qstr = hreq.getQueryString();
			String url = hreq.getRequestURI() + (qstr != null ? '?' + qstr : "");
			final long reqid = counter++;
			Set<String> mdcs = new HashSet<>();
			mdcs.add("http_user");
			mdcs.add("http_clientAddr");
			mdcs.add("http_clientPort");
			mdcs.add("http_method");
			mdcs.add("http_reqid");
			mdcs.add("http_url");
			mdcs.add("http_queryString");
			mdcs.add("http_type");
			mdcs.add("http_userAgent");
			mdcs.add("http_referer");
			mdcs.add("http_contentLength");
			mdcs.add("http_status");

			try {
				putMDC("http_type", "request");
				putMDC("http_user", user != null ? user.getName() : null);
				putMDC("http_clientAddr", addr);
				putMDC("http_clientPort", port);
				putMDC("http_method", hreq.getMethod());
				putMDC("http_reqid", String.valueOf(reqid));
				putMDC("http_url", hreq.getRequestURL().toString());
				putMDC("http_queryString", hreq.getQueryString());

				Matcher m = getMatcher(url);
				int flag = m != null ? m.getFlag() : 0;
				long delay = m != null ? m.getDelay() : 0;
				if (flag > 0) {
					HttpServletRequest xreq = null;
					try {
						xreq = ((flag & FLAG_REQUEST) != 0) ? new RequestWrapper(hreq) : hreq;
						HttpServletResponse xres = ((flag & FLAG_RESPONSE) != 0 || (flag & FLAG_RESPONSE_PRETTY) != 0)
								? new ResponseWrapper(hresp) : hresp;

						Set<String> headerMdcs = new HashSet<>();
						try {
//							for (Enumeration<?> e = hreq.getHeaderNames(); e.hasMoreElements();) {
//								String str = (String) e.nextElement();
//								Enumeration<?> e2 = hreq.getHeaders(str);
//								if (e2.hasMoreElements()) {
//									Object v1 = e2.nextElement();
//									if (e2.hasMoreElements()) {
//										int idx = 0;
//										headerMdcs.add("http_header_" + str + "[" + (++idx) + "]");
//										putMDC("http_header_" + str + "[" + idx + "]",
//												v1 != null ? v1.toString() : null);
//										do {
//											v1 = e2.nextElement();
//											headerMdcs.add("http_header_" + str + "[" + (++idx) + "]");
//											putMDC("http_header_" + str + "[" + idx + "]",
//													v1 != null ? v1.toString() : null);
//										} while (e2.hasMoreElements());
//									} else {
//										headerMdcs.add("http_header_" + str);
//										putMDC("http_header_" + str, v1 != null ? v1.toString() : null);
//									}
//								}
//							}
							HttpSession session = hreq.getSession(false);
							if (session != null) {
								headerMdcs.add("http_sessionId");
								putMDC("http_sessionId", session.getId());
							}

							if (xreq instanceof RequestWrapper) {
								byte bb[] = ((RequestWrapper) xreq).toByteArray();
								if (bb.length > 0) {
									putMDC("http_contentLength", String.valueOf(bb.length));
									String ct = ((RequestWrapper) xreq).getContentType();
									if (ct != null && ct.startsWith("application/json")) {
										LOG.info("Request {}", StructuredArguments.raw("http_body", new String(bb, "UTF-8")));
									} else {
										LOG.info("Request");
									}
								} else {
									LOG.info("Request");
								}
							} else {
								LOG.info("Request");
							}
						} finally {
							MDC.remove("http_type");
							for (String mh : headerMdcs) {
								MDC.remove(mh);
							}
						}

						if (delay > 0) {
							try {
								LOG.info("Delay "+delay);
								Thread.sleep(delay);
							} catch (InterruptedException e1) {
							}
						}

						chain.doFilter(xreq, xres);

						putMDC("http_type", "response");
						if (xres instanceof ResponseWrapper) {
							putMDC("http_status", String.valueOf(xres.getStatus()));
							if ((flag & FLAG_RESPONSE) != 0 || (flag & FLAG_RESPONSE_PRETTY) != 0) {
//								for (String str : xres.getHeaderNames()) {
//									Iterator<String> itr = xres.getHeaders(str).iterator();
//									if (itr.hasNext()) {
//										String v1 = itr.next();
//										if (itr.hasNext()) {
//											int idx = 0;
//											mdcs.add("http_header_" + str + "[" + (++idx) + "]");
//											putMDC("http_header_" + str + "[" + idx + "]",
//													v1 != null ? v1.toString() : null);
//											do {
//												v1 = itr.next();
//												mdcs.add("http_header_" + str + "[" + (++idx) + "]");
//												putMDC("http_header_" + str + "[" + idx + "]", v1);
//											} while (itr.hasNext());
//										} else {
//											mdcs.add("http_header_" + str);
//											putMDC("http_header_" + str, v1);
//										}
//									}
//								}
								byte bb[] = ((ResponseWrapper) xres).toByteArray();
								if (bb.length > 0) {
									putMDC("http_contentLength", String.valueOf(bb.length));
									if ((flag & FLAG_RESPONSE_PRETTY) != 0) {
										String ct = ((ResponseWrapper) xres).getContentType();
										if (ct != null && ct.startsWith("application/json")) {
											LOG.info("Response {}", StructuredArguments.raw("http_body", new String(bb, "UTF-8")));
										} else {
											LOG.info("Response");
										}
									} else {
										LOG.info("Response");
									}
								} else {
									LOG.info("Response");
								}
							}
						}
					} catch (IOException e) {
						putMDC("http_type", "error");
						LOG.error("Error " + e.getMessage(), e);
						throw e;
					} catch (ServletException e) {
						putMDC("http_type", "error");
						LOG.error("Error " + e.getMessage(), e);
						throw e;
					}
				} else {
					chain.doFilter(req, resp);
				}
			} finally {
				for (String m : mdcs) {
					MDC.remove(m);
				}
			}
		} else {
			chain.doFilter(req, resp);
		}
	}

	static class Matcher {
		final String path;
		final int flag;
		final long delay;

		public Matcher(String path, int flag, long delay) {
			this.path = path;
			this.flag = flag;
			this.delay = delay;
		}

		public String getPath() {
			return path;
		}

		public int getFlag() {
			return flag;
		}

		public long getDelay() {
			return delay;
		}
	}
}
