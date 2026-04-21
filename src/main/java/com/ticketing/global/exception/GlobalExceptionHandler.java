package com.ticketing.global.exception;

import java.time.LocalDateTime;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {
	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e, HttpServletRequest request){
		ErrorCode errorCode = e.getErrorCode();
		
		ErrorResponse response = ErrorResponse.builder()
								.timestamp(LocalDateTime.now())
								.status(errorCode.getStatus().value())
								.code(errorCode.getCode())
								.message(errorCode.getMessage())
								.path(request.getRequestURI())
								.build();
		
		return ResponseEntity.status(errorCode.getStatus()).body(response);
	}
	
	 @ExceptionHandler(IllegalArgumentException.class)
     public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException e, HttpServletRequest request
     ) {
         ErrorResponse response = ErrorResponse.builder()
                 .timestamp(LocalDateTime.now())
                 .status(ErrorCode.INVALID_REQUEST.getStatus().value())
                 .code(ErrorCode.INVALID_REQUEST.getCode())
                 .message(e.getMessage())
                 .path(request.getRequestURI())
                 .build();

         return ResponseEntity.status(ErrorCode.INVALID_REQUEST.getStatus()).body(response);
     }

     @ExceptionHandler(Exception.class)
     public ResponseEntity<ErrorResponse> handleException(Exception e, HttpServletRequest request
     ) {
         ErrorResponse response = ErrorResponse.builder()
                 .timestamp(LocalDateTime.now())
                 .status(ErrorCode.INTERNAL_SERVER_ERROR.getStatus().value())
                 .code(ErrorCode.INTERNAL_SERVER_ERROR.getCode())
                 .message(ErrorCode.INTERNAL_SERVER_ERROR.getMessage())
                 .path(request.getRequestURI())
                 .build();

         return ResponseEntity.status(ErrorCode.INTERNAL_SERVER_ERROR.getStatus()).body(response);
     }
}
