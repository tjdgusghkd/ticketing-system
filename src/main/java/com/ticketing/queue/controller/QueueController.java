package com.ticketing.queue.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/queue")
public class QueueController {
	
	@GetMapping("/queue")
	public String queuePage() {
		return "queue/queue";
	}
}
