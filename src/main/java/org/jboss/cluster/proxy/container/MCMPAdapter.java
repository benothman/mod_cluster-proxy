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

	static MCMConfig conf;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.coyote.Adapter#service(org.apache.coyote.Request,
	 * org.apache.coyote.Response)
	 */
	public void service(Request req, Response res) throws Exception {
		MessageBytes methodMB = req.method();

		if (methodMB.equals(Constants.GET)) {
			// In fact that is /mod_cluster_manager
		} else if (methodMB.equals(Constants.CONFIG)) {
			process_config(req, res);
		} else if (methodMB.equals(Constants.ENABLE_APP)) {
			process_enable(req, res);
		} else if (methodMB.equals(Constants.DISABLE_APP)) {
			process_disable(req, res);
		} else if (methodMB.equals(Constants.STOP_APP)) {
			process_stop(req, res);
		} else if (methodMB.equals(Constants.REMOVE_APP)) {
			process_remove(req, res);
		} else if (methodMB.equals(Constants.STATUS)) {
			process_status(req, res);
		} else if (methodMB.equals(Constants.DUMP)) {
			process_dump(req, res);
		} else if (methodMB.equals(Constants.INFO)) {
			process_info(req, res);
		} else if (methodMB.equals(Constants.PING)) {
			process_ping(req, res);
		}
		
		ByteChunk chunk = new ByteChunk();
		
		byte bytes[] = "Hello world!".getBytes();
		chunk.append(bytes, 0, bytes.length);
		res.setContentLength(bytes.length);

		// Write chunk
		res.doWrite(chunk);
		// Send response headers and commit
		res.sendHeaders();
		res.action(ActionCode.ACTION_CLIENT_FLUSH, null);
	}

	private void process_ping(Request req, Response res) {
		// TODO Auto-generated method stub

	}

	private void process_info(Request req, Response res) {
		// TODO Auto-generated method stub
		res.setStatus(200);
		res.setMessage("OK");

	}

	private void process_dump(Request req, Response res) {
		// TODO Auto-generated method stub

	}

	private void process_status(Request req, Response res) {
		// TODO Auto-generated method stub

	}

	private void process_remove(Request req, Response res) {
		// TODO Auto-generated method stub

	}

	private void process_stop(Request req, Response res) {
		// TODO Auto-generated method stub

	}

	private void process_disable(Request req, Response res) {
		// TODO Auto-generated method stub

	}

	private void process_enable(Request req, Response res) {
		// TODO Auto-generated method stub

	}

	private void process_config(Request req, Response res) {
		Parameters params = req.getParameters();
		if (params == null) {
			process_error(TYPESYNTAX, SMESPAR, res);
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
				process_error(TYPESYNTAX, SBADFLD + name + SBADFLD1, res);
				return;
			}

			conf.insertuodate(balancer);
			conf.insertuodate(node);

			res.setStatus(200);
		}

	}

	private void process_error(String type, String errstring, Response res) {
		res.setStatus(500);
		res.addHeader("Version", VERSION_PROTOCOL);
		res.addHeader("Type", type);
		res.addHeader("Mess", errstring);

	}
}
