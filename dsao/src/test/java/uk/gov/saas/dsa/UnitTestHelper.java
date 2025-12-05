package uk.gov.saas.dsa;

import java.util.ArrayList;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.servlet.ModelAndView;

import uk.gov.saas.dsa.domain.readonly.Stud;
import uk.gov.saas.dsa.domain.readonly.StudCourseYear;
import uk.gov.saas.dsa.persistence.readonly.StudRepository;
import uk.gov.saas.dsa.service.FindStudentService;
import uk.gov.saas.dsa.vo.StudentCourseYearVO;
import uk.gov.saas.dsa.vo.StudentResultVO;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;

public class UnitTestHelper {
	private final static Logger logger = LogManager.getLogger(UnitTestHelper.class);

	public static final String FIRST_NAME = "FIRST_NAME";
	public static final long DSA_APPLICATION_NO = 1l;
	public static final long STUDENT_REFERENCE_NUMBER = 12222L;
	public static final String BLAH_ACTION = "blahAction";
	public static final ResultMatcher IS3XX_REDIRECTION = MockMvcResultMatchers.status().is3xxRedirection();
	public static final ResultMatcher IS2XX_SUCCESSFUL = MockMvcResultMatchers.status().is2xxSuccessful();

	public static ModelAndView performPostMap(MockMvc mockMvc, String path, Map<String, Object> flashMap,
			Map<String, String> paramMap, ResultMatcher resultMatcher) throws Exception {
		MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post("/" + path);

		if (flashMap != null) {
			for (String key : flashMap.keySet()) {
				Object object = flashMap.get(key);
				builder.flashAttr(key, object);
			}
		}
		if (paramMap != null) {
			for (String key : paramMap.keySet()) {
				String value = paramMap.get(key);
				builder.param(key, value);
			}
		}
		ResultActions resultActions = mockMvc.perform(builder);

		resultActions.andExpect(resultMatcher);

		MvcResult mvcResult = resultActions.andReturn();

		ModelAndView modelAndView = mvcResult.getModelAndView();

		return modelAndView;
	}

	public static boolean hasKeyValue(ModelAndView modelAndView, String key, String value) {
		Map<String, Object> modelMap = modelAndView.getModel();

		Object object = modelMap.get(key);
		return object.toString().contains(value);

	}

	public static boolean hasValue(ModelAndView modelAndView, String value) {
		Map<String, Object> modelMap = modelAndView.getModel();
		logger.info("model Map" + modelMap);
		logger.info("model Map Values: " + modelMap.values());
		return modelMap.values().stream().anyMatch(t -> t.toString().contains(value));

	}

	public static void mockStudentFirstName(FindStudentService findStudentService) throws Exception {
		StudentResultVO studentResultVO = new StudentResultVO();
		studentResultVO.setFirstName(FIRST_NAME);
		studentResultVO.setStudentReferenceNumber(STUDENT_REFERENCE_NUMBER);
		StudentCourseYearVO studentCourseYearVO = new StudentCourseYearVO();
		studentCourseYearVO.setAcademicYearFull("2000 to 2001");
		studentResultVO.setStudentCourseYear(studentCourseYearVO);

		Mockito.when(findStudentService.findByStudReferenceNumber(STUDENT_REFERENCE_NUMBER)).thenReturn(studentResultVO);
		Mockito.when(findStudentService.findByStudReferenceNumber(STUDENT_REFERENCE_NUMBER, 0)).thenReturn(studentResultVO);
	}
}

