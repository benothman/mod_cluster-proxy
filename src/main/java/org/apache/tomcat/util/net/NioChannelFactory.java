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
package org.apache.tomcat.util.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.AsynchronousChannelGroup;
import java.util.Hashtable;

import org.apache.tomcat.util.net.jsse.NioJSSEChannelFactory;

/**
 * {@code NioChannelFactory}
 * 
 * Created on Aug 9, 2012 at 10:24:40 AM
 * 
 * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
 */
public abstract class NioChannelFactory implements Cloneable {

	protected static org.jboss.logging.Logger log = org.jboss.logging.Logger
			.getLogger(NioChannelFactory.class);

	private static NioChannelFactory theFactory;
	protected Hashtable<String, Object> attributes = new Hashtable<String, Object>();
	protected AsynchronousChannelGroup channelGroup;

	/**
	 * Create a new instance of {@code NioChannelFactory}
	 */
	protected NioChannelFactory() {
		/* NOTHING */
	}

	/**
	 * Create a new instance of {@code NioChannelFactory}
	 * 
	 * @param threadGroup
	 */
	protected NioChannelFactory(AsynchronousChannelGroup threadGroup) {
		this.channelGroup = threadGroup;
	}

	/**
	 * Create and configure a new secure {@code NioChannelFactory} instance
	 * 
	 * @param threadGroup
	 *            the thread group that will be used with the server sockets
	 * @return a new secure {@code NioChannelFactory} instance
	 */
	public static synchronized NioChannelFactory createSecureFactory(
			AsynchronousChannelGroup threadGroup) {
		return new NioJSSEChannelFactory(threadGroup);
	}

	/**
	 * Create a new {@code NioChannelFactory} instance
	 * 
	 * @param threadGroup
	 *            the thread group that will be used with the server sockets
	 * @param secure
	 * @return a new secure {@code NioChannelFactory} instance
	 */
	public static synchronized NioChannelFactory createNioChannelFactory(
			AsynchronousChannelGroup threadGroup, boolean secure) {
		return secure ? createSecureFactory(threadGroup) : getDefault(threadGroup);
	}

	/**
	 * Returns a copy of the environment's default socket factory.
	 * 
	 * @param threadGroup
	 * @return the default factory
	 */
	public static synchronized NioChannelFactory getDefault(AsynchronousChannelGroup threadGroup) {
		//
		// optimize typical case: no synch needed
		//

		if (theFactory == null) {
			//
			// Different implementations of this method could
			// work rather differently. For example, driving
			// this from a system property, or using a different
			// implementation than JavaSoft's.
			//

			theFactory = new DefaultNioChannelFactory(threadGroup);
		}

		try {
			return (NioChannelFactory) theFactory.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	/**
	 * Initialize the factory
	 * 
	 * @throws IOException
	 */
	public abstract void init() throws IOException;

	/**
	 * Destroy the factory
	 * 
	 * @throws IOException
	 */
	public abstract void destroy() throws IOException;

	/**
	 * Initialize the specified {@code NioChannel}
	 * 
	 * @param channel
	 *            The channel to be initialized
	 * @throws Exception
	 */
	public abstract void initChannel(NioChannel channel) throws Exception;

	/**
	 * Extra function to initiate the handshake. Sometimes necessary for SSL
	 * 
	 * @param channel
	 * 
	 * @exception IOException
	 */
	public abstract void handshake(NioChannel channel) throws IOException;

	/**
	 * Open a new channel
	 * 
	 * @return the channel opened
	 * @throws IOException
	 */
	public NioChannel open() throws IOException {
		return NioChannel.open();
	}

	/**
	 * Connect the {@code NioChannel} to the remote address
	 * 
	 * @param channel
	 *            the channel to connect
	 * @param socketAddress
	 *            the remote address
	 * @return the channel connected to the remote address
	 * @throws Exception
	 */
	public NioChannel connect(NioChannel channel, SocketAddress socketAddress) throws Exception {
		channel.connect(socketAddress).get();
		return channel;
	}

	/**
	 * Open a new {@code NioChannel} and connect it the remote address
	 * 
	 * @param socketAddress
	 *            the remote address
	 * @return a new connected {@code NioChannel}
	 * @throws Exception
	 * @see {@link #connect(NioChannel, SocketAddress)}
	 */
	public NioChannel connect(SocketAddress socketAddress) throws Exception {
		return connect(open(), socketAddress);
	}

	/**
	 * Open a new {@code NioChannel} and connect it the remote address given by
	 * the <tt>hostname</tt> and <tt>port</tt> number
	 * 
	 * @param hostname
	 * @param port
	 * @return
	 * @throws Exception
	 * @see {@link #connect(SocketAddress)}
	 */
	public NioChannel connect(String hostname, int port) throws Exception {
		return connect(new InetSocketAddress(hostname, port));
	}

	/**
	 * General mechanism to pass attributes from the ServerConnector to the
	 * channel factory.
	 * 
	 * Note that the "preferred" mechanism is to use bean setters and explicit
	 * methods, but this allows easy configuration via server.xml or simple
	 * Properties
	 * 
	 * @param name
	 * @param value
	 */
	public void setAttribute(String name, Object value) {
		if (name != null && value != null) {
			attributes.put(name, value);
		}
	}
}
