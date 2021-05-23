package com.fradantim.springhttpiointerceptors.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fradantim.springhttpiointerceptors.service.PetStoreService;

@Service
public class PetStoreServiceImpl implements PetStoreService{

	@Value("${swagger.petstore.url}")
	private String swaggerPetstoreUrl;
	
	@Autowired
	private RestTemplate restTemplate;
	
	@Override
	public void callGetService() {
		restTemplate.getForEntity(swaggerPetstoreUrl+"/store/inventory", Object.class);
	}

	@Override
	public void callPostService() {
		Map<String, Object> body = new HashMap<>();
		body.put("category", new HashMap<String, Object>() {
			private static final long serialVersionUID = 1L;
			{
				put("id", 0);
				put("name", "string");
			}
		});
		
		body.put("name", "doggie");
		body.put("photoUrls", new String [] {"string"});
		
		body.put("status", "available");
		
		restTemplate.postForEntity(swaggerPetstoreUrl+"/pet", body, Object.class);
		
	}

	
}
