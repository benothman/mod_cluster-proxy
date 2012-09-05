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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Enumeration;

import org.apache.catalina.connector.Connector;
import org.apache.coyote.ActionCode;
import org.apache.coyote.Adapter;
import org.apache.coyote.Request;
import org.apache.coyote.Response;
import org.apache.coyote.http11.Constants;
import org.apache.tomcat.util.buf.ByteChunk;
import org.apache.tomcat.util.buf.MessageBytes;
import org.apache.tomcat.util.http.Parameters;
import org.apache.tomcat.util.net.NioChannel;
import org.apache.tomcat.util.net.SocketStatus;

/**
 * Adapter. This represents the entry point in a coyote-based servlet container.
 * reads the MCM element
 * 
 * @author Jean-Frederic Clere
 * 
 */
public class MCMPAdapter implements Adapter {

	private static final String VERSION_PROTOCOL = "0.2.1";
	private static final String TYPESYNTAX = "SYNTAX";
	private static final String TYPEMEM = "MEM";

	/* the syntax error messages */
	private static final String SMESPAR = "SYNTAX: Can't parse message";
	private static final String SBALBIG = "SYNTAX: Balancer field too big";
	private static final String SBAFBIG = "SYNTAX: A field is too big";
	private static final String SROUBIG = "SYNTAX: JVMRoute field too big";
	private static final String SROUBAD = "SYNTAX: JVMRoute can't be empty";
	private static final String SDOMBIG = "SYNTAX: LBGroup field too big";
	private static final String SHOSBIG = "SYNTAX: Host field too big";
	private static final String SPORBIG = "SYNTAX: Port field too big";
	private static final String STYPBIG = "SYNTAX: Type field too big";
	private static final String SALIBAD = "SYNTAX: Alias without Context";
	private static final String SCONBAD = "SYNTAX: Context without Alias";
	private static final String SBADFLD = "SYNTAX: Invalid field ";
	private static final String SBADFLD1 = " in message";
	private static final String SMISFLD = "SYNTAX: Mandatory field(s) missing in message";
	private static final String SCMDUNS = "SYNTAX: Command is not supported";
	private static final String SMULALB = "SYNTAX: Only one Alias in APP command";
	private static final String SMULCTB = "SYNTAX: Only one Context in APP command";
	private static final String SREADER = "SYNTAX: %s can't read POST data";

	/* the mem error messages */
	private static final String MNODEUI = "MEM: Can't update or insert node";
	private static final String MNODERM = "MEM: Old node still exist";
	private static final String MBALAUI = "MEM: Can't update or insert balancer";
	private static final String MNODERD = "MEM: Can't read node";
	private static final String MHOSTRD = "MEM: Can't read host alias";
	private static final String MHOSTUI = "MEM: Can't update or insert host alias";
	private static final String MCONTUI = "MEM: Can't update or insert context";
	
	static final byte[] CRLF = "\r\n".getBytes();

	private Connector connector;

	/**
	 * Create a new instance of {@code MCMPaddapter}
	 * 
	 * @param connector
	 */
	public MCMPAdapter(Connector connector) {
		this.connector = connector;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.coyote.Adapter#init()
	 */
	public void init() throws Exception {
		// NOPE
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.coyote.Adapter#event(org.apache.coyote.Request,
	 * org.apache.coyote.Response, org.apache.tomcat.util.net.SocketStatus)
	 */
	public boolean event(Request req, Response res, SocketStatus status)
			throws Exception {
		return false;
	}

	static MCMConfig conf = new MCMConfig();

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.coyote.Adapter#service(org.apache.coyote.Request,
	 * org.apache.coyote.Response)
	 */
	public void service(Request req, Response res) throws Exception {

		NioChannel channel = (NioChannel) res
				.getNote(org.jboss.cluster.proxy.http11.Constants.NODE_CHANNEL_NOTE);


       		Parameters parameters = req.getParameters();
		
		Enumeration<String> names = parameters.getParameterNames();

		while (names.hasMoreElements()) {
			String name = names.nextElement();
			System.out.println(name + "=" + parameters.getParameter(name));
		}		
		
		MessageBytes methodMB = req.method();

		if (methodMB.equals(Constants.GET)) {
			// In fact that is /mod_cluster_manager
		} else if (methodMB.equals(Constants.CONFIG)) {
			process_config(req, channel);
		} else if (methodMB.equals(Constants.ENABLE_APP)) {
			process_enable(req, channel);
		} else if (methodMB.equals(Constants.DISABLE_APP)) {
			process_disable(req, channel);
		} else if (methodMB.equals(Constants.STOP_APP)) {
			process_stop(req, channel);
		} else if (methodMB.equals(Constants.REMOVE_APP)) {
			process_remove(req, channel);
		} else if (methodMB.equals(Constants.STATUS)) {
			process_status(req, channel);
		} else if (methodMB.equals(Constants.DUMP)) {
			process_dump(req, channel);
		} else if (methodMB.equals(Constants.INFO)) {
			process_info(req, channel);
		} else if (methodMB.equals(Constants.PING)) {
			process_ping(req, channel);
		}
        channel.close();
 	}

	private void process_ping(Request req, NioChannel channel) {
		// TODO Auto-generated method stub

	}

	/* Something like:

Node: [1],Name: 368e2e5c-d3f7-3812-9fc6-f96d124dcf79,Balancer: cluster-prod-01,LBGroup: ,Host: 127.0.0.1,Port: 8443,Type: https,Flushpackets: Off,Flushwait: 10,Ping: 10,Smax: 21,Ttl: 60,Elected: 0,Read: 0,Transfered: 0,Connected: 0,Load: 1
Vhost: [1:1:1], Alias: default-host
Vhost: [1:1:2], Alias: localhost
Vhost: [1:1:3], Alias: example.com
Context: [1:1:1], Context: /myapp, Status: ENABLED

     */

	private void process_info(Request req, NioChannel channel) throws Exception {

		String data = "";
		int i = 1;
		
		System.out.println("process_info: " + conf.getNodes());
		for (Node node : conf.getNodes()) {
			String nod = "Node: [" + i + "],Name: " + node.getJvmRoute() + ",Balancer: " + node.getBalancer() +
					",LBGroup: " + node.getDomain() + ",Host: " + node.getHostname() + ",Port: " + node.getPort() +
					",Type: " + node.getType() + ",Flushpackets: " + (node.isFlushpackets()?"On":"Off") +
					",Flushwait: " + node.getFlushwait() + ",Ping: " + node.getPing() + ",Smax: " + node.getSmax() +
					",Ttl: " + node.getTtl() + ",Elected: " + node.getElected() + ",Read: " + node.getRead() +
					",Transfered: " + node.getTransfered() + ",Connected: " + node.getConnected() + ",Load: " + node.getLoad() + "\r\n";
			data = data.concat(nod);
			i++;	
		}
		
		for (VHost host : conf.getHosts()) {
			int j = 1;
			long node = conf.getNodeId(host.getJVMRoute());
			for (String alias : host.getAliases()) {
				String hos = "Vhost: [" + node + ":" + host.getId() + ":" + j + ", Alias: " + alias + "\r\n";
				data = data.concat(hos);
				j++;
			}
		}
		
		i = 1;
		for (Context context : conf.getContexts()) {
			String cont = "Context: [" + conf.getNodeId(context.getJVMRoute()) + ":" + context.getHostId() + ":" + i + "], Context: " + context.getPath() +
					", Status: " + context.getStatus() + "\r\n";
			data = data.concat(cont);
		}
		
		ByteBuffer src = ByteBuffer.allocate(1024);
		src.put("HTTP/1.1 200 OK".getBytes());
		src.put(CRLF);
		addHeader(src, "Content-Type", "text/plain");
		addHeader(src, "Server", "Mod_CLuster/0.0.0");
		if (data.length()>0) {
			addHeader(src, "Content-Length", Integer.toString(data.length()));
			src.put(CRLF);
			src.put(data.getBytes());
		}
		src.put(CRLF);
        src.flip();
        channel.writeBytes(src);
	}

	/*
	 *  something like:

balancer: [1] Name: cluster-prod-01 Sticky: 1 [JSESSIONID]/[jsessionid] remove: 0 force: 0 Timeout: 0 maxAttempts: 1
node: [1:1],Balancer: cluster-prod-01,JVMRoute: 368e2e5c-d3f7-3812-9fc6-f96d124dcf79,LBGroup: [],Host: 127.0.0.1,Port: 8443,Type: https,flushpackets: 0,flushwait: 10,ping: 10,smax: 21,ttl: 60,timeout: 0
host: 1 [default-host] vhost: 1 node: 1
host: 2 [localhost] vhost: 1 node: 1
host: 3 [example.com] vhost: 1 node: 1
context: 1 [/myapp] vhost: 1 node: 1 status: 1

	 */
	private void process_dump(Request req, NioChannel channel) {
		String data = "";
		int i = 1;
		for (Balancer balancer : conf.getBalancers()) {
			String bal = "balancer: [" + i + "] Name: " + balancer.getName() + " Sticky: " + (balancer.isStickySession()?"1":"0") +
					" [" + balancer.getStickySessionCookie() + "]/[" + balancer.getStickySessionPath() +
					"] remove: " + (balancer.isStickySessionRemove()?"1":"0") + " force: " + (balancer.isStickySessionForce()?"1":"0") +
					" Timeout: " + balancer.getWaitWorker() + " maxAttempts: " + balancer.getMaxattempts() + "\r\n";
			data = data.concat(bal);
			i++;
		}
		// TODO Add more...

	}

	private void process_status(Request req, NioChannel channel) throws Exception {
		Parameters params = req.getParameters();
		if (params == null) {
			process_error(TYPESYNTAX, SMESPAR, channel);
			return;
		}
		String jvmRoute = null;
		String load = null;
		Enumeration<String> names = params.getParameterNames();
		while (names.hasMoreElements()) {
			String name = (String) names.nextElement();
			String[] value = params.getParameterValues(name);
			if (name.equalsIgnoreCase("JVMRoute")) {
				jvmRoute= value[0];
			} else if (name.equalsIgnoreCase("Load")) {
				load = value[0];
			} else {
				process_error(TYPESYNTAX, SBADFLD + value[0] + SBADFLD1, channel);
				return;
			}
		}
		if (load == null || jvmRoute == null) {
			process_error(TYPESYNTAX, SMISFLD, channel);
			return;
		}
		
		Node node = conf.getNode(jvmRoute);
		if (node == null) {
			process_error(TYPEMEM, MNODERD, channel);
			return;
		}
		node.setLoad(Integer.parseInt(load));
		/* TODO we need to check the node here */
		node.setStatus(Node.NodeStatus.NODE_UP);
		process_OK(channel);
	}

	private void process_remove(Request req, NioChannel channel) {
		// TODO Auto-generated method stub

	}

	private void process_stop(Request req, NioChannel channel) {
		// TODO Auto-generated method stub

	}

	private void process_disable(Request req, NioChannel channel) {
		// TODO Auto-generated method stub

	}

	private void process_enable(Request req, NioChannel channel) throws Exception {
		Parameters params = req.getParameters();
		if (params == null) {
			process_error(TYPESYNTAX, SMESPAR, channel);
			return;
		}

		Context context = new Context();
		VHost host = new VHost();
		Enumeration<String> names = params.getParameterNames();
		while (names.hasMoreElements()) {
			String name = (String) names.nextElement();
			String[] value = params.getParameterValues(name);
			if (name.equalsIgnoreCase("JVMRoute")) {
				if (conf.getNodeId(value[0]) == -1) {
					process_error(TYPEMEM, MNODERD, channel);
					return;
				}
				host.setJVMRoute(value[0]);
				context.setJVMRoute(value[0]);
			} else if (name.equalsIgnoreCase("Alias")) {
				// Alias is something like =default-host,localhost,example.com
				String aliases[] = value[0].split(",");
				host.setAliases(Arrays.asList(aliases));
			} else if (name.equalsIgnoreCase("Context")) {
				context.setPath(value[0]);
			}

		}
		if (context.getJVMRoute() == null) {
			process_error(TYPESYNTAX, SROUBAD, channel);
			return;
		}
		context.setStatus(Context.Status.ENABLED);
		conf.insertupdate(host);
		conf.insertupdate(context);
		process_OK(channel);

	}

	private void process_config(Request req, NioChannel channel) throws Exception {
		Parameters params = req.getParameters();
		if (params == null) {
			process_error(TYPESYNTAX, SMESPAR, channel);
			return;
		}

		Balancer balancer = new Balancer();
		Node node = new Node();

		Enumeration<String> names = params.getParameterNames();
		while (names.hasMoreElements()) {
			String name = (String) names.nextElement();
			String[] value = params.getParameterValues(name);
			if (name.equalsIgnoreCase("Balancer")) {
				balancer.setName(value[0]);
			} else if (name.equalsIgnoreCase("StickySession")) {
				if (value[0].equalsIgnoreCase("No"))
					balancer.setStickySession(false);
			} else if (name.equalsIgnoreCase("StickySessionCookie")) {
				balancer.setStickySessionCookie(value[0]);
			} else if (name.equalsIgnoreCase("StickySessionPath")) {
				balancer.setStickySessionPath(value[0]);
			} else if (name.equalsIgnoreCase("StickySessionRemove")) {
				if (value[0].equalsIgnoreCase("Yes"))
					balancer.setStickySessionRemove(true);
			} else if (name.equalsIgnoreCase("StickySessionForce")) {
				if (value[0].equalsIgnoreCase("no"))
					balancer.setStickySessionForce(false);
			} else if (name.equalsIgnoreCase("WaitWorker")) {
				balancer.setWaitWorker(Integer.valueOf(value[0]));
			} else if (name.equalsIgnoreCase("Maxattempts")) {
				balancer.setMaxattempts(Integer.valueOf(value[0]));
			} else if (name.equalsIgnoreCase("JVMRoute")) {
				node.setJvmRoute(value[0]);
			} else if (name.equalsIgnoreCase("Domain")) {
				node.setDomain(value[0]);
			} else if (name.equalsIgnoreCase("Host")) {
				node.setHostname(value[0]);
			} else if (name.equalsIgnoreCase("Port")) {
				node.setPort(Integer.valueOf(value[0]));
			} else if (name.equalsIgnoreCase("Type")) {
				node.setType(value[0]);
			} else if (name.equalsIgnoreCase("Reversed")) {
				continue; // ignore it.
			} else if (name.equalsIgnoreCase("flushpacket")) {
				if (value[0].equalsIgnoreCase("on"))
					node.setFlushpackets(true);
				if (value[0].equalsIgnoreCase("auto"))
					node.setFlushpackets(true);
			} else if (name.equalsIgnoreCase("flushwait")) {
				node.setFlushwait(Integer.valueOf(value[0]));
			} else if (name.equalsIgnoreCase("ping")) {
				node.setPing(Integer.valueOf(value[0]));
			} else if (name.equalsIgnoreCase("smax")) {
				node.setSmax(Integer.valueOf(value[0]));
			} else if (name.equalsIgnoreCase("ttl")) {
				node.setTtl(Integer.valueOf(value[0]));
			} else if (name.equalsIgnoreCase("Timeout")) {
				node.setTimeout(Integer.valueOf(value[0]));
			} else {
				process_error(TYPESYNTAX, SBADFLD + name + SBADFLD1, channel);
				return;
			}
		}
		
		conf.insertupdate(balancer);
		conf.insertupdate(node);

		process_OK(channel);
	}

	private void process_OK(NioChannel channel) throws Exception {
        ByteBuffer src = ByteBuffer.allocate(1024);
        src.put("HTTP/1.1 200 OK".getBytes());
        src.put(CRLF);
        addHeader(src, "Content-type", "plain/text");
        src.put(CRLF);
        src.flip();
        channel.writeBytes(src);
	}
	private void process_error(String type, String errstring, NioChannel channel) throws Exception {
		System.out.println("process_error: " + type + " : " + errstring);
        ByteBuffer src = ByteBuffer.allocate(1024);
        src.put("HTTP/1.1 500 ERROR".getBytes());
        src.put(CRLF);
		addHeader(src, "Version", VERSION_PROTOCOL);
		addHeader(src, "Type", type);
		addHeader(src, "Mess", errstring);
		src.put(CRLF);
        src.flip();
        channel.writeBytes(src);	

	}

	private void addHeader(ByteBuffer src, String string, String errstring) {
		String buf = string;
		buf = string.concat(": " + errstring);
		src.put(buf.getBytes());
		src.put(CRLF);
	}
}
