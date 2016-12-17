package com.github.wtekiela.opensub4j.parser;

import com.github.wtekiela.opensub4j.response.Status;

public class StatusBuilder extends AbstractResponseObjectBuilder<Status> {

	private String status;

	public void setStatus(String status) {
		this.status = status;
	}

	@Override
	public Status build() {
		if (status == null) {
			throw new IllegalStateException("Status must be initialized!");
		}
		return new Status(status);
	}

}
