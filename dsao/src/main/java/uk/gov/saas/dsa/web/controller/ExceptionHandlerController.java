package uk.gov.saas.dsa.web.controller;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Date;

/**
 * Generic and specific error pages
 */
@Controller
public class ExceptionHandlerController implements ErrorController {

	public static final String DEFAULT_ERROR_VIEW = "error";
	public static final String NOT_FOUND_ERROR_VIEW = "error404";
	public static final String INTERNAL_SERVER_ERROR_VIEW = "error500";

	@RequestMapping("/error")
	public ModelAndView handleError(HttpServletRequest request, Exception e) {
		ModelAndView mav = new ModelAndView(DEFAULT_ERROR_VIEW);
		Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

		if (status != null) {
			int statusCode = Integer.parseInt(status.toString());
			if (statusCode == HttpStatus.NOT_FOUND.value()) {
				mav.setViewName(NOT_FOUND_ERROR_VIEW);
				return mav;
			} else if (statusCode >= 500 && statusCode <= 599) {
				mav.setViewName(INTERNAL_SERVER_ERROR_VIEW);
				return mav;
			}
		}

		mav.addObject("datetime", new Date());
		mav.addObject("exception", e);
		mav.addObject("url", request.getRequestURL());

		return mav;
	}
}
