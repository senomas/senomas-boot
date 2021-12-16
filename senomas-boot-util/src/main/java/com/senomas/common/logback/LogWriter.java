package com.senomas.common.logback;

import ch.qos.logback.classic.spi.LoggingEvent;

public interface LogWriter {
	
	void stop();

	void write(LoggingEvent e);

}
