/**
 * JBoss, Home of Professional Open Source. Copyright 2012, Red Hat, Inc., and
 * individual contributors as indicated by the @author tags. See the
 * copyright.txt file in the distribution for a full listing of individual
 * contributors.
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.jboss.cluster.proxy.logging;

import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.Map;

/**
 * {@code Logger}
 * 
 * Created on Jun 29, 2012 at 4:43:15 PM
 * 
 * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
 */
public class Logger {

	private static final Map<String, Logger> loggers = new Hashtable<>();
	private static final boolean debugEnabled = Boolean.valueOf(System.getProperty(
			"org.jboss.logging.debug.ENABLE", "false"));

	private String name;
	private SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss,SSS");

	/**
	 * Create a new instance of {@code Logger}
	 */
	private Logger(String name) {
		this.name = name;
	}

	/**
	 * 
	 * @param clazz
	 * @return a new {@code Logger} instance within the specified class name
	 */
	public static Logger getLogger(Class<?> clazz) {
		if (loggers.get(clazz.getName()) != null) {
			return loggers.get(clazz.getName());
		}

		Logger logger = new Logger(clazz.getName());
		loggers.put(logger.name, logger);
		return logger;
	}

	/**
	 * @param name
	 *            the name of the logger
	 * @return a new {@code Logger} instance within the specified name
	 */
	public static Logger getLogger(String name) {
		if (name == null) {
			throw new NullPointerException("The logger name is null");
		}

		if (loggers.get(name) != null) {
			return loggers.get(name);
		}

		try {
			Class<?> clazz = Class.forName(name);
			return getLogger(clazz);
		} catch (Throwable t) {
			throw new LoggerException("Invalid logger name", t);
		}
	}

	/**
	 * 
	 * @param message
	 */
	public void info(String message) {
		doLog(Level.INFO, message, null);
	}

	/**
	 * 
	 * @param message
	 */
	public void error(String message) {
		error(message, null);
	}

	/**
	 * 
	 * @param message
	 * @param t
	 */
	public void error(String message, Throwable t) {
		doLog(Level.ERROR, message, t);
	}

	/**
	 * 
	 * @param message
	 */
	public void fatal(String message) {
		fatal(message, null);
	}

	/**
	 * 
	 * @param message
	 * @param t
	 */
	public void fatal(String message, Throwable t) {
		doLog(Level.FATAL, message, t);
	}

	/**
	 * 
	 * @param message
	 */
	public void warn(String message) {
		warn(message, null);
	}

	/**
	 * 
	 * @param message
	 * @param t
	 */
	public void warn(String message, Throwable t) {
		doLog(Level.WARNING, message, null);
	}

	/**
	 * 
	 * @param message
	 */
	public void debug(String message) {
		debug(message, null);
	}

	/**
	 * If <tt>DEBUG</tt> is enabled, Print a debug log message else nothing
	 * happens
	 * 
	 * @param message
	 *            the message to print out
	 * @param t
	 */
	public void debug(String message, Throwable t) {
		if (!isDebugEnabled()) {
			return;
		}
		doLog(Level.DEBUG, message, t);
	}

	/**
	 * @param level
	 *            the log level
	 * @return the string representation of the current time, the specified
	 *         level and the name of the logger.
	 */
	private String getInitial(Level level) {
		return getDate() + level + "[" + this.name + "]  ";
	}

	/**
	 * @return the current with the specified format
	 */
	private String getDate() {
		return format.format(new Date(System.currentTimeMillis()));
	}

	/**
	 * @return <tt>true</tt> if the debug mode is enabled else <tt>false</tt>
	 */
	public boolean isDebugEnabled() {
		return debugEnabled;
	}

	/**
	 * 
	 * @param level
	 *            the log level
	 * @param message
	 *            the log message
	 * @param t
	 *            the error (if any)
	 */
	private synchronized void doLog(Level level, String message, Throwable t) {
		String msg = message;
		if (msg != null) {
			msg = msg.replaceAll("\n", "\n" + level);
		}

		PrintStream ps = (level == Level.ERROR || level == Level.FATAL) ? System.err : System.out;
		String intial = getInitial(level);
		ps.println(intial + msg);
		if (t != null) {
			intial += "\tat ";
			for (StackTraceElement ste : t.getStackTrace()) {
				ps.println(intial + ste);
			}
		}
	}

	/**
	 * {@code Level}
	 * 
	 * Created on Jun 29, 2012 at 6:49:05 PM
	 * 
	 * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
	 */
	private static enum Level {

		/**
		 * For info message
		 */
		INFO,
		/**
		 * For error messages
		 */
		ERROR,
		/**
		 * For fatal error messages
		 */
		FATAL,
		/**
		 * For warning messages
		 */
		WARNING,
		/**
		 * For debug messages
		 */
		DEBUG;

		private String name;

		/**
		 * Create a new instance of {@code Level}
		 */
		private Level() {
			this.name = ' ' + super.toString() + "  ";
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Enum#toString()
		 */
		@Override
		public String toString() {
			return this.name;
		}
	}

}
