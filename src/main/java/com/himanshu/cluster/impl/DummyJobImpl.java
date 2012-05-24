package com.himanshu.cluster.impl;

import org.jgroups.Address;

import com.himanshu.cluster.NodeJob;
import com.himanshu.cluster.NodeJobResult;

public class DummyJobImpl implements NodeJob {
	
	private JobType jobType;
	private JobStatus jobStatus;
	private String jobId;
	private NodeJobResult nodeJobResult;
	private String operationType;
	
	public String getOperationType() {
		return operationType;
	}
	public void setOperationType(String operationType) {
		this.operationType = operationType;
	}
	public NodeJobResult getNodeJobResult() {
		return nodeJobResult;
	}
	public void setNodeJobResult(NodeJobResult nodeJobResult) {
		this.nodeJobResult = nodeJobResult;
	}
	public String getJobId() {
		return jobId;
	}
	public void setJobId(String jobId) {
		this.jobId = jobId;
	}
	public JobType getJobType() {
		return jobType;
	}
	public void setJobType(JobType jobType) {
		this.jobType = jobType;
	}
	public JobStatus getJobStatus() {
		return jobStatus;
	}
	public void setJobStatus(JobStatus jobStatus) {
		this.jobStatus = jobStatus;
	}

}
