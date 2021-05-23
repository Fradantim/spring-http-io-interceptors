package com.fradantim.springhttpiointerceptors.web.rest.resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fradantim.springhttpiointerceptors.service.RemoteServiceExample;

@RestController()
@RequestMapping(SimpleResource.MAPPING)
public class SimpleResource {
	
	public static final String MAPPING = "/simple-resource";

	@Autowired
	private RemoteServiceExample remoteService;
	
	@GetMapping()
	public String getExample() {
		remoteService.callGetService();
		return "You sent me a GET HTTP request!";
	}
	
	@PostMapping()
	public String postExample() {
		remoteService.callPostService();
		return "You sent me a POST HTTP request!";
	}
}
