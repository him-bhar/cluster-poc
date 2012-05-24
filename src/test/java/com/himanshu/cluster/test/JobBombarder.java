package com.himanshu.cluster.test;

import java.util.Timer;
import java.util.TimerTask;

import com.himanshu.cluster.Cluster;
import com.himanshu.cluster.NodeJob.JobStatus;
import com.himanshu.cluster.NodeJob.JobType;
import com.himanshu.cluster.impl.DummyJobImpl;

public class JobBombarder {
	private Cluster cluster;
	public Cluster getCluster() {
		return cluster;
	}
	public void setCluster(Cluster cluster) {
		this.cluster = cluster;
	}
	
	public void startBombing() {
		TimerTask task = new TimerTask() {
			
			@SuppressWarnings("deprecation")
			@Override
			public void run() {
				DummyJobImpl job = new DummyJobImpl();
				job.setJobStatus(JobStatus.SUBMIT);
				job.setJobType(JobType.REQUEST);
				job.setOperationType("DUMMY");
				try {
					getCluster().submitJob(job);
					//getCluster().broadcastJob(job);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		
		Timer timer = new Timer();
		timer.schedule(task, 10, 5000);
	}
	
}
