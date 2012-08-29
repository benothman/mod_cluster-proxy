package org.jboss.cluster.proxy.container;

import java.io.IOException;

import org.apache.coyote.Adapter;
import org.apache.coyote.InputBuffer;
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
public class MCMPaddapter implements Adapter {
	
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
	private static final String SBADFLD = "SYNTAX: Invalid field \"%s\" in message";
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
	
	
	public void service(Request req, Response res) throws Exception
	{
		MessageBytes methodMB= req.method();
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

	}
	private void process_ping(Request req, Response res) {
		// TODO Auto-generated method stub
		
	}
	private void process_info(Request req, Response res) {
		// TODO Auto-generated method stub
		
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
		// Need more...
		
		
	}
	private void process_error(String type, String errstring, Response res) {
		res.setStatus(500);
		res.addHeader("Version", VERSION_PROTOCOL);
		res.addHeader("Type", type);
		res.addHeader("Mess", errstring);
		
	}
	public boolean event(Request req, Response res, SocketStatus status) throws Exception
	{
		return false;
	}
}
