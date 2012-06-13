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
package org.jboss.modcluster.proxy.net.jsse;

import java.net.Socket;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.X509ExtendedKeyManager;
import javax.net.ssl.X509KeyManager;

/**
 * X509KeyManager which allows selection of a specific keypair and certificate
 * chain (identified by their keystore alias name) to be used by the server to
 * authenticate itself to SSL clients.
 * 
 * @author Jan Luehe
 */
public final class JSSEKeyManager extends X509ExtendedKeyManager {

	private X509KeyManager delegate;
	private String serverKeyAlias;

	/**
	 * Constructor.
	 * 
	 * @param mgr
	 *            The X509KeyManager used as a delegate
	 * @param serverKeyAlias
	 *            The alias name of the server's keypair and supporting
	 *            certificate chain
	 */
	public JSSEKeyManager(X509KeyManager mgr, String serverKeyAlias) {
		super();
		this.delegate = mgr;
		this.serverKeyAlias = serverKeyAlias;
	}

	/**
	 * Choose an alias to authenticate the client side of a secure socket, given
	 * the public key type and the list of certificate issuer authorities
	 * recognized by the peer (if any).
	 * 
	 * @param keyType
	 *            The key algorithm type name(s), ordered with the
	 *            most-preferred key type first
	 * @param issuers
	 *            The list of acceptable CA issuer subject names, or null if it
	 *            does not matter which issuers are used
	 * @param socket
	 *            The socket to be used for this connection. This parameter can
	 *            be null, in which case this method will return the most
	 *            generic alias to use
	 * 
	 * @return The alias name for the desired key, or null if there are no
	 *         matches
	 */
	@Override
	public String chooseClientAlias(String[] keyType, Principal[] issuers, Socket socket) {
		return delegate.chooseClientAlias(keyType, issuers, socket);
	}

	/**
	 * Returns this key manager's server key alias that was provided in the
	 * constructor.
	 * 
	 * @param keyType
	 *            Ignored
	 * @param issuers
	 *            Ignored
	 * @param socket
	 *            Ignored
	 * 
	 * @return Alias name for the desired key
	 */
	@Override
	public String chooseServerAlias(String keyType, Principal[] issuers, Socket socket) {
		return serverKeyAlias;
	}

	/**
	 * Returns the certificate chain associated with the given alias.
	 * 
	 * @param alias
	 *            The alias name
	 * 
	 * @return Certificate chain (ordered with the user's certificate first and
	 *         the root certificate authority last), or null if the alias can't
	 *         be found
	 */
	@Override
	public X509Certificate[] getCertificateChain(String alias) {
		return delegate.getCertificateChain(alias);
	}

	/**
	 * Get the matching aliases for authenticating the client side of a secure
	 * socket, given the public key type and the list of certificate issuer
	 * authorities recognized by the peer (if any).
	 * 
	 * @param keyType
	 *            The key algorithm type name
	 * @param issuers
	 *            The list of acceptable CA issuer subject names, or null if it
	 *            does not matter which issuers are used
	 * 
	 * @return Array of the matching alias names, or null if there were no
	 *         matches
	 */
	@Override
	public String[] getClientAliases(String keyType, Principal[] issuers) {
		return delegate.getClientAliases(keyType, issuers);
	}

	/**
	 * Get the matching aliases for authenticating the server side of a secure
	 * socket, given the public key type and the list of certificate issuer
	 * authorities recognized by the peer (if any).
	 * 
	 * @param keyType
	 *            The key algorithm type name
	 * @param issuers
	 *            The list of acceptable CA issuer subject names, or null if it
	 *            does not matter which issuers are used
	 * 
	 * @return Array of the matching alias names, or null if there were no
	 *         matches
	 */
	@Override
	public String[] getServerAliases(String keyType, Principal[] issuers) {
		return delegate.getServerAliases(keyType, issuers);
	}

	/**
	 * Returns the key associated with the given alias.
	 * 
	 * @param alias
	 *            The alias name
	 * 
	 * @return The requested key, or null if the alias can't be found
	 */
	@Override
	public PrivateKey getPrivateKey(String alias) {
		return delegate.getPrivateKey(alias);
	}

	/**
	 * Choose an alias to authenticate the client side of a secure socket, given
	 * the public key type and the list of certificate issuer authorities
	 * recognized by the peer (if any).
	 * 
	 * @param keyType
	 *            The key algorithm type name(s), ordered with the
	 *            most-preferred key type first
	 * @param issuers
	 *            The list of acceptable CA issuer subject names, or null if it
	 *            does not matter which issuers are used
	 * @param engine
	 *            Ignored
	 * 
	 * @return The alias name for the desired key, or null if there are no
	 *         matches
	 */
	@Override
	public String chooseEngineClientAlias(String[] keyType, Principal[] issuers, SSLEngine engine) {
		return delegate.chooseClientAlias(keyType, issuers, null);
	}

	/**
	 * Returns this key manager's server key alias that was provided in the
	 * constructor.
	 * 
	 * @param keyType
	 *            Ignored
	 * @param issuers
	 *            Ignored
	 * @param engine
	 *            Ignored
	 * 
	 * @return Alias name for the desired key
	 */
	@Override
	public String chooseEngineServerAlias(String keyType, Principal[] issuers, SSLEngine engine) {
		return serverKeyAlias;
	}
}
