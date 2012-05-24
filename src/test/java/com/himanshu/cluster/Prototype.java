package com.himanshu.cluster;

//import org.jgroups.Address;
//import org.jgroups.JChannel;
//import org.jgroups.Message;
//import org.jgroups.ReceiverAdapter;
//import org.jgroups.View;

/*
 * http://www.jgroups.org/manual/html/index.html
 */
public class Prototype {
//    
//    private static class Receiver extends ReceiverAdapter {
//
//        @Override
//        public void receive(Message msg) {
//            System.out.println(msg);
//            super.receive(msg);
//        }
//
//        @Override
//        public byte[] getState() {
//            // TODO Auto-generated method stub
//            return super.getState();
//        }
//
//        @Override
//        public void setState(byte[] state) {
//            // TODO Auto-generated method stub
//            super.setState(state);
//        }
//
//        @Override
//        public void viewAccepted(View new_view) {
//            // TODO Auto-generated method stub
//            super.viewAccepted(new_view);
//        }
//
//        @Override
//        public void suspect(Address suspected_mbr) {
//            // TODO Auto-generated method stub
//            super.suspect(suspected_mbr);
//        }
//
//        @Override
//        public void block() {
//            // TODO Auto-generated method stub
//            super.block();
//        }
//        
//    }
//    public static void main(String[] args) throws Exception {
//        JChannel channel = new JChannel();
//        channel.setReceiver(new Receiver());
//        //channel.setName();
//        channel.connect("cluster1");
//        Message msg = new Message();
//        msg.setBuffer(new byte[0]);
//        channel.send(msg);
//        
//        /*
//        JChannel channel = new JChannel("/home/bela/udp.xml");
//        channel.setReceiver(new ReceiverAdapter() {
//            public void receive(Message msg) {
//                System.out.println("received msg from " + msg.getSrc() + ": " + msg.getObject());
//            }
//        });
//        channel.connect("MyCluster");
//        channel.send(new Message(null, null, "hello world"));
//        channel.close();
//        */
//    }
}
