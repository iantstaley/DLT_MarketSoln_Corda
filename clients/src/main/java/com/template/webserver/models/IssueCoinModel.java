package com.template.webserver.models;

import com.fasterxml.jackson.annotation.JsonProperty;


public class IssueCoinModel {
	private final String accountName;
	private final double value;

	public IssueCoinModel(@JsonProperty("accountName") String accountName,
						  @JsonProperty("value") double value) {
		this.accountName = accountName;
		this.value = value;
	}

	public String getAccountName() {
		return accountName;
	}

	public double getValue() {
		return value;
	}
}