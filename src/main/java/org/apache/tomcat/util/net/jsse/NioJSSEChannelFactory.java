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
package org.apache.tomcat.util.net.jsse;

import java.io.IOException;
import java.nio.channels.AsynchronousChannelGroup;

import org.apache.tomcat.util.net.DefaultNioChannelFactory;
import org.apache.tomcat.util.net.NioChannel;

/**
 * {@code NioJSSEChannelFactory}
 * 
 * Created on Aug 9, 2012 at 10:32:03 AM
 * 
 * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
 */
public class NioJSSEChannelFactory extends DefaultNioChannelFactory {

	/**
	 * Create a new instance of {@code NioJSSEChannelFactory}
	 */
	public NioJSSEChannelFactory() {
		super();
	}

	/**
	 * Create a new instance of {@code NioJSSEChannelFactory}
	 * 
	 * @param threadGroup
	 */
	public NioJSSEChannelFactory(AsynchronousChannelGroup threadGroup) {
		super(threadGroup);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.tomcat.util.net.DefaultNioChannelFactory#init()
	 */
	@Override
	public void init() throws IOException {
		super.init();
		// TODO
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.tomcat.util.net.DefaultNioChannelFactory#destroy()
	 */
	@Override
	public void destroy() throws IOException {
		super.destroy();
		// TODO
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.tomcat.util.net.DefaultNioChannelFactory#initChannel(org.apache
	 * .tomcat.util.net.NioChannel)
	 */
	public void initChannel(NioChannel channel) throws Exception {
		// TODO
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.tomcat.util.net.DefaultNioChannelFactory#handshake(org.apache
	 * .tomcat.util.net.NioChannel)
	 */
	@Override
	public void handshake(NioChannel channel) throws IOException {
		// TODO
	}

}
