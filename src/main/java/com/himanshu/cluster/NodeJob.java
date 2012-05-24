package com.himanshu.cluster;

import java.io.Serializable;

public interface NodeJob extends Serializable {
	String getJobId();
	JobType getJobType();
	void setJobStatus(JobStatus status);
	JobStatus getJobStatus();
	String getOperationType();
	enum JobStatus {
		SUBMIT, IN_PROGRESS, COMPLETED, FAIL, TIME_OUT, INVALID_TYPE
	}
	enum JobType {
		REQUEST, RESPONSE
	}
	NodeJobResult getNodeJobResult();
}
