import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;
import org.jgroups.blocks.MessageDispatcher;
import org.jgroups.blocks.RequestHandler;
import org.jgroups.blocks.RequestOptions;


public class Cluster_Node3 {
	private View prev_view;
	private Address localAddress;
	static {
		System.setProperty("java.net.preferIPv4Stack", "true");
		System.setProperty("jgroups.udp.mcast_port", "40011");
		//System.setProperty("mcast_group_addr", "238.0.1.1");
		System.setProperty("jgroups.udp.mcast_addr", "228.1.2.5");
		
	}
	public static void main(String[] args) {
		Cluster_Node3 node = new Cluster_Node3();
		RequestHandler handler = new RequestHandler() {
			
			@Override
			public Object handle(Message msg) {
				System.out.println("handling object:  "+msg.getObject());
				return "Handled Object";
			}
		};
		try {
			/*JChannel channel = new JChannel();
			
			ReceiverAdapter adapter = new ReceiverAdapter() {
				@Override
				public void viewAccepted(View view) {
					System.out.println("New View::"+view);
					super.viewAccepted(view);
				}
				
				@Override
				public void receive(Message msg) {
					System.out.println("New Msg received");
					super.receive(msg);
				}
			};
			
			MessageDispatcher dispatcher = new MessageDispatcher(channel, adapter, adapter, handler);*/
			JChannel channel = new JChannel();
			channel.connect("mg_cache");
			channel.setReceiver(new ReceiverAdapter() {
				@Override
				public void viewAccepted(View view) {
					System.out.println("View Changed Node1:"+view);
					super.viewAccepted(view);
				}
				
				@Override
				public void receive(Message msg) {
					System.out.println("Received  "+msg);
					super.receive(msg);
				}
			});
			//dispatcher.setRequestHandler(handler);
			//channel.connect("himanshu");
			node.prev_view = channel.getView();
			node.localAddress = channel.getAddress();
			
			Message msg = new Message(null, node.localAddress, new DummyObject());
			channel.send(msg);
			/*RspList responses = dispatcher.castMessage(null, msg, new RequestOptions(ResponseMode.GET_ALL, 0));
			if(responses.containsKey(node.localAddress)) {
				Rsp response = responses.get(node.localAddress);
				System.err.println("Exception in response:"+response.getException());
				System.out.println("Response is:"+response.getValue());
			}*/
			//dispatcher.sendMessage(msg, new RequestOptions());
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/*@Override
	public void viewAccepted(View view) {
		System.out.println("Previous view is:"+prev_view);
		System.out.println("View Changed:"+view);
		System.out.println("Rank for: "+localAddress.toString()+" is :"+Util.getRank(view, localAddress));
		super.viewAccepted(view);
	}
	
	@Override
	public void receive(Message msg) {
		System.out.println("New message received:"+msg.getObject());
		super.receive(msg);
	}*/
}
