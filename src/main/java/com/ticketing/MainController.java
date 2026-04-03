package com.ticketing;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.ticketing.concert.entity.Concert;
import com.ticketing.concert.service.ConcertService;

import lombok.RequiredArgsConstructor;



@Controller
@RequiredArgsConstructor
public class MainController {
	
 	private final ConcertService concertService;
	@GetMapping("/")
	public String home(Model model) {
		List<Concert> list = concertService.findAllWithSchedules();
		model.addAttribute("concertList", list);
		return "index";
	}
}
