# Spring HTTP IO Interceptors
> This may one day save your life... or not, I don't know what I'm talking about.
> 
> \- Fradantim

<br />

This is a simple project to understand how to intercept both inbound and outbound http messages and show the requests and responses contents without adding extra business logic.

<br />

# Table of contents

- [Spring HTTP IO Interceptors](#spring-http-io-interceptors)
- [Table of contents](#table-of-contents)
- [Try!](#try)
  - [Inbound messages](#inbound-messages)
  - [Inbound messages - GET](#inbound-messages---get)
  - [Inbound messages - POST](#inbound-messages---post)
  - [Outbound messages](#outbound-messages)
  - [Outbound messages - GET](#outbound-messages---get)
  - [Outbound messages - POST](#outbound-messages---post)
- [How?](#how)
- [Not production-ready](#not-production-ready)

<br />

# Try!

<br />

## Inbound messages

Incomming messages to `/intercepted-resource` are being intercepted and logged.

<br />

## Inbound messages - GET

<br />

Request
```bash
curl -X 'GET' 'http://localhost:8080/intercepted-resource?foo=bar&faa=bor'
```
Response
```
You sent me a GET HTTP request! I'm an intercepted resource, you can see the request i received and this response I sent you int the app's log.
```
log:
```
2021-05-23 17:34:12.444  INFO 32632 --- [nio-8080-exec-3] c.f.s.web.filter.HTTPIOFilter            : INBOUND REQUEST :
	URI     : http://localhost:8080/intercepted-resource
	Query   : foo=bar&faa=bor
	Method  : GET
	Headers : {host=( localhost:8080 ), user-agent=( curl/7.58.0 ), accept=( */* )}
	Body    : 
2021-05-23 17:34:12.446  INFO 32632 --- [nio-8080-exec-3] c.f.s.web.filter.HTTPIOFilter            : INBOUND RESPONSE :
	Status  : 200
	Headers : {}
	Body    : You sent me a GET HTTP request! I'm an intercepted resource, you can see the request i received and this response I sent you int the app's log.
```

<br />


## Inbound messages - POST

<br />

Request
```bash
curl -X 'POST' 'http://localhost:8080/intercepted-resource' -d '{ "msg":"This is my request body" }'
```
Response
```
You sent me a POST HTTP request! I'm an intercepted resource, you can see the request i received and this response I sent you int the app's log.
```
log:
```
2021-05-23 17:41:54.469  INFO 32632 --- [nio-8080-exec-7] c.f.s.web.filter.HTTPIOFilter            : INBOUND REQUEST :
	URI     : http://localhost:8080/intercepted-resource
	Query   : null
	Method  : POST
	Headers : {host=( localhost:8080 ), user-agent=( curl/7.58.0 ), accept=( */* ), content-length=( 34 ), content-type=( application/x-www-form-urlencoded )}
	Body    : { "msg":"This is my request body" }
2021-05-23 17:41:54.469  INFO 32632 --- [nio-8080-exec-7] c.f.s.web.filter.HTTPIOFilter            : INBOUND RESPONSE :
	Status  : 200
	Headers : {}
	Body    : You sent me a POST HTTP request! I'm an intercepted resource, you can see the request i received and this response I sent you int the app's log.
```

<br />

---

<br />

## Outbound messages

Incomming messages to `/simple-resource` are not being intercepted nor logged, but will perform an http request with the same http-method to a remote service (currently only get and post), which will be intercepted and logged.

<br />

## Outbound messages - GET
```bash
curl -X 'GET' 'http://localhost:8080/simple-resource'
```
Response
```
You sent me a GET HTTP request!
```
log:
```
2021-05-23 17:56:24.213  INFO 32632 --- [nio-8080-exec-1] c.f.s.w.c.interceptor.HTTPIOInterceptor  : OUTBOND REQUEST :
	URI     : https://petstore.swagger.io/v2/store/inventory
	Query   : null
	Method  : GET
	Headers : {Accept=( application/json, application/*+json ), Content-Length=( 0 )}
	Body    : 

2021-05-23 17:56:24.737  INFO 32632 --- [nio-8080-exec-1] c.f.s.w.c.interceptor.HTTPIOInterceptor  : OUTBOND RESPONSE :
	Status code  : 200 OK
	Status text  : OK
	Headers      : {Date=( Sun, 23 May 2021 20:56:24 GMT ), Content-Type=( application/json ), Transfer-Encoding=( chunked ), Connection=( keep-alive ), Access-Control-Allow-Origin=( * ), Access-Control-Allow-Methods=( GET, POST, DELETE, PUT ), Access-Control-Allow-Headers=( Content-Type, api_key, Authorization ), Server=( Jetty(9.2.9.v20150224) )}
	Response body: {"sold":20,"string":328,"pending":8,"available":572,"UPDATED STATUS":6,"new status":1}
```

<br />

## Outbound messages - POST

<br />

```bash
curl -X 'POST' 'http://localhost:8080/simple-resource'
```
Response
```
You sent me a POST HTTP request!
```
log:
```
2021-05-23 17:57:10.780  INFO 32632 --- [nio-8080-exec-3] c.f.s.w.c.interceptor.HTTPIOInterceptor  : OUTBOND REQUEST :
	URI     : https://petstore.swagger.io/v2/pet
	Query   : null
	Method  : POST
	Headers : {Accept=( application/json, application/*+json ), Content-Type=( application/json ), Content-Length=( 97 )}
	Body    : {"photoUrls":["string"],"name":"doggie","category":{"name":"string","id":0},"status":"available"}

2021-05-23 17:57:11.286  INFO 32632 --- [nio-8080-exec-3] c.f.s.w.c.interceptor.HTTPIOInterceptor  : OUTBOND RESPONSE :
	Status code  : 200 OK
	Status text  : OK
	Headers      : {Date=( Sun, 23 May 2021 20:57:11 GMT ), Content-Type=( application/json ), Transfer-Encoding=( chunked ), Connection=( keep-alive ), Access-Control-Allow-Origin=( * ), Access-Control-Allow-Methods=( GET, POST, DELETE, PUT ), Access-Control-Allow-Headers=( Content-Type, api_key, Authorization ), Server=( Jetty(9.2.9.v20150224) )}
	Response body: {"id":9223127516080552807,"category":{"id":0,"name":"string"},"name":"doggie","photoUrls":["string"],"tags":[],"status":"available"}
```

<br />

# How?
[HTTPIOFilter.java](src/main/java/com/fradantim/springhttpiointerceptors/web/filter/HTTPIOFilter.java) is a classic Java Filter implementation which takes care of incomming messages.

[HTTPIOInterceptor.java](src/main/java/com/fradantim/springhttpiointerceptors/web/client/interceptor/HTTPIOInterceptor.java) is a Spring ClientHttpRequestInterceptor and ResponseErrorHandler implementation which takes care of outgoing messages sent by a RestTemplate.

Both of these are configured in the [WebConfig.java](src/main/java/com/fradantim/springhttpiointerceptors/web/config/WebConfig.java) file taking arguments from the [application.properties](src/main/resources/application.properties) file:
```java
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
		RestTemplate rt = new RestTemplate();
		HTTPIOInterceptor interceptor = new HTTPIOInterceptor();
		rt.setInterceptors(Arrays.asList(interceptor));
		rt.setErrorHandler(interceptor);
		return rt;
	}

	@Bean
	@ConditionalOnMissingBean(RestTemplate.class)
	public RestTemplate getDefaultRT() {
		return new RestTemplate();
	}
}
```

<br />

# Not production-ready
No pa, currently both requests and reponses are beingh sent directly to the console output, this allow CRLF inyection. If something
```
s̶̥̗͛̀̆̎͋͆͌͛̋͛͋̀̎͛p̷̛̗͕̩̝̰̰̫̆́́͝o̴͉̭̾̔̓͗̅͂̅̍͗̑͗͘͝ö̸̧̨͎̪̗̦̱̳̺̤͉̫͔́̿̃̑̂͋̂̄̕͜͝k̸̢̧̠͉̳͍̰̯̅̈̋͆͒͗͛͗́͝y̶̛̭̽̌͋̆̐͒͛̆̇͋̂ ̷̧̟̬̬̲̺̼͇͍̹̳͈̇̔͜ͅs̷̗̯̃̌͛̊̋͑͘ç̴̡̞̰͍̣̩̮̠̥̫͒͌͒̓̉͑͘ǎ̵̧̢̢̭̮̥͖͚̔r̵̥̹͉͙̰̺̻̻̱͎͇̎̈́̏͆̽̓̌́̒̽̉͊̎̚͜͠y̶̧̨̧̢͕̠̫̳̥̜̪̳̼̐̓̀̂̉̓̑̚̚͘͜͠
``` 
comes either in one of the bodies or headers it will be sent directly to the console. It's an internal fight between showing what truly cames-and-goes and allow vulnerabilities, or normalizing all data and show a lie, choose your poison.

<br />

---

<br />

Fradantim 2021