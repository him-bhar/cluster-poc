package com.himanshu.cluster;

import java.util.Collection;

/**
 * This is the common interface which the clustering implementations, should implement in order to be used.
 * 
 * @author himanshu.bhardwaj
 *
 */
public interface Cluster {
	
	/**
	 * This method is used to start a cluster.
	 */
	void prepareCluster();
	
	/**
	 * This method is used to join another cluster, after disconnecting from the current cluster.
	 * @param clusterName
	 */
	void joinCluster(String clusterName);
	
	/**
	 * This method returns true if the current node is master node of the cluster, returns false otherwise.
	 * @return boolean
	 */
	boolean isMaster();
	
	/**
	 * This method is used to submit a job into the cluster.
	 * @param job
	 * @throws Exception
	 */
	void submitJob(NodeJob job) throws Exception;
	
	/**
	 * This method is used to broadcast a job into the cluster for all nodes to run.
	 * @param job
	 * @throws Exception
	 */
	void broadcastJob(NodeJob job) throws Exception;
	
	/**
	 * This method is used to get the current node's IP address, which may be used by underlying API for Internal Purposes.
	 * @return
	 * @throws Exception
	 */
	String getCurrentNodeIpAddress () throws Exception;
	
	/**
	 * This method is used to get the current node's logical name, which may be used by underlying API for Internal Purposes.
	 * @return
	 * @throws Exception
	 */
	String getCurrentNodeLogicalAddress () throws Exception;
	
	/**
	 * This method is used to disconnect from the cluster.
	 */
	void disconnectCurrentNode();
	
	/**
	 * This method is used to reconnect to a new cluster
	 * @param clusterName
	 */
	void reconnectToNewCluster(String clusterName);
	
	/**
	 * This method provides the next node Ip address to which the job can be assigned
	 * @return ip address of the node
	 */
	String getNextNodeIpAddress ();
	
	/**
	 * This method provides a way to register listeners to the cluster system to get triggers for any activities internal to cluster
	 * @param listener
	 */
	public void registerListener(Listener listener);
	
	/**
	 * This is used to listen to new events that occur inside a cluster
	 * @author himanshu.bhardwaj
	 *
	 */
	interface Listener {
		/**
		 * Master node changed
		 * @param masterNodeAddress
		 * @param currentNodeAddress
		 */
		void onMasterReelection(String masterNodeAddress, String currentNodeAddress);
		
		/**
		 * New job received on current node
		 * @param job
		 * @return
		 */
		NodeJob onJobReceive(NodeJob job);
		
		/**
		 * Response received on current node
		 * @param result
		 */
		void onResponseReceive (Collection<NodeJob> result);
		
		/**
		 * List of the new nodes that are added inside the cluster
		 * @param nodesLogicalNames
		 */
		void onNewNodesAdded (Collection<String> nodesLogicalNames);
		
		/**
		 * List of nodes that were removed from the cluster
		 * @param nodesLogicalNames
		 */
		void onNodesRemoved (Collection<String> nodesLogicalNames);
	}
}
