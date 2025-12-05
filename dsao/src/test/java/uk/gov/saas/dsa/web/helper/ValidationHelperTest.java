package uk.gov.saas.dsa.web.helper;

import static uk.gov.saas.dsa.web.helper.AllowancesHelper.POST_CODE_REGEX;
import static uk.gov.saas.dsa.web.helper.ValidationHelper.matches;

import java.util.regex.Pattern;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ValidationHelperTest {

	@BeforeEach
	void setUp() {
	}

	@Test
	void invalidPostcodes() {
		boolean emptyPostCode = matches(Pattern.compile(POST_CODE_REGEX), " ");
		boolean specialChars = matches(Pattern.compile(POST_CODE_REGEX), "EH54%6TH");
		boolean moreThanEightChars = matches(Pattern.compile(POST_CODE_REGEX), "EH54 6THH");
		boolean allNumbers = matches(Pattern.compile(POST_CODE_REGEX), "12345678");
		boolean allAlphabets = matches(Pattern.compile(POST_CODE_REGEX), "abcdefgh");
		boolean allSpaces = matches(Pattern.compile(POST_CODE_REGEX), "        ");
		boolean randomText = matches(Pattern.compile(POST_CODE_REGEX), "qwqwqwqw");
		boolean postcodeWithSpace = matches(Pattern.compile(POST_CODE_REGEX), "EH54 6TH");
		boolean postcodeWithoutSpace = matches(Pattern.compile(POST_CODE_REGEX), "EH546TH");
		boolean sixCasePostcode = matches(Pattern.compile(POST_CODE_REGEX), "BD7 3BG");
		boolean sixCasePostcodeNoSpace = matches(Pattern.compile(POST_CODE_REGEX), "BD73BG");
		boolean edinBurgh = matches(Pattern.compile(POST_CODE_REGEX), "EH11AD");
		boolean postcodeFveChars = matches(Pattern.compile(POST_CODE_REGEX), "l1 1rd");
		boolean postcodeTailingSpaces = matches(Pattern.compile(POST_CODE_REGEX), "l1 1rd ");

		Assertions.assertFalse(emptyPostCode, "emptyPostCode");
		Assertions.assertFalse(specialChars, "specialChars");
		Assertions.assertFalse(moreThanEightChars, "moreThanEightChars");
		Assertions.assertFalse(allNumbers, "allNumbers");
		Assertions.assertFalse(allAlphabets, "allAlphabets");
		Assertions.assertFalse(allSpaces, "allSpaces");
		Assertions.assertFalse(randomText, "randomText");
		Assertions.assertFalse(postcodeTailingSpaces, "postcodeTailingSpaces");
		Assertions.assertTrue(postcodeWithoutSpace, "postcodeWithoutSpace");
		Assertions.assertTrue(postcodeWithSpace, "postcodeWithSpace");
		Assertions.assertTrue(sixCasePostcode, "sixCasePostcode");
		Assertions.assertTrue(sixCasePostcodeNoSpace, "sixCasePostcodeNoSpace");
		Assertions.assertTrue(edinBurgh, "edinBurgh");
		Assertions.assertTrue(postcodeFveChars, "postcodeFveChars");
	}

}
