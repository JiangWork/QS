package qs.spring.boot.rest;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class MyStartupRunner implements CommandLineRunner {

	@Override
	public void run(String... args) throws Exception {
		System.out.println(this.getClass().getCanonicalName() + ": running CommandLineRunner.");
	}

}
