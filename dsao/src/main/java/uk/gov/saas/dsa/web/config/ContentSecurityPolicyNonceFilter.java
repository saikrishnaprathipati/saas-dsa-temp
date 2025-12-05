package uk.gov.saas.dsa.web.config;

import java.io.IOException;
import java.util.UUID;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.filter.GenericFilterBean;

public class ContentSecurityPolicyNonceFilter extends GenericFilterBean implements Filter {
	
	private final Logger csplogger = LogManager.getLogger(this.getClass());
	
	private static final String CSP_NONCE_ATTRIBUTE = "cspNonce";
	
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
		
		final String nonce;
		final Object existingNonce = req.getAttribute(CSP_NONCE_ATTRIBUTE);
		
		HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
		
		if (existingNonce == null) {
			nonce = UUID.randomUUID().toString();
	        request.setAttribute(CSP_NONCE_ATTRIBUTE, nonce);
	        csplogger.debug("Nonce value {}", nonce);
			
		} else {
			nonce = (String) existingNonce;
			csplogger.debug("Existing nonce value retained {}", nonce);
		}

        chain.doFilter(request, new CSPNonceResponseWrapper(response, nonce));
		
	}

    @Override
    public void afterPropertiesSet() {
        // initialization logic here (optional)
    }

	/**
     * Wrapper to fill the nonce value
     */
    public static class CSPNonceResponseWrapper extends HttpServletResponseWrapper {
        private String nonce;
 
        public CSPNonceResponseWrapper(HttpServletResponse response, String nonce) {
            super(response);
            this.nonce = nonce;
        }
 
        @Override
        public void setHeader(String name, String value) {
            if (name.equals("Content-Security-Policy") && StringUtils.isNotBlank(value)) {
                super.setHeader(name, value.replace("{nonce}", nonce));
            } else {
                super.setHeader(name, value);
            }
        }
 
        @Override
        public void addHeader(String name, String value) {
            if (name.equals("Content-Security-Policy") && StringUtils.isNotBlank(value)) {
                super.addHeader(name, value.replace("{nonce}", nonce));
            } else {
                super.addHeader(name, value);
            }
        }
    }

}
