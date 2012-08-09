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
import java.nio.channels.AsynchronousChannelGroup;

/**
 * {@code DefaultNioChannelFactory}
 * 
 * Created on Aug 9, 2012 at 10:26:46 AM
 * 
 * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
 */
public class DefaultNioChannelFactory extends NioChannelFactory {

	/**
	 * Create a new instance of {@code DefaultNioChannelFactory}
	 */
	public DefaultNioChannelFactory() {
		super();
	}

	/**
	 * Create a new instance of {@code DefaultNioChannelFactory}
	 * 
	 * @param threadGroup
	 */
	protected DefaultNioChannelFactory(AsynchronousChannelGroup threadGroup) {
		this.threadGroup = threadGroup;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.tomcat.util.net.NioChannelFactory#init()
	 */
	@Override
	public void init() throws IOException {
		log.info("Intializing the channel factory");
		// TODO
		log.info("Channel factory Intialized");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.tomcat.util.net.NioChannelFactory#destroy()
	 */
	@Override
	public void destroy() throws IOException {
		log.info("Destroying the channel factory");
		if (this.threadGroup != null) {
			this.threadGroup.shutdown();
		}

		this.attributes.clear();
		this.attributes = null;
		log.info("Channel factory Destroyed");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.tomcat.util.net.NioChannelFactory#initChannel(org.apache.tomcat
	 * .util.net.NioChannel)
	 */
	@Override
	public void initChannel(NioChannel channel) throws Exception {
		// NOPE
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.tomcat.util.net.NioChannelFactory#handshake(org.apache.tomcat
	 * .util.net.NioChannel)
	 */
	@Override
	public void handshake(NioChannel channel) throws IOException {
		// NOPE
	}

}
