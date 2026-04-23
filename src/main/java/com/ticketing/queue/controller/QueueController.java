package com.ticketing.queue.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/queue")
public class QueueController {
	
	@GetMapping("/queue/{scheduleNo}")
	public String queuePage(@PathVariable("scheduleNo") Long scheduleNo, Model model) {
		model.addAttribute("scheduleNo", scheduleNo);
		
		return "queue/queue";
	}
}
