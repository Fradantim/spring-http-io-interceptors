package com.fradantim.springhttpiointerceptors.web.filter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.function.Function;

import javax.servlet.FilterChain;
import javax.servlet.ReadListener;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletResponse;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;

public class HTTPIOFilter extends OncePerRequestFilter{
	
	private static Logger logger = LoggerFactory.getLogger(HTTPIOFilter.class); 

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		BufferedRequestWrapper bufferedRequest = new BufferedRequestWrapper(request);
		
		printRequestInfo(request,bufferedRequest);

		final ByteArrayPrintWriter printWriter = new ByteArrayPrintWriter();
		HttpServletResponse wrappedResponse = wrapResponse(printWriter, response);
		
		// execute next filters / call endpoints
		filterChain.doFilter(bufferedRequest, wrappedResponse);

		HasBuffer responseBufferHolder = getBodyBufferHolder(printWriter,response);			
		printResponseInfo(wrappedResponse, responseBufferHolder);
	}

	
	private void printRequestInfo(HttpServletRequest httpServletRequest, HasBuffer hasBuffer) {
		StringBuilder request = new StringBuilder();
		
		try {
			request.append("INBOUND REQUEST :\n");
			request.append("\tURI     : " + httpServletRequest.getRequestURL().toString() + "\n");
			request.append("\tQuery   : " + httpServletRequest.getQueryString() + "\n");
			request.append("\tMethod  : " + httpServletRequest.getMethod() + "\n");
			request.append("\tHeaders : " + getHeaders(httpServletRequest) + "\n");
			request.append("\tBody    : " + buffer2String(hasBuffer));
    	} catch (Exception e) {
    		request.append("\tIRRECUPERABLE : " + e.getMessage());
    	}
		
		logger.info(request.toString());
	}
	
	private HasBuffer getBodyBufferHolder(ByteArrayPrintWriter pw, ServletResponse response) throws IOException {
		final byte[] bytes = pw.toByteArray();
		response.getOutputStream().write(bytes);
		return new HasBuffer() {
			public byte[] getBuffer() { return bytes; }
		};
	}

	private void printResponseInfo(HttpServletResponse servletResponse, HasBuffer hasBuffer) {
		StringBuilder resposne = new StringBuilder();
		
		try {
			resposne.append("INBOUND RESPONSE :\n");
			resposne.append("\tStatus  : " + servletResponse.getStatus() + "\n");
			resposne.append("\tHeaders : " + getHeaders(servletResponse) + "\n");
			resposne.append("\tBody    : " + buffer2String(hasBuffer));
    	} catch (Exception e) {
    		resposne.append("\tIRRECUPERABLE : " + e.getMessage());
    	}
		
		logger.info(resposne.toString());
	}	
		
	private static String buffer2String(HasBuffer hasBuffer) {
		if(hasBuffer == null || hasBuffer.getBuffer()==null) {
			return "null";
		}
		return new String(hasBuffer.getBuffer());
	}
	
	/** @see #getHeaders(Collection, Function) */
	private static String getHeaders(HttpServletRequest httpServletRequest) {
		Enumeration<String> headerNamesEnum = httpServletRequest.getHeaderNames();
		Collection<String> headerNames = new ArrayList<>();
		
		while(headerNamesEnum.hasMoreElements()) {
			headerNames.add(headerNamesEnum.nextElement());
		}
		
		return getHeaders(headerNames, httpServletRequest::getHeader);
	}
	
	/** @see #getHeaders(Collection, Function) */ 
	private static String getHeaders(HttpServletResponse httpServletResponse) {
		return getHeaders(httpServletResponse.getHeaderNames(), httpServletResponse::getHeader);
	}
	
	/** Returns a String with the format: "{key1=( valuek1a, valuek1b ), key2=( valuek2a, valuek2b ), (...) }" */ 
	private static String getHeaders(Collection<String> headerNames, Function<String, String> headerValueProvider) {
		StringBuilder result = new StringBuilder(); 
		result.append("{");
		
		int count = 0;
		for(String headerName: headerNames) {
			count ++;
			result.append(headerName);
			
			String headerValue = headerValueProvider.apply(headerName);
			
			if(headerValue != null && !headerValue.isEmpty()) {
				result.append("=( ");
				result.append(headerValue);
				result.append(" )");
			}
			
			if(count != headerNames.size()) {
				result.append(", ");
			}
		}

		result.append("}");
		
		return result.toString();
	}

	/* --- Internal classes to wrap both request y response ---*/
	
	private static HttpServletResponse wrapResponse(final ByteArrayPrintWriter pw , HttpServletResponse response) {
		return new HttpServletResponseWrapper(response) {
			public PrintWriter getWriter() {
				return pw.getWriter();
			}

			public ServletOutputStream getOutputStream() {
				return pw.getStream();
			}

		};
	}
	
	private static class ByteArrayServletStream extends ServletOutputStream {

		private ByteArrayOutputStream baos;

		ByteArrayServletStream(ByteArrayOutputStream baos) {
			this.baos = baos;
		}

		public void write(int param) throws IOException {
			baos.write(param);
		}

		@Override
		public boolean isReady() { return false;}

		@Override
		public void setWriteListener(WriteListener writeListener) { }
	}

	private static class ByteArrayPrintWriter {

		private ByteArrayOutputStream baos = new ByteArrayOutputStream();
		private PrintWriter pw = new PrintWriter(baos);
		private ServletOutputStream sos = new ByteArrayServletStream(baos);

		public PrintWriter getWriter() {
			return pw;
		}

		public ServletOutputStream getStream() {
			return sos;
		}

		byte[] toByteArray() {
			return baos.toByteArray();
		}
	}

	private static class BufferedServletInputStream extends ServletInputStream {

		private ByteArrayInputStream bais;

		public BufferedServletInputStream(ByteArrayInputStream bais) {
			this.bais = bais;
		}

		public int available() { 
			return bais.available();
		}

		public int read() {
			return bais.read();
		}

		public int read(byte[] buf, int off, int len) {
			return bais.read(buf, off, len);
		}

		@Override
		public boolean isFinished() { return false; }

		@Override
		public boolean isReady() { return false; }

		@Override
		public void setReadListener(ReadListener readListener) { }

	}
	
	private static interface HasBuffer{
		public byte[] getBuffer();
	}

	private static class BufferedRequestWrapper extends HttpServletRequestWrapper implements HasBuffer {

		private ByteArrayInputStream bais;
		private ByteArrayOutputStream baos;
		private BufferedServletInputStream bsis;

		byte[] buffer;

		public BufferedRequestWrapper(HttpServletRequest req) throws IOException {
			super(req);
			InputStream is = req.getInputStream();
			baos = new ByteArrayOutputStream();
			byte buf[] = new byte[1024];
			int letti;
			while ((letti = is.read(buf)) > 0) {
				baos.write(buf, 0, letti);
			}
			buffer = baos.toByteArray();
		}

		public ServletInputStream getInputStream() {
			try {
				bais = new ByteArrayInputStream(buffer);
				bsis = new BufferedServletInputStream(bais);
			} catch (Exception ex) {
				// e?
			}

			return bsis;
		}

		public byte[] getBuffer() {
			return buffer;
		}
	}
}
