/**
 * Copyright © 2016 VMware, Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the “License”); you may not 
 * use this file except in compliance with the License. You may obtain a copy of 
 * the License at http://www.apache.org/licenses/LICENSE-2.0
 * Some files may be comprised of various open source software components, each of which
 * has its own license that is located in the source code of the respective component.
 */
package com.vmware.loginsightapi.core;

public class AggregateResponseBin {
	
	private Long minTimestamp;
	private Long maxTimestamp;
	private Long value;
	public Long getMinTimestamp() {
		return minTimestamp;
	}
	public void setMinTimestamp(Long minTimestamp) {
		this.minTimestamp = minTimestamp;
	}
	public Long getMaxTimestamp() {
		return maxTimestamp;
	}
	public void setMaxTimestamp(Long maxTimestamp) {
		this.maxTimestamp = maxTimestamp;
	}
	public Long getValue() {
		return value;
	}
	public void setValue(Long value) {
		this.value = value;
	}

}
