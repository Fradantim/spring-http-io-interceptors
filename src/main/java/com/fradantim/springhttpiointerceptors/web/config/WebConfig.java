package com.fradantim.springhttpiointerceptors.web.config;

import java.util.Arrays;

import javax.servlet.Filter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import com.fradantim.springhttpiointerceptors.web.client.interceptor.HTTPIOInterceptor;
import com.fradantim.springhttpiointerceptors.web.filter.HTTPIOFilter;

@Configuration
public class WebConfig {

	@Value("${web.config.interceptable-urls}")
	public String[] interceptableUrls;

	@Bean
	public FilterRegistrationBean<Filter> filterRegistrationBean() {
		FilterRegistrationBean<Filter> registrationBean = new FilterRegistrationBean<Filter>();
		Filter httpioFilter = new HTTPIOFilter();

		registrationBean.setFilter(httpioFilter);
		registrationBean.addUrlPatterns(interceptableUrls);
		registrationBean.setOrder(0); // highest precedence
		return registrationBean;
	}

	@Bean
	@ConditionalOnExpression("${web.config.intercept-rest-client:false}")
	public RestTemplate getInterceptedRT() {
		// Buffered so the response can be read more than 1 time
		RestTemplate rt = new RestTemplate(new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory()));
		HTTPIOInterceptor interceptor = new HTTPIOInterceptor();
		rt.setInterceptors(Arrays.asList(interceptor));
		return rt;
	}

	@Bean
	@ConditionalOnMissingBean(RestTemplate.class)
	public RestTemplate getDefaultRT() {
		return new RestTemplate();
	}
}
