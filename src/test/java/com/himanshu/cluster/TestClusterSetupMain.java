package com.himanshu.cluster;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;

import com.himanshu.cluster.Cluster;
import com.himanshu.cluster.NodeJob;
import com.himanshu.cluster.impl.DummyJobImpl;
import com.himanshu.cluster.impl.DummyJobResultImpl;
import com.himanshu.cluster.impl.JGroupsClusterImpl;

public class TestClusterSetupMain {
	static Logger LOG = LoggerFactory.getLogger(TestClusterSetupMain.class);
	public XmlBeanFactory ctx = null; 
	public String CLUSTER_BEAN_NAME = "cluster";
	public String JOB_BOMBARDER_BEAN_NAME = "jobBombarder";
	public String CLUSTER_MXBEAN_BEAN_NAME = "clusterMBean";
	public JGroupsClusterImpl cluster = null;
	
	public static void main(String[] args) throws Exception {
		TestClusterSetupMain obj = new TestClusterSetupMain();
		System.setProperty("com.iperia.vx.mg.cluster.name", "nest");
		org.springframework.core.io.Resource xmlRes = new ClassPathResource("/mg-cluster.xml");
		obj.ctx = new XmlBeanFactory(xmlRes);
		PropertyPlaceholderConfigurer cfg = new PropertyPlaceholderConfigurer();
		cfg.setProperties(System.getProperties());
		cfg.postProcessBeanFactory(obj.ctx);
		//JGroupsClusterImpl clusterInstance = (JGroupsClusterImpl)ctx.getBean(BEAN_NAME);
		Cluster cluster = (JGroupsClusterImpl)obj.ctx.getBean(obj.CLUSTER_BEAN_NAME);
		obj.ctx.getBean(obj.CLUSTER_MXBEAN_BEAN_NAME);
		//LOG.info("Current node address is :"+obj.cluster.getCurrentNodeIpAddress());
		cluster.registerListener(new Cluster.Listener() {
			
			@Override
			public void onMasterReelection(String masterAddress, String currentNodeAddress) {
				LOG.debug("Master Re-election done");
			}
			
			@Override
			public NodeJob onJobReceive(NodeJob job) {
				//job = null;
				job.getJobId();
				LOG.debug("New job received:"+job);
				DummyJobResultImpl result = new DummyJobResultImpl();
				result.setResult("GOLA");
				((DummyJobImpl)job).setNodeJobResult(result);
				return job;
			}

			@Override
			public void onResponseReceive(Collection<NodeJob> result) {
				LOG.debug("Response received is:"+result);
				//return result;
			}

			@Override
			public void onNewNodesAdded(Collection<String> nodesLogicalNames) {
				LOG.debug("New nodes added are: "+nodesLogicalNames);
				
			}

			@Override
			public void onNodesRemoved(Collection<String> nodesLogicalNames) {
				LOG.debug("Nodes removed are :"+nodesLogicalNames);
			}
		});
		LOG.debug("Cluster started");
		while (true) {
			Thread.currentThread().sleep(2*1000);
			System.out.println(((JGroupsClusterImpl)cluster).getPrevView());
		}
		/*try {
			Thread.currentThread().sleep(24*60*60*1000);	//24 hours sleep
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
	}
}
