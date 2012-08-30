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
	private List<Balancer> balancers  = new ArrayList<Balancer>();;
	
	public void insertuodate(Node node) {
		if (nodes.isEmpty()) {
			nodes.add(node);
		} else {
			for (Node nod : nodes) {
				if (nod.getJvmRoute().equals(node.getJvmRoute())) {
					// replace it.
					// TODO that is more tricky see mod_cluster C code.
					nodes.remove(nod);
					nodes.add(node);
					break; // Done
				}
			}
		}
	}
	public void insertuodate(Balancer balancer) {
		if (balancers.isEmpty()) {
			balancers.add(balancer);
		} else {
			for (Balancer bal : balancers) {
				if (bal.getName().equals(balancer.getName())) {
					// replace it.
					// TODO that is more tricky see mod_cluster C code.
					balancers.remove(bal);
					balancers.add(balancer);
					break; // Done
				}
			}			
		}		
	}
	
}
