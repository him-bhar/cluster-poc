package com.himanshu.cluster.mbean;

import java.lang.management.ManagementFactory;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import com.himanshu.cluster.Cluster;
import com.himanshu.cluster.ClusterMBean;

public class ClusterMBeanImpl implements ClusterMBean {
	
	Cluster cluster;

	public Cluster getCluster() {
		return cluster;
	}

	public void setCluster(Cluster cluster) {
		this.cluster = cluster;
	}

	@Override
	public void disconnectNode() {
		cluster.disconnectCurrentNode();

	}

	@Override
	public void joinCluster(String clusterName) {
		cluster.reconnectToNewCluster(clusterName);

	}

	@Override
	public boolean isMaster() {
		return cluster.isMaster();
	}
	
	public void init() throws Exception {
		MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
		ObjectName name = new ObjectName("com.iperia.vx.mg.cluster:type=".concat(cluster.getCurrentNodeLogicalAddress()));		
		mBeanServer.registerMBean(this, name);
	}

	@Override
	public String getCurrentNodeIpAddress() throws Exception {
		return cluster.getCurrentNodeIpAddress();
	}

	@Override
	public String getCurrentNodeLogicalAddress() throws Exception {
		return cluster.getCurrentNodeLogicalAddress();
	}

}
