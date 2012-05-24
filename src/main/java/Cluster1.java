import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;


public class Cluster1 {
	static {
		System.setProperty("java.net.preferIPv4Stack", "true");
		System.setProperty("jgroups.udp.mcast_port", "40011");
		//System.setProperty("mcast_group_addr", "238.0.1.1");
		System.setProperty("jgroups.udp.mcast_addr", "230.0.0.6");
	}
	public static void main(String[] args) {
		try {
			JChannel channel = new JChannel();
			/*System.setProperty("jgroups.udp.mcast_port", "400");
			//System.setProperty("mcast_group_addr", "238.0.1.1");
			System.setProperty("jgroups.udp.mcast_addr", "228.1.2.5");*/
			channel.connect("mg_cache");
			channel.setReceiver(new ReceiverAdapter() {
				@Override
				public void viewAccepted(View view) {
					System.out.println("View Changed Node1:"+view);
					super.viewAccepted(view);
				}
			});
			System.out.println(channel.getName());
			Message msg = new Message(null, null, new DummyObject());
			channel.send(msg);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
