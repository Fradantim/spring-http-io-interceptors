package com.fradantim.springhttpiointerceptors.web.client.interceptor;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StreamUtils;

public class HTTPIOInterceptor implements ClientHttpRequestInterceptor {
	
	private static Logger logger = LoggerFactory.getLogger(HTTPIOInterceptor.class);
	
	@Override
	public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
			throws IOException {
		printRequestInfo(request, body);
		
		// execute HTTP request
        ClientHttpResponse response = execution.execute(request, body);
        
        printResponseInfo(response);
        return response;
	}
	
	protected void printRequestInfo(HttpRequest httpRequest, byte[] body) {
    	StringBuilder request = new StringBuilder();
		try {
			request.append("OUTBOND REQUEST :\n");
			request.append("\tURI     : " + getURIWithoutQueryParams(httpRequest.getURI()) + "\n");
			request.append("\tQuery   : " + httpRequest.getURI().getQuery() + "\n");
			request.append("\tMethod  : " + httpRequest.getMethod() + "\n");
			request.append("\tHeaders : " + headersToString(httpRequest.getHeaders()) + "\n");
			request.append("\tBody    : " + new String(body, "UTF-8") + "\n");
    	} catch (Exception e) {
    		request.append("\tIRRECUPERABLE : " + e.getMessage());
    	}
		
		logger.info(request.toString());
    }
 
    protected void printResponseInfo(ClientHttpResponse clientHttpResponse) throws IOException {
    	StringBuilder response = new StringBuilder();
    	try {
    		response.append("OUTBOND RESPONSE :\n");
    		response.append("\tStatus code  : " + clientHttpResponse.getStatusCode() + "\n");
    		response.append("\tStatus text  : " + clientHttpResponse.getStatusText() + "\n");
    		response.append("\tHeaders      : " + headersToString(clientHttpResponse.getHeaders()) + "\n");
    		response.append("\tResponse body: " + StreamUtils.copyToString(clientHttpResponse.getBody(), Charset.defaultCharset()));
    	} catch (Exception e) {
    		response.append("\tIRRECUPERABLE : "+e.getMessage());
    	}
    	
    	logger.info(response.toString());
    }
    
    /** Returns a String with the format: "{key1=( valuek1a, valuek1b ), key2=( valuek2a, valuek2b ), (...) }" */
    private static String headersToString(HttpHeaders headers) {
    	StringBuilder result = new StringBuilder(); 
		result.append("{");
		
		int count = 0;
    	for(Entry<String, List<String>> entry :  headers.entrySet()) {
    		count ++;
    		result.append(entry.getKey());
    		
    		String headerValue = String.join(", ", entry.getValue());
			
			if(headerValue != null && !headerValue.isEmpty()) {
				result.append("=( ");
				result.append(headerValue);
				result.append(" )");
			}
			
			if(count != headers.size()) {
				result.append(", ");
			}
    	}
    	
    	result.append("}");
		
		return result.toString();
    	
    }
    
	private static String getURIWithoutQueryParams(URI uri) throws URISyntaxException {
		return new URI(uri.getScheme(),
                uri.getAuthority(),
                uri.getPath(),
                null, // Ignore the query part of the input url
                uri.getFragment()).toString();
	}
}
