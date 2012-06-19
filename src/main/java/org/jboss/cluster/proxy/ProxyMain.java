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
package org.jboss.cluster.proxy;

import java.io.InputStream;
import java.net.InetSocketAddress;

import org.jboss.logging.Logger;

/**
 * {@code ProxyMain}
 * 
 * Created on Jun 18, 2012 at 4:18:59 PM
 * 
 * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
 */
public class ProxyMain {

	private static final Logger logger = Logger.getLogger(ProxyMain.class.getPackage().getName());
	private static final String DEFAULT_PROTOCOL = "org.apache.coyote.http11.Http11NioProtocol";
	private static final String DEFAULT_SCHEME = "http";
	private static WebConnectorService service;

	/**
	 * Create a new instance of {@code ProxyMain}
	 */
	public ProxyMain() {
		super();
	}

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		String java_home = System.getProperty("java.home");
		java_home = java_home.substring(0, java_home.length() - 3);

		System.out
				.println("\n=========================================================================\n");
		System.out.println("  JBoss Mod Cluster Proxy Bootstrap Environment\n");
		System.out.println("  JAVA_HOME: " + java_home + "\n");
		System.out.println("  JBOSS_CLUSER_HOME: " + System.getProperty("user.dir") + "\n");
		System.out
				.println("=========================================================================\n\n");

		// Loading configuration first
		try (InputStream in = ProxyMain.class.getResourceAsStream("config.properties")) {
			logger.info("Loading configuration");
			System.getProperties().load(in);
		} catch (Throwable t) {
			logger.error("unable to load configurations");
			System.exit(-1);
		}

		try {
			String protocol = System.getProperty("http-protocol", DEFAULT_PROTOCOL);
			String scheme = System.getProperty("scheme", DEFAULT_SCHEME);
			// Creating the web connector service
			service = new WebConnectorService(protocol, scheme);
			// configure the web connector service

			int maxConnections = Integer.valueOf(System.getProperty(Constants.MAX_THREAD, ""
					+ Constants.DEFAULT_MAX_CONNECTIONS));
			service.setMaxConnections(maxConnections);

			InetSocketAddress address = null;
			int port = Integer.valueOf(System.getProperty("org.apache.tomcat.util.PORT", "8080"));
			System.out.println("PORT = " + port);
			String hostname = System.getProperty("org.apache.tomcat.util.ADDRESS","localhost");
			address = (hostname == null) ? new InetSocketAddress(port) : new InetSocketAddress(
					hostname, port);

			System.out.println(address);
			
			service.setAddress(address);
			
			// Starting the web connector service
			service.start();
		} catch (Exception e) {
			logger.error("creating protocol handler error");
			System.exit(-1);
		}

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				try {
					service.stop();
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}
		});
	}
}
