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

import java.util.logging.ConsoleHandler;
import java.util.logging.Logger;

import org.jboss.logmanager.formatters.PatternFormatter;

/**
 * {@code Logger}
 * 
 * Created on Jul 10, 2012 at 11:41:54 AM
 * 
 * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
 */
public class LogFactory {

	private static final String DEFAULT_PATTERN = "%d{HH:mm:ss,SSS} %-5p [%c] %m%n";

	/**
	 * Create a new instance of {@code Logger}
	 */
	public LogFactory() {
		super();
	}

	/**
	 * @param name
	 *            the logger name
	 * @return a new instance of {@link Logger}
	 */
	public static Logger getLogger(String name) {
		Logger logger = Logger.getLogger(name);

		String pattern = System.getProperty("formatter.PATTERN.pattern", DEFAULT_PATTERN);

		PatternFormatter formatter = new PatternFormatter(pattern);
		ConsoleHandler handler = new ConsoleHandler();
		handler.setFormatter(formatter);
		logger.addHandler(handler);
		logger.setUseParentHandlers(false);

		return logger;
	}

	/**
	 * @param clazz
	 *            the class of the owner of the logger
	 * @return a new instance of {@link Logger}
	 */
	public static Logger getLogger(Class<?> clazz) {
		return getLogger(clazz.getName());
	}

}
