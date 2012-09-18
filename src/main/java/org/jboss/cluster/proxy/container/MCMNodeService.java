package org.jboss.cluster.proxy.container;

import org.apache.coyote.Request;
import org.apache.tomcat.util.http.Cookies;
import org.apache.tomcat.util.http.ServerCookie;
import org.jboss.cluster.proxy.NodeService;
import org.jboss.cluster.proxy.container.Node.NodeStatus;

public class MCMNodeService extends NodeService {
	static MCMConfig conf = MCMPAdapter.conf;
	@Override
	public void init() throws Exception {
		// Nothing to do :D
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.jboss.cluster.proxy.NodeService#start()
	 */
	@Override
	public void start() {
		// Nothing to do :D
	}
	
	@Override
	public Node getNode(Request request) {
		System.out.println("MCMNodeService: getNode");
        Cookies cookies = request.getCookies();
         Balancer ba = null;
        String value = null;
        for (int i=0; i < cookies.getCookieCount(); i++) {
        	ServerCookie co = cookies.getCookie(i);
        	String name = co.getName().getString();
         	for (Balancer bal : conf.getBalancers()) {
        		if (name.equals(bal.getStickySessionCookie())) {
        			ba = bal;
        			value = co.getValue().getString();
        			break; // Found the balancer.
        		}
        	}
        	if (ba != null)
        		break;
        }
        
        
        Node node = null;
		if (ba != null) {
        	// we have a balancer and a cookie
        	int index = value.lastIndexOf('.');
        	if (index != -1)
        		node = conf.getNode(value.substring(index+1));
        } else {
        	// TODO complete code here
        	node = conf.getNode();
        }
        System.out.println("getNode returns: " + node);
        return node;
	}
	@Override
	public Node getNode(Request request, Node failed) {
		System.out.println("MCMNodeService: getNode (failed:" + failed + ")");
		if (failed != null) {
			failed.setStatus(NodeStatus.NODE_DOWN);
			conf.insertupdate(failed);
		}
		return this.getNode(request);
	}

}
