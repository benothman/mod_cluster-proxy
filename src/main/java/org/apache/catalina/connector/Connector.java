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
package org.apache.catalina.connector;

import java.lang.reflect.Method;
import java.util.Set;

import org.apache.coyote.Adapter;
import org.apache.coyote.ProtocolHandler;
import org.apache.tomcat.util.IntrospectionUtils;
import org.apache.tomcat.util.res.StringManager;
import org.jboss.cluster.proxy.container.MCMPaddapter;
import org.jboss.logging.Logger;

/**
 * {@code Connector}
 * 
 * Created on Jun 19, 2012 at 11:59:51 AM
 * 
 * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
 */
public final class Connector {

	private static Logger log = Logger.getLogger(Connector.class);
	private ProtocolHandler protocolHandler;
	private String protocol;
	/**
	 * Has this component been initialized yet?
	 */
	private boolean initialized = false;

	/**
	 * Has this component been started yet?
	 */
	private boolean started = false;

	/**
	 * The shutdown signal to our background thread
	 */
	private boolean stopped = false;

	/**
	 * The request scheme that will be set on all requests received through this
	 * connector.
	 */
	private String scheme = "http";

	/**
	 * The secure connection flag that will be set on all requests received
	 * through this connector.
	 */
	private boolean secure = false;

	/**
	 * The port number on which we listen for requests.
	 */
	private int port = 0;

	/**
	 * The server name to which we should pretend requests to this Connector
	 * were directed. This is useful when operating Tomcat behind a proxy
	 * server, so that redirects get constructed accurately. If not specified,
	 * the server name included in the <code>Host</code> header is used.
	 */
	private String proxyName = null;

	/**
	 * The server port to which we should pretent requests to this Connector
	 * were directed. This is useful when operating Tomcat behind a proxy
	 * server, so that redirects get constructed accurately. If not specified,
	 * the port number specified by the <code>port</code> property is used.
	 */
	private int proxyPort = 0;

	/**
	 * The redirect port for non-SSL to SSL redirects.
	 */
	private int redirectPort = 443;

	/**
	 * Maximum size of a POST which will be saved by the container during
	 * authentication. 4kB by default
	 */
	private int maxSavePostSize = 4 * 1024;

	/**
	 * 
	 */
	private String domain;

	/**
	 * Coyote adapter.
	 */
	protected Adapter adapter = null;

	/**
	 * The "enable DNS lookups" flag for this Connector.
	 */
	private boolean enableLookups = false;

	/**
	 * Allowed virtual hosts.
	 */
	private Set<String> allowedHosts = null;

	/**
	 * Descriptive information about this Connector implementation.
	 */
	protected static final String info = "org.apache.catalina.connector.Connector/2.1";

	/**
	 * The string manager for this package.
	 */
	private StringManager sm = StringManager.getManager(Constants.Package);

	/**
	 * Coyote Protocol handler class name. Defaults to the Coyote HTTP/1.1
	 * protocolHandler.
	 */
	private String protocolHandlerClassName = "org.apache.coyote.http11.Http11NioProtocol";

	protected static final boolean USE_BODY_ENCODING_FOR_QUERY_STRING = Boolean
			.valueOf(
					System.getProperty(
							"org.apache.catalina.connector.USE_BODY_ENCODING_FOR_QUERY_STRING",
							"false")).booleanValue();

	/**
	 * URI encoding as body.
	 */
	protected boolean useBodyEncodingForURI = USE_BODY_ENCODING_FOR_QUERY_STRING;

	/**
	 * 
	 */
	private int maxPostSize = 2 * 1024 * 1024;

	/**
	 * Create a new instance of {@code Connector}
	 * 
	 * @param protocol
	 */
	public Connector(String protocol) {
		setProtocolHandlerClassName(protocol);
		// Instantiate protocol handler
		try {
			Class<?> clazz = Class.forName(protocolHandlerClassName);
			this.protocolHandler = (ProtocolHandler) clazz.newInstance();
		} catch (Exception e) {
			throw new IllegalArgumentException(sm.getString(
					"coyoteConnector.protocolHandlerInstantiationFailed", e));
		}
	}

	/**
	 * 
	 * @throws Exception
	 */
	public void init() throws Exception {
		if (initialized) {
			log.info(sm.getString("coyoteConnector.alreadyInitialized"));
			return;
		}

		this.initialized = true;

		if (this.protocolHandler.getClass().equals(
				org.jboss.cluster.proxy.http11.Http11NioProtocol.class)) {
			this.adapter = new MCMPaddapter(this);
		}

		// Initializing adapter
		if (this.adapter == null) {
			// By default we use the CoyoteAdapter
			adapter = new CoyoteAdapter(this);
			((CoyoteAdapter) adapter).init();
		}

		protocolHandler.setAdapter(adapter);
		IntrospectionUtils.setProperty(protocolHandler, "jkHome",
				System.getProperty("catalina.base"));

		try {
			log.info("Invoke Protocol Handler initialization");
			protocolHandler.init();
		} catch (Exception e) {
			throw new Exception(sm.getString(
					"coyoteConnector.protocolHandlerInitializationFailed", e));
		}
	}

	/**
	 * @throws Exception
	 */
	public void start() throws Exception {
		if (!initialized) {
			init();
		}

		// Validate and update our current state
		if (started) {
			log.info(sm.getString("coyoteConnector.alreadyStarted"));
			return;
		}
		started = true;

		try {
			protocolHandler.start();
		} catch (Exception e) {
			throw new Exception(sm.getString(
					"coyoteConnector.protocolHandlerStartFailed", e));
		}
	}

	/**
	 * Pause the connector.
	 * 
	 * @throws Exception
	 */
	public void pause() throws Exception {
		try {
			protocolHandler.pause();
		} catch (Exception e) {
			log.error(
					sm.getString("coyoteConnector.protocolHandlerPauseFailed"),
					e);
		}
	}

	/**
	 * Pause the connector.
	 * 
	 * @throws Exception
	 */
	public void resume() throws Exception {
		try {
			protocolHandler.resume();
		} catch (Exception e) {
			log.error(
					sm.getString("coyoteConnector.protocolHandlerResumeFailed"),
					e);
		}
	}

	/**
	 * Terminate processing requests via this Connector.
	 * 
	 * @exception Exception
	 *                if a fatal shutdown error occurs
	 */
	public void stop() throws Exception {
		log.info("Stopping Connector");
		// Validate and update our current state
		if (!started) {
			log.error(sm.getString("coyoteConnector.notStarted"));
			return;

		}
		started = false;
		try {
			protocolHandler.destroy();
		} catch (Exception e) {
			throw new Exception(sm.getString(
					"coyoteConnector.protocolHandlerDestroyFailed", e));
		}
	}

	/**
	 * Getter for protocolHandler
	 * 
	 * @return the protocolHandler
	 */
	public ProtocolHandler getProtocolHandler() {
		return this.protocolHandler;
	}

	/**
	 * Setter for the protocolHandler
	 * 
	 * @param protocolHandler
	 *            the protocolHandler to set
	 */
	public void setProtocolHandler(ProtocolHandler protocolHandler) {
		this.protocolHandler = protocolHandler;
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
		if ("HTTP/1.1".equals(protocol) || "http".equals(protocol)) {
			setProtocolHandlerClassName("org.apache.coyote.http11.Http11Protocol");
		} else if ("AJP/1.3".equals(protocol) || "ajp".equals(protocol)) {
			setProtocolHandlerClassName("org.apache.coyote.ajp.AjpProtocol");
		} else if (protocol != null) {
			setProtocolHandlerClassName(protocol);
		}

	}

	/**
	 * Set the class name of the Coyote protocol handler which will be used by
	 * the connector.
	 * 
	 * @param protocolHandlerClassName
	 *            The new class name
	 */
	public void setProtocolHandlerClassName(String protocolHandlerClassName) {
		this.protocolHandlerClassName = protocolHandlerClassName;
	}

	/**
	 * Getter for initialized
	 * 
	 * @return the initialized
	 */
	public boolean isInitialized() {
		return this.initialized;
	}

	/**
	 * Setter for the initialized
	 * 
	 * @param initialized
	 *            the initialized to set
	 */
	public void setInitialized(boolean initialized) {
		this.initialized = initialized;
	}

	/**
	 * Getter for started
	 * 
	 * @return the started
	 */
	public boolean isStarted() {
		return this.started;
	}

	/**
	 * Setter for the started
	 * 
	 * @param started
	 *            the started to set
	 */
	public void setStarted(boolean started) {
		this.started = started;
	}

	/**
	 * Getter for stopped
	 * 
	 * @return the stopped
	 */
	public boolean isStopped() {
		return this.stopped;
	}

	/**
	 * Setter for the stopped
	 * 
	 * @param stopped
	 *            the stopped to set
	 */
	public void setStopped(boolean stopped) {
		this.stopped = stopped;
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
	 * Getter for secure
	 * 
	 * @return the secure
	 */
	public boolean isSecure() {
		return this.secure;
	}

	/**
	 * Setter for the secure
	 * 
	 * @param secure
	 *            the secure to set
	 */
	public void setSecure(boolean secure) {
		this.secure = secure;
	}

	/**
	 * Getter for domain
	 * 
	 * @return the domain
	 */
	public String getDomain() {
		return this.domain;
	}

	/**
	 * Setter for the domain
	 * 
	 * @param domain
	 *            the domain to set
	 */
	public void setDomain(String domain) {
		this.domain = domain;
	}

	/**
	 * Getter for protocolHandlerClassName
	 * 
	 * @return the protocolHandlerClassName
	 */
	public String getProtocolHandlerClassName() {
		return this.protocolHandlerClassName;
	}

	/**
	 * Getter for port
	 * 
	 * @return the port
	 */
	public int getPort() {
		return this.port;
	}

	/**
	 * Setter for the port
	 * 
	 * @param port
	 *            the port to set
	 */
	public void setPort(int port) {
		this.port = port;
		try {
			Method m = this.protocolHandler.getClass().getMethod("setPort",
					Integer.TYPE);
			m.invoke(this.protocolHandler, port);
		} catch (Exception exp) {
			exp.printStackTrace();
		}

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
	public int getProxyPort() {
		return this.proxyPort;
	}

	/**
	 * Setter for the proxyPort
	 * 
	 * @param proxyPort
	 *            the proxyPort to set
	 */
	public void setProxyPort(int proxyPort) {
		this.proxyPort = proxyPort;
	}

	/**
	 * Getter for redirectPort
	 * 
	 * @return the redirectPort
	 */
	public int getRedirectPort() {
		return this.redirectPort;
	}

	/**
	 * Setter for the redirectPort
	 * 
	 * @param redirectPort
	 *            the redirectPort to set
	 */
	public void setRedirectPort(int redirectPort) {
		this.redirectPort = redirectPort;
	}

	/**
	 * Setter for the maxPostSize
	 * 
	 * @param maxPostSize
	 *            the maxPostSize to set
	 */
	public void setMaxPostSize(int maxPostSize) {
		this.maxPostSize = maxPostSize;
		try {
			Method m = this.protocolHandler.getClass().getMethod(
					"setMaxPostSize", Integer.class);
			m.invoke(this.protocolHandler, maxPostSize);
		} catch (Throwable t) {
			log.warn("The protocol handler does not support max post size", t);
		}
	}

	/**
	 * @return
	 */
	public int getMaxPostSize() {
		return this.maxPostSize;
	}

	/**
	 * Getter for maxSavePostSize
	 * 
	 * @return the maxSavePostSize
	 */
	public int getMaxSavePostSize() {
		return this.maxSavePostSize;
	}

	/**
	 * Setter for the maxSavePostSize
	 * 
	 * @param maxSavePostSize
	 *            the maxSavePostSize to set
	 */
	public void setMaxSavePostSize(int maxSavePostSize) {
		this.maxSavePostSize = maxSavePostSize;
	}

	/**
	 * Getter for enableLookups
	 * 
	 * @return the enableLookups
	 */
	public boolean isEnableLookups() {
		return this.enableLookups;
	}

	/**
	 * Setter for the enableLookups
	 * 
	 * @param enableLookups
	 *            the enableLookups to set
	 */
	public void setEnableLookups(boolean enableLookups) {
		this.enableLookups = enableLookups;
	}

	/**
	 * Getter for allowedHosts
	 * 
	 * @return the allowedHosts
	 */
	public Set<String> getAllowedHosts() {
		return this.allowedHosts;
	}

	/**
	 * Setter for the allowedHosts
	 * 
	 * @param allowedHosts
	 *            the allowedHosts to set
	 */
	public void setAllowedHosts(Set<String> allowedHosts) {
		this.allowedHosts = allowedHosts;
	}

	/**
	 * Getter for the adapter
	 * 
	 * @return
	 */
	public Adapter getAdapter() {
		return this.adapter;
	}

	/**
	 * Setter for the Adapter
	 * 
	 * @param adapter
	 */
	public void setAdapter(Adapter adapter) {
		this.adapter = adapter;
	}

	/**
	 * @return the useBodyEncodingForURI
	 */
	public boolean getUseBodyEncodingForURI() {
		return useBodyEncodingForURI;
	}

	/**
	 * @param useBodyEncodingForURI
	 *            the useBodyEncodingForURI to set
	 */
	public void setUseBodyEncodingForURI(boolean useBodyEncodingForURI) {
		this.useBodyEncodingForURI = useBodyEncodingForURI;
	}
}
