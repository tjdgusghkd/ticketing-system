package com.ticketing.queue.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class QueueEnterResponseDto {
	private boolean allowed;
	private int rank;
}
