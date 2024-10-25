package com.voxloud.provisioning.exception;

import lombok.Data;

@Data
public class ErrorResponse {

	private String errorPlaceInfo;
	private String code;
	private String cause;
}
