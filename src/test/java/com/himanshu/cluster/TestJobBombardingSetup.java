package com.himanshu.cluster;

import java.util.Collection;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;

import com.himanshu.cluster.Cluster;
import com.himanshu.cluster.NodeJob;
import com.himanshu.cluster.NodeJob.JobType;
import com.himanshu.cluster.impl.DummyJobImpl;
import com.himanshu.cluster.impl.DummyJobResultImpl;
import com.himanshu.cluster.impl.JGroupsClusterImpl;

public class TestJobBombardingSetup {
	static Logger LOG = LoggerFactory.getLogger(TestJobBombardingSetup.class);
	private XmlBeanFactory ctx = null; 
	private String CLUSTER_BEAN_NAME = "cluster";
	private String JOB_BOMBARDER_BEAN_NAME = "jobBombarder";
	
	@Before
	public void initMethod() {
		System.setProperty("com.iperia.vx.mg.cluster.name", "himanshu");
		org.springframework.core.io.Resource xmlRes = new ClassPathResource("/mg-cluster.xml");
		ctx = new XmlBeanFactory(xmlRes);
		PropertyPlaceholderConfigurer cfg = new PropertyPlaceholderConfigurer();
		cfg.setProperties(System.getProperties());
		cfg.postProcessBeanFactory(ctx);
	}
	
	@Test
	public void test() {
		Cluster cluster = (JGroupsClusterImpl)ctx.getBean(CLUSTER_BEAN_NAME);
		cluster.registerListener(new Cluster.Listener() {
			
			@Override
			public void onMasterReelection(String masterAddress, String currentNodeAddress) {
				LOG.debug("Master Re-election done");
			}
			
			@Override
			public NodeJob onJobReceive(NodeJob job) {
				//job = null;
				if (job.getJobType().equals(JobType.REQUEST)) {
					if (job.getOperationType() != null) {
						if (job.getOperationType().equalsIgnoreCase("DUMMY")) {
							LOG.debug("Executing the DUMMY job");
							DummyJobResultImpl result = new DummyJobResultImpl();
							result.setResult("HOLA");
							((DummyJobImpl)job).setNodeJobResult(result);
						} else {
							LOG.error("Operation Type unknown");
						}
					} else {
						LOG.error("Operation type not available");
					}
				} else {
					LOG.error("Job Type unknown");
				}
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
		ctx.getBean(JOB_BOMBARDER_BEAN_NAME);
		LOG.debug("Cluster started");
		try {
			Thread.currentThread().sleep(24*60*60*1000);	//24 hours sleep
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@After
	public void cleanup() {
		
	}

}
