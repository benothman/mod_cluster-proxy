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
package org.jboss.cluster.proxy.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * {@code VHost}
 * 
 * Created on Jun 12, 2012 at 3:33:21 PM
 * 
 * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
 */
public class VHost implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private List<String> alias;

	/**
	 * Create a new instance of {@code VirtualHost}
	 */
	public VHost() {
		this.alias = new ArrayList<>();
	}

	/**
	 * Add the specified alias to the list
	 * 
	 * @param alias
	 *            the alias to be added
	 * @return <tt>true</tt> if the {@code alias} was added successfully else
	 *         <tt>false</tt>
	 */
	public boolean addAlias(String alias) {
		return this.alias.add(alias);
	}

	/**
	 * Remove the specified alias from the list of aliases
	 * 
	 * @param alias
	 *            the alias to be removed
	 * @return <tt>true</tt> if the {@code alias} was removed else
	 *         <tt>false</tt>
	 */
	public boolean removeAlias(String alias) {
		return this.alias.remove(alias);
	}

	/**
	 * Getter for aliases list
	 * 
	 * @return the list of aliases
	 */
	public List<String> getAlias() {
		return this.alias;
	}

	/**
	 * Setter for the aliases list
	 * 
	 * @param alias
	 *            the alias to set
	 */
	public void setAlias(List<String> alias) {
		this.alias = alias;
	}

}
