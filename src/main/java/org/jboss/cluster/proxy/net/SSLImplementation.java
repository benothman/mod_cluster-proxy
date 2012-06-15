/**
 * JBoss, Home of Professional Open Source. Copyright 2011, Red Hat, Inc., and
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
package org.jboss.cluster.proxy.net;

import javax.net.ssl.SSLSession;

import org.jboss.cluster.proxy.net.jsse.NioJSSESocketChannelFactory;

/**
 * {@code SSLImplementation}
 * <p>
 * Abstract factory and base class for all SSL implementations.
 * </p>
 * 
 * 
 * Created on Feb 22, 2012 at 12:55:17 PM
 * 
 * @author EKR & <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
 * @param <E>
 */
public abstract class SSLImplementation<E> {

	private static org.jboss.logging.Logger logger = org.jboss.logging.Logger
			.getLogger(SSLImplementation.class);
	// The default implementations in our search path
	private static final String JSSEImplementationClass = "org.jboss.cluster.proxy.net.jsse.NioJSSEImplementation";
	private static final String[] implementations = { JSSEImplementationClass,
			"org.jboss.cluster.proxy.net.jsse.JSSEImplementation" };

	/**
	 * @return the default implementation of {@code SSLImplementation}
	 * @throws ClassNotFoundException
	 */
	public static SSLImplementation<?> getInstance() throws ClassNotFoundException {
		for (int i = 0; i < implementations.length; i++) {
			try {
				SSLImplementation<?> impl = getInstance(implementations[i]);
				return impl;
			} catch (Exception e) {
				if (logger.isTraceEnabled()) {
					logger.trace("Error creating " + implementations[i], e);
				}
			}
		}

		// If we can't instantiate any of these
		throw new ClassNotFoundException("Can't find any SSL implementation");
	}

	/**
	 * Returns the {@code SSLImplementation} specified by the name of it's class
	 * 
	 * @param className
	 * @return a new instance of the {@code SSLImplementation} given by it's
	 *         name
	 * @throws ClassNotFoundException
	 */
	public static SSLImplementation<?> getInstance(String className) throws ClassNotFoundException {
		if (className == null) {
			return getInstance();
		}

		try {
			// Workaround for the J2SE 1.4.x classloading problem (under
			// Solaris).
			// Class.forName(..) fails without creating class using new.
			// This is an ugly workaround.
			if (JSSEImplementationClass.equals(className)) {
				return new org.jboss.cluster.proxy.net.jsse.NioJSSEImplementation();
			}
			Class<?> clazz = Class.forName(className);
			return (SSLImplementation<?>) clazz.newInstance();
		} catch (Exception e) {
			if (logger.isDebugEnabled()) {
				logger.debug("Error loading SSL Implementation " + className, e);
			}
			throw new ClassNotFoundException("Error loading SSL Implementation " + className + " :"
					+ e.toString());
		}
	}

	/**
	 * @return the implementation name
	 */
	abstract public String getImplementationName();

	/**
	 * 
	 * @return a new instance of {@link NioJSSESocketChannelFactory}
	 */
	public abstract NioJSSESocketChannelFactory getServerSocketChannelFactory();

	/**
	 * Return a {@link SSLSupport} attached to the socket
	 * 
	 * @param sock
	 * @return a {@link SSLSupport} attached to the socket
	 */
	abstract public SSLSupport<E> getSSLSupport(E sock);

	/**
	 * @param session
	 * @return the {@link SSLSupport} attached to this session
	 */
	abstract public SSLSupport<?> getSSLSupport(SSLSession session);
}
