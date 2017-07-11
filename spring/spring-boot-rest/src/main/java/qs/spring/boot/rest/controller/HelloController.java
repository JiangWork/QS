package qs.spring.boot.rest.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/hello")
public class HelloController {
	
	@RequestMapping("/china")
	public String speakChinese() {
		return "你好！";
	}
	
	@RequestMapping("/english")
	public String speakEnglish() {
		return "hello!";
	}
}
