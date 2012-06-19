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

import javax.net.ssl.SSLSession;
import org.apache.tomcat.util.net.SSLSupport;

/**
 * {@code JSSEFactory}
 * 
 * Created on Jun 15, 2012 at 2:29:09 PM
 * 
 * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
 * @param <E>
 */
public interface SSLFactory<E> {

	/**
	 * Returns the SSLSupport attached to this channel.
	 * 
	 * @param channel
	 * @return the SSLSupport attached to this channel
	 */
	public SSLSupport<E> getSSLSupport(E channel);

	/**
	 * Return the SSLSupport attached to this session
	 * 
	 * @param session
	 * @return the SSLSupport attached to this session
	 */
	public SSLSupport<E> getSSLSupport(SSLSession session);

}
