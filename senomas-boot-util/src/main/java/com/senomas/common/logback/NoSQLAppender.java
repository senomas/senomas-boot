package com.senomas.common.logback;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import ch.qos.logback.core.AppenderBase;

public class NoSQLAppender extends AppenderBase<LoggingEvent> {

	private LogWriter writer;

	private String uri;

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}
	
	@Override
	public void start() {
		super.start();
		try {
			String uri = this.uri;
			if (uri.startsWith("mongodb://")) {
				int ix = uri.lastIndexOf('/');
				if (ix <= 0)
					throw new RuntimeException("1 Invalid uri '" + uri + "'");
				int iy = uri.lastIndexOf('.');
				if (iy <= 0)
					throw new RuntimeException("2 Invalid uri '" + uri + "' " + ix);
				final MongoClient mongo = new MongoClient(new MongoClientURI(uri.substring(0, iy)));
				final DB db = mongo.getDB(uri.substring(ix+1, iy));
				final DBCollection collection = db.getCollection(uri.substring(iy+1));
				writer = new LogWriter() {
					@Override
					public void stop() {
						mongo.close();
					}
					
					@Override
					public void write(LoggingEvent e) {
						BasicDBObjectBuilder objectBuilder = BasicDBObjectBuilder.start().add("ts", new Date(e.getTimeStamp()))
								.add("msg", e.getFormattedMessage()).add("level", e.getLevel().toString())
								.add("logger", e.getLoggerName()).add("thread", e.getThreadName());
						if (e.hasCallerData()) {
							List<DBObject> caller = new LinkedList<>();
							int count = 0;
							for (StackTraceElement st : e.getCallerData()) {
								BasicDBObjectBuilder stBuilder = BasicDBObjectBuilder.start();
								stBuilder.add("file", st.getFileName());
								stBuilder.add("class", st.getClassName());
								stBuilder.add("method", st.getMethodName());
								stBuilder.add("line", st.getLineNumber());
								caller.add(stBuilder.get());
								if (count++ > 100) break;
							}
							objectBuilder.add("caller", caller);
						}
						StackTraceElementProxy[] sta = e.getThrowableProxy().getStackTraceElementProxyArray();
						if (sta != null && sta.length > 0) {
							List<DBObject> stacks = new LinkedList<>();
							int count = 0;
							for (StackTraceElementProxy stp : sta) {
								StackTraceElement st = stp.getStackTraceElement();
								BasicDBObjectBuilder stBuilder = BasicDBObjectBuilder.start();
								stBuilder.add("file", st.getFileName());
								stBuilder.add("class", st.getClassName());
								stBuilder.add("method", st.getMethodName());
								stBuilder.add("line", st.getLineNumber());
								stacks.add(stBuilder.get());
								if (count++ > 100) break;
							}
							objectBuilder.add("stacks", stacks);
						}

						@SuppressWarnings("deprecation")
						Map<String, String> mdc = e.getMdc();
						if (mdc != null && !mdc.isEmpty()) {
							objectBuilder.add("mdc", new BasicDBObject(mdc));
						}
						collection.insert(objectBuilder.get());
					}
				};
			} else {
				throw new RuntimeException("Invalid uri '" + uri + "'");
			}
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}
	
	@Override
	public void stop() {
		if (writer != null) writer.stop();
		super.stop();
	}

	@Override
	protected void append(LoggingEvent e) {
		writer.write(e);
	}
}
