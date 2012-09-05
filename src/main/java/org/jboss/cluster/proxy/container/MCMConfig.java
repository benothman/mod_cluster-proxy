/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership. The ASF
 * licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */


package org.jboss.cluster.proxy.container;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration of the cluster received via the MCM elements.
 * 
 * @author Jean-Frederic Clere
 *
 */
public class MCMConfig {
	
	private List<Node> nodes = new ArrayList<Node>();
	private List<Balancer> balancers  = new ArrayList<Balancer>();
	private List<VHost> hosts = new ArrayList<VHost>();
	private List<Context> contexts = new ArrayList<Context>();
	
	public void insertupdate(Node node) {
		if (getNodes().isEmpty()) {
			getNodes().add(node);
		} else {
			for (Node nod : getNodes()) {
				if (nod.getJvmRoute().equals(node.getJvmRoute())) {
					// replace it.
					// TODO that is more tricky see mod_cluster C code.
					getNodes().remove(nod);
					getNodes().add(node);
					break; // Done
				}
			}
		}
	}
	public void insertupdate(Balancer balancer) {
		if (getBalancers().isEmpty()) {
			getBalancers().add(balancer);
		} else {
			for (Balancer bal : getBalancers()) {
				if (bal.getName().equals(balancer.getName())) {
					// replace it.
					// TODO that is more tricky see mod_cluster C code.
					getBalancers().remove(bal);
					getBalancers().add(balancer);
					break; // Done
				}
			}			
		}		
	}
	public List<Node> getNodes() {
		return nodes;
	}
	public void setNodes(List<Node> nodes) {
		this.nodes = nodes;
	}
	public List<VHost> getHosts() {
		return hosts;
	}
	public void setHosts(List<VHost> hosts) {
		this.hosts = hosts;
	}
	public long getNodeId(String jvmRoute) {
		for (Node nod : getNodes()) {
			if (nod.getJvmRoute().equals(jvmRoute)) {
				return nod.getId();
			}
		}
		return -1;
	}
	public List<Context> getContexts() {
		return contexts;
	}
	public void setContexts(List<Context> contexts) {
		this.contexts = contexts;
	}
	public List<Balancer> getBalancers() {
		return balancers;
	}
	public void setBalancers(List<Balancer> balancers) {
		this.balancers = balancers;
	}
	public Node getNode(String jvmRoute) {
		for (Node nod : getNodes()) {
			if (nod.getJvmRoute().equals(jvmRoute)) {
				return nod;
			}
		}
		return null;
	}
	public long insertupdate(VHost host) {
		int i = 1;
		if (getHosts().isEmpty()) {
			host.setId(i);
			getHosts().add(host);
			return 1;
		} else {
			for (VHost hos : getHosts()) {
				if (hos.getJVMRoute().equals(host.getJVMRoute()) &&
						isSame(host.getAliases(), hos.getAliases())) {
					break;
				}
				i++;
			}
		}
		getHosts().add(host);
		return i;
	}
	private boolean isSame(String[] aliases, String[] aliases2) {
		if (aliases.length != aliases2.length)
			return false;
		for (String host :  aliases)
			if (isNotIn(host, aliases))
				return false;
		return true;
	}
	private boolean isNotIn(String host, String[] aliases) {
		for (String hos :  aliases)
			if (!host.equals(hos))
				return false;
		return true;
	}
	public void insertupdate(Context context) {
		if (getContexts().isEmpty()) {		
			getContexts().add(context);
			return;
		} else {
			for (Context con : getContexts()) {
				if (context.getJVMRoute().equals(con.getJVMRoute()) &&
						context.getHostid() == con.getHostid()	) {
					// update the status.
					con.setStatus(context.getStatus());
					break; // Done
				}
			}			
		}
	}
	
}
