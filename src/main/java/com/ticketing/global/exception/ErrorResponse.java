package com.ticketing.global.exception;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ErrorResponse {
	private LocalDateTime timestamp;
	private int status;
	private String code;
	private String message;
	private String path;
		
}
