package com.himanshu.cluster;

import javax.management.MXBean;

@MXBean
public interface ClusterMBean {
	void disconnectNode();
	void joinCluster(String clusterName);
	boolean isMaster();
	String getCurrentNodeIpAddress() throws Exception;
	String getCurrentNodeLogicalAddress() throws Exception;
}
