package com.himanshu.cluster.impl;

import com.himanshu.cluster.NodeJobResult;

public class DummyJobResultImpl implements NodeJobResult {
	
	private Object result;

	public Object getResult() {
		return result;
	}

	public void setResult(Object result) {
		this.result = result;
	}

}
