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

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.Executor;

import org.apache.catalina.connector.Connector;
import org.jboss.cluster.proxy.container.MCMNodeService;
import org.jboss.logging.Logger;

/**
 * {@code WebConnectorService}
 * 
 * Created on Jun 19, 2012 at 3:05:13 PM
 * 
 * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
 */
public class WebConnectorService {

	private static final Logger logger = Logger.getLogger(WebConnectorService.class);
	private volatile String protocol = "HTTP/1.1";
	private volatile String scheme = "http";
	private Boolean enableLookups = null;
	private String proxyName = null;
	private Integer proxyPort = null;
	private Integer redirectPort = null;
	private Boolean secure = null;
	private Integer maxPostSize = null;
	private Integer maxSavePostSize = null;
	private Integer maxConnections = null;
	private Executor executor;
	private InetSocketAddress address;
	private Connector connector;
	private NodeService service = null;

	/**
	 * Create a new instance of {@code WebConnectorService}
	 * 
	 * @param protocol
	 * @param scheme
	 * @param object 
	 */
	public WebConnectorService(String protocol, String scheme, NodeService  service) {
		if (protocol != null) {
			this.protocol = protocol;
		}
		if (scheme != null) {
			this.scheme = scheme;
		}
		
		if (service != null)
			this.service  = service;
	}

	/**
	 * Starts the connector
	 * 
	 * @throws StartException
	 *             in case of any problem
	 */
	public synchronized void start() throws StartException {
		logger.info("Starting Web Connector Service");
		try {
			// Create connector
			Connector connector = new Connector(protocol);
			connector.setNodeService(service);
	
			connector.setConnectionManager(ProxyMain.CONNECTION_MANAGER);
			connector.setPort(address.getPort());
			connector.setScheme(scheme);
			if("https".equalsIgnoreCase(getScheme())) {
				connector.setSecure(true);
			}
			
			if (enableLookups != null)
				connector.setEnableLookups(enableLookups);
			if (getMaxPostSize() != null)
				connector.setMaxPostSize(maxPostSize);
			if (maxSavePostSize != null)
				connector.setMaxSavePostSize(maxSavePostSize);
			if (proxyName != null)
				connector.setProxyName(proxyName);
			if (proxyPort != null)
				connector.setProxyPort(proxyPort);
			if (redirectPort != null)
				connector.setRedirectPort(redirectPort);
			if (secure != null)
				connector.setSecure(secure);

			if (executor != null) {
				Method m = connector.getProtocolHandler().getClass()
						.getMethod("setExecutor", Executor.class);
				m.invoke(connector.getProtocolHandler(), executor);
			}
			if (address != null && address.getAddress() != null) {
				Method m = connector.getProtocolHandler().getClass()
						.getMethod("setAddress", InetAddress.class);
				m.invoke(connector.getProtocolHandler(), address.getAddress());
			}
			if (maxConnections != null) {
				try {
					Method m = connector.getProtocolHandler().getClass()
							.getMethod("setPollerSize", Integer.TYPE);
					m.invoke(connector.getProtocolHandler(), maxConnections);
				} catch (NoSuchMethodException e) {
					// Not all connectors will have this
				}

				Method m = connector.getProtocolHandler().getClass()
						.getMethod("setMaxThreads", Integer.TYPE);
				m.invoke(connector.getProtocolHandler(), maxConnections);
			}

			// Initialize the connector
			connector.init();
			// Start the connector
			connector.start();
			this.connector = connector;
			logger.info("Web Connector Service started successfully");
		} catch (Throwable e) {
			throw new StartException("Error starting web connector service", e);
		}
	}

	/**
	 * Stop the web connector service
	 */
	public synchronized void stop() {
		logger.info("Stopping Web Connector Service");
		final Connector connector = this.connector;
		try {
			connector.pause();
		} catch (Exception e) {
			// NOPE
		}
		try {
			connector.stop();
		} catch (Exception e) {
			// NOPE
		}
		this.connector = null;
		logger.info("Web Connector Service stopped successfully");
	}

	/**
	 * Getter for protocol
	 * 
	 * @return the protocol
	 */
	public String getProtocol() {
		return this.protocol;
	}

	/**
	 * Setter for the protocol
	 * 
	 * @param protocol
	 *            the protocol to set
	 */
	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	/**
	 * Getter for scheme
	 * 
	 * @return the scheme
	 */
	public String getScheme() {
		return this.scheme;
	}

	/**
	 * Setter for the scheme
	 * 
	 * @param scheme
	 *            the scheme to set
	 */
	public void setScheme(String scheme) {
		this.scheme = scheme;
	}

	/**
	 * Getter for enableLookups
	 * 
	 * @return the enableLookups
	 */
	public Boolean getEnableLookups() {
		return this.enableLookups;
	}

	/**
	 * Setter for the enableLookups
	 * 
	 * @param enableLookups
	 *            the enableLookups to set
	 */
	public void setEnableLookups(Boolean enableLookups) {
		this.enableLookups = enableLookups;
	}

	/**
	 * Getter for proxyName
	 * 
	 * @return the proxyName
	 */
	public String getProxyName() {
		return this.proxyName;
	}

	/**
	 * Setter for the proxyName
	 * 
	 * @param proxyName
	 *            the proxyName to set
	 */
	public void setProxyName(String proxyName) {
		this.proxyName = proxyName;
	}

	/**
	 * Getter for proxyPort
	 * 
	 * @return the proxyPort
	 */
	public Integer getProxyPort() {
		return this.proxyPort;
	}

	/**
	 * Setter for the proxyPort
	 * 
	 * @param proxyPort
	 *            the proxyPort to set
	 */
	public void setProxyPort(Integer proxyPort) {
		this.proxyPort = proxyPort;
	}

	/**
	 * Getter for redirectPort
	 * 
	 * @return the redirectPort
	 */
	public Integer getRedirectPort() {
		return this.redirectPort;
	}

	/**
	 * Setter for the redirectPort
	 * 
	 * @param redirectPort
	 *            the redirectPort to set
	 */
	public void setRedirectPort(Integer redirectPort) {
		this.redirectPort = redirectPort;
	}

	/**
	 * Getter for secure
	 * 
	 * @return the secure
	 */
	public Boolean getSecure() {
		return this.secure;
	}

	/**
	 * Setter for the secure
	 * 
	 * @param secure
	 *            the secure to set
	 */
	public void setSecure(Boolean secure) {
		this.secure = secure;
	}

	/**
	 * Getter for maxPostSize
	 * 
	 * @return the maxPostSize
	 */
	public Integer getMaxPostSize() {
		if (this.maxPostSize == null) {
			String str = System.getProperty("org.apache.coyote.http11.MAX_POST_SIZE");
			if (str != null) {
				this.maxPostSize = Integer.valueOf(str);
			}
		}

		return this.maxPostSize;
	}

	/**
	 * Setter for the maxPostSize
	 * 
	 * @param maxPostSize
	 *            the maxPostSize to set
	 */
	public void setMaxPostSize(Integer maxPostSize) {
		this.maxPostSize = maxPostSize;
	}

	/**
	 * Getter for maxSavePostSize
	 * 
	 * @return the maxSavePostSize
	 */
	public Integer getMaxSavePostSize() {
		return this.maxSavePostSize;
	}

	/**
	 * Setter for the maxSavePostSize
	 * 
	 * @param maxSavePostSize
	 *            the maxSavePostSize to set
	 */
	public void setMaxSavePostSize(Integer maxSavePostSize) {
		this.maxSavePostSize = maxSavePostSize;
	}

	/**
	 * Getter for maxConnections
	 * 
	 * @return the maxConnections
	 */
	public Integer getMaxConnections() {
		return this.maxConnections;
	}

	/**
	 * Setter for the maxConnections
	 * 
	 * @param maxConnections
	 *            the maxConnections to set
	 */
	public void setMaxConnections(Integer maxConnections) {
		this.maxConnections = maxConnections;
	}

	/**
	 * Getter for connector
	 * 
	 * @return the connector
	 */
	public Connector getConnector() {
		return this.connector;
	}

	/**
	 * Setter for the connector
	 * 
	 * @param connector
	 *            the connector to set
	 */
	public void setConnector(Connector connector) {
		this.connector = connector;
	}

	/**
	 * Getter for executor
	 * 
	 * @return the executor
	 */
	public Executor getExecutor() {
		return this.executor;
	}

	/**
	 * Setter for the executor
	 * 
	 * @param executor
	 *            the executor to set
	 */
	public void setExecutor(Executor executor) {
		this.executor = executor;
	}

	/**
	 * Getter for address
	 * 
	 * @return the address
	 */
	public InetSocketAddress getAddress() {
		return this.address;
	}

	/**
	 * Setter for the address
	 * 
	 * @param address
	 *            the address to set
	 */
	public void setAddress(InetSocketAddress address) {
		this.address = address;
	}

}
