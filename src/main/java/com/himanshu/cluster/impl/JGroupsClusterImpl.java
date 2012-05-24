package com.himanshu.cluster.impl;

import java.net.InetAddress;
import java.net.URL;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import org.jgroups.Address;
import org.jgroups.Event;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.PhysicalAddress;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;
import org.jgroups.blocks.MessageDispatcher;
import org.jgroups.blocks.Request;
import org.jgroups.blocks.RequestHandler;
import org.jgroups.blocks.RequestOptions;
import org.jgroups.protocols.TP;
import org.jgroups.stack.IpAddress;
import org.jgroups.util.RspList;
import org.jgroups.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.himanshu.cluster.Cluster;
import com.himanshu.cluster.NodeJob;

public class JGroupsClusterImpl implements Cluster {
	
	static Logger LOG = LoggerFactory.getLogger(JGroupsClusterImpl.class);
	
	static {
		System.setProperty("java.net.preferIPv4Stack", "true");
	}
	
	Map<NodeJob, Address> jobLoadMap = new ConcurrentHashMap<NodeJob, Address>();
	
	Map<Address, Timestamp> nodeLoadAgeMap = new ConcurrentHashMap<Address, Timestamp>();	//We will follow the LRU policy for submission of jobs
	
    JChannel channel = null;
    
    MessageDispatcher dispatcher = null;
	
	URL configLocation;
	
	View prevView;
	
	public View getPrevView() {
		return prevView;
	}

	public void setPrevView(View prevView) {
		this.prevView = prevView;
	}

	String clusterName;
	
	public String getClusterName() {
		return clusterName;
	}

	public void setClusterName(String clusterName) {
		this.clusterName = clusterName;
	}

	protected List<Listener> listeners = new ArrayList<Cluster.Listener>();
	
	public void registerListener(Listener listener) {
		listeners.add(listener);
	}
	
	public URL getConfigLocation() {
		return configLocation;
	}

	public void setConfigLocation(URL configLocation) {
		this.configLocation = configLocation;
	}

	@Override
	public void joinCluster(String clusterName) {
		try {
			RequestHandler handler = new RequestHandler() {
				
				@Override
				public Object handle(Message msg) {
					Collection<NodeJob> responseList = new ArrayList<NodeJob>();
					try {
						for (Listener listener : listeners) {
							responseList.add(listener.onJobReceive((NodeJob)msg.getObject()));
						}
					} catch (Exception e) {
						LOG.error("Error occured in handling object in :"+channel.getAddressAsUUID());
					}
					return responseList;
				}
			};
			channel = new JChannel(getConfigLocation());
			channel.connect(clusterName);
			prevView = channel.getView();
			ReceiverAdapter adapter = new ReceiverAdapter() {
				@Override
				public void viewAccepted(View view) {
					Address masterNode = null;
					Address newMasterNode = null;
					
					//Find previous master node
					Collection<Address> prevClusterMembers = prevView.getMembers();
					for (Address address : prevClusterMembers) {
						if (Util.getRank(prevView, address) == 1) {
							masterNode = address;
							break;
						} else {
							continue;
						}
					}
					
					//Determine the left members in the cluster
					Collection<Address> leftMembers = Util.determineLeftMembers(prevView.getMembers(), view.getMembers());
					Collection<String> leftMembersAddressStr = new ArrayList<String>();
					for (Address address : leftMembers) {
						leftMembersAddressStr.add(address.toString());
					}
					
					for (Listener listener : listeners) {
						listener.onNodesRemoved(leftMembersAddressStr);
					}
					
					//
					
					//Determine the new members added in the cluster
					Collection<Address> newMembers = Util.newMembers(prevView.getMembers(), view.getMembers());
					Collection<String> newMembersAddressStr = new ArrayList<String>();
					for (Address address : newMembers) {
						newMembersAddressStr.add(address.toString());
					}
					
					for (Listener listener : listeners) {
						listener.onNewNodesAdded(newMembersAddressStr);
					}
					//
					
					cleanupNodeLoadAgeMap(leftMembers, newMembers);
					
					prevView = view;
					
					//Find current(NEW) master node
					Collection<Address> newClusterMembers = prevView.getMembers();
					for (Address address : newClusterMembers) {
						if (Util.getRank(prevView, address) == 1) {
							newMasterNode = address;
							break;
						} else {
							continue;
						}
					}
					
					//Execute listener method if master is re-elected
					if (masterNode.compareTo(newMasterNode) != 0) {
						for (Listener listener : listeners) {
							listener.onMasterReelection(newMasterNode.toString(), channel.getAddressAsString());
						}
					}
					
					super.viewAccepted(view);
				}
				
				/*@Override
				public void unblock() {
					super.unblock();
					
				}*/
				
				@Override
				public void suspect(Address suspected_mbr) {
					super.suspect(suspected_mbr);
					
				}
				
				@Override
				public void block() {
					super.block();
				}
				
				/*@Override
				public void setState(InputStream input) throws Exception {
					super.setState(input);
				}*/
				
				@Override
				public void receive(Message msg) {
					for (Listener listener : listeners) {
						listener.onJobReceive((NodeJob)msg.getObject());
					}
					super.receive(msg);
				}
				
				/*@Override
				public void getState(OutputStream output) throws Exception {
					super.getState(output);
				}*/
			};
			
			dispatcher = new MessageDispatcher(channel, adapter, adapter, handler);
		} catch (Exception e) {
			LOG.error("Error in starting the node", e);
			throw new IllegalArgumentException("Error in starting the node, please check cluster configuration", e);
		}
	}

	@Override
	public boolean isMaster() {
		if (Util.getRank(prevView, channel.getAddress()) == 1) {
			return true;
		} else {
			return false;
		}
	}

	//Find the least stressed node and send job on that node
	/*@Override
	public void submitJob(NodeJob job) throws Exception {
		if (isMaster()) {
			//Proceed with job submission
			List<Address> clusterMembers = new ArrayList<Address>(this.prevView.getMembers());
			
			//Job Cleanups
			Set<NodeJob> jobs = jobLoadMap.keySet();
			for (NodeJob cachedJob : jobs) {
				if (cachedJob.getJobStatus() != null) {
					if (cachedJob.getJobStatus().equals(JobStatus.COMPLETED)) {
						jobLoadMap.remove(cachedJob);
					}
				}
			}
			
			//Calculate stress on nodes
			jobs = null; //de-reference
			jobs = jobLoadMap.keySet();
			Map<Address, AtomicInteger> nodeLoadMap = new ConcurrentHashMap<Address, AtomicInteger>();
			for (Address address : clusterMembers) {
				nodeLoadMap.put(address, new AtomicInteger(0));
			}
			for (NodeJob cachedJob : jobs) {
				Address nodeAddress = jobLoadMap.get(cachedJob);
				if (nodeLoadMap.get(nodeAddress) != null) {
					AtomicInteger jobCounts = nodeLoadMap.get(nodeAddress);
					jobCounts.incrementAndGet();
				} else {
					//This is not a valid job
					jobLoadMap.remove(cachedJob);
				}
			}
			
			//Decide where to push job to
			if (clusterMembers.size() == nodeLoadMap.size()) {
				Set<Address> nodeAddressSet = nodeLoadMap.keySet();
				Address leastStressedNode = null;
				int prevLoad = 0;
				boolean isEntered = false;
				for (Address address : nodeAddressSet) {
					if (! isEntered) {
						leastStressedNode = address;
						prevLoad = nodeLoadMap.get(address).intValue();
						isEntered = true;
					} else {
						int currentNodeLoad = nodeLoadMap.get(address).intValue();
						if (prevLoad > currentNodeLoad) {
							leastStressedNode = address;
							prevLoad = currentNodeLoad;
						}
					}
				}
				if (leastStressedNode == null) {
					//There is some problem with algo check for that.
					LOG.warn("There is some problem with cluster job submit algorithm check for least stressed node.");
				}
				Message jobMsg = new Message(leastStressedNode, this.channel.getAddress(), job);
				//channel.send(jobMsg);
				jobLoadMap.put(job, leastStressedNode);
				nodeLoadMap.put(leastStressedNode, new AtomicInteger(1));
				LOG.debug("Job {} submitted to {}", job, leastStressedNode);
				Object response = dispatcher.sendMessage(jobMsg, new RequestOptions(ResponseMode.GET_ALL, 10000));
				LOG.debug("Response is  "+response);
			} else {
				//Identify the missing node and assign it jobs
				for (Address address : clusterMembers) {
					if (! nodeLoadMap.containsKey(address)) {
						Message jobMsg = new Message(address, this.channel.getAddress(), job);
						//channel.send(jobMsg);
						dispatcher.sendMessage(jobMsg, new RequestOptions(ResponseMode.GET_ALL, 10000));
						jobLoadMap.put(job, address);
						nodeLoadMap.put(address, new AtomicInteger(1));
						LOG.debug("Job {} submitted to {}", job, address);
						break;
					}
				}
			}
		} else {
			// Discard job submissions if any as this is not master node.
		}
	}*/
	
	
	//For Testing Purpose
	//Submit to master
	/*@Override
	public void submitJob(NodeJob job) throws Exception {
		List<Address> list = prevView.getMembers();
		for (Address add : list) {
			if (Util.getRank(prevView, add) == 1) {
				Message msg = new Message(add, channel.getAddress(), job);
				System.out.println(dispatcher.sendMessage(msg, new RequestOptions(ResponseMode.GET_ALL, 10000)));
				break;
			}
		}
	}*/
	
	private Address getNextNodeAddress() {
		List<Address> clusterMembers = prevView.getMembers();
		for (Address member : clusterMembers) {
			if (! nodeLoadAgeMap.containsKey(member)) {
				nodeLoadAgeMap.put(member, new Timestamp(new Date().getTime()));
			} else {
				if (nodeLoadAgeMap.get(member) == null) {
					nodeLoadAgeMap.put(member, new Timestamp(new Date().getTime()));
				} else {
					continue;
				}
			}
		}
		Address lruNode = channel.getAddress();	//Capture local address
		long currentTime = System.currentTimeMillis();
		Set<Address> addresses = nodeLoadAgeMap.keySet();
		for (Address addressIter : addresses) {
			//Address lruNode = addressIter;
			long iteratorNodeTime = currentTime-nodeLoadAgeMap.get(addressIter).getTime(); 	// This gives the time in millis since iter node was used
			long lruNodeTime = currentTime-nodeLoadAgeMap.get(lruNode).getTime(); // This gives the time in millis since lru node was used
			if ((lruNodeTime - iteratorNodeTime) > 0) {
				//LRU node is the older one
			} else {
				lruNode = addressIter;
			}
		}
		return lruNode;
	}
	
	@Override
	public void submitJob(NodeJob job) throws Exception {
		if (prevView != null) {
			if (isMaster()) {
				Address lruNode = getNextNodeAddress();
				Message msg = new Message(lruNode, channel.getAddress(), job);
				nodeLoadAgeMap.put(lruNode, new Timestamp(new Date().getTime()));
				try {
					Collection<NodeJob> responseList = (Collection<NodeJob>)dispatcher.sendMessage(msg, new RequestOptions(Request.GET_ALL, 10000));
					for (Listener listener : listeners) {
						listener.onResponseReceive(responseList);
					}
				} catch (Exception e) {
					LOG.error("There was error in submitting the jobs");
					throw e;
				}
			} else {
				LOG.debug("Not a master, so cannot submit job in cluster");
			}
		} else {
			LOG.error("There are no nodes to submit job to, raise error");
			throw new UnsupportedOperationException("There are no nodes to submit job to, raising error");
		}
	}

	@Override
	public String getCurrentNodeIpAddress() throws Exception {
		/*InetAddress inetAddress = 
		if (inetAddress != null) {
			return inetAddress.getHostAddress();
		} else {
			throw new UnknownHostException("Address not found");
		}
		return channel.getProtocolStack().getTransport().getBindAddress();*/
		Address nextNode = getNextNodeAddress();
		//TP t = (TP) channel.getProtocolStack().getTransport();
		PhysicalAddress physicalAddr = (PhysicalAddress)channel.downcall(new Event(Event.GET_PHYSICAL_ADDRESS, channel.getAddress()));
        if(physicalAddr instanceof IpAddress) {
            IpAddress ipAddr = (IpAddress)physicalAddr;
            InetAddress inetAddr = ipAddr.getIpAddress();
            //System.out.println(inetAddr.getHostAddress());
            return inetAddr.getHostAddress();
        }
		return null;
	}

	@Override
	public String getCurrentNodeLogicalAddress() throws Exception {
		return channel.getAddressAsString();
	}
	
	private void cleanupNodeLoadAgeMap(Collection<Address> leftMembers,	Collection<Address> newMembers) {
		for (Address leftMember : leftMembers) {
			if (nodeLoadAgeMap.containsKey(leftMember)) {
				nodeLoadAgeMap.remove(leftMember);
			}
		}
	}

	@Override
	public void disconnectCurrentNode() {
		channel.disconnect();
	}

	@Override
	public void reconnectToNewCluster(String clusterName) {
		channel.disconnect();
		joinCluster(clusterName);
	}

	@Override
	public void prepareCluster() {
		joinCluster(getClusterName());
	}

	@Override
	public void broadcastJob(NodeJob job) throws Exception {
		Message msg = new Message(null, channel.getAddress(), job);
		RspList response = dispatcher.castMessage(null, msg, new RequestOptions(Request.GET_ALL, 10000));
		Vector<Object> resultVector = (Vector<Object>)response.getResults();
		Collection<NodeJob> resultList = new ArrayList<NodeJob>();
		for (Object resultV : resultList) {
			resultList.add((NodeJob)resultV);
		}
		
		for (Listener listener : listeners) {
			listener.onResponseReceive(resultList);
		}
	}

	@Override
	public String getNextNodeIpAddress() {
		Address nextNode = getNextNodeAddress();
		//TP t = (TP) channel.getProtocolStack().getTransport();
		PhysicalAddress physicalAddr = (PhysicalAddress)channel.downcall(new Event(Event.GET_PHYSICAL_ADDRESS, nextNode));
        if(physicalAddr instanceof IpAddress) {
            IpAddress ipAddr = (IpAddress)physicalAddr;
            InetAddress inetAddr = ipAddr.getIpAddress();
            //System.out.println(inetAddr.getHostAddress());
            return inetAddr.getHostAddress();
        }
		return null;
	}
	
}
