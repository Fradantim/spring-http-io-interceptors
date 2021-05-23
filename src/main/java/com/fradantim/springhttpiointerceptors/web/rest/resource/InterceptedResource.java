package com.fradantim.springhttpiointerceptors.web.rest.resource;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController()
@RequestMapping(InterceptedResource.MAPPING)
public class InterceptedResource {
	
	public static final String MAPPING = "/intercepted-resource";

	private static final String MESSAGE = "I'm an intercepted resource, you can see the request i received and this response I sent you int the app's log.";
	
	@GetMapping()
	public String getExample() {
		return "You sent me a GET HTTP request! "+MESSAGE;
	}
	
	@PostMapping()
	public String postExample() {
		return "You sent me a POST HTTP request! "+MESSAGE;
	}
}
