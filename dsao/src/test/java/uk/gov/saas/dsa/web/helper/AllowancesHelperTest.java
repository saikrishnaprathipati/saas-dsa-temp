package uk.gov.saas.dsa.web.helper;

import static org.junit.jupiter.api.Assertions.*;

class AllowancesHelperTest {

	public static void main(String[] args) {
		assertEquals(AllowancesHelper.sanetizeSpecialCharacters("a sample string to cehck under socres"),
				"a_sample_string_to_cehck_under_socres");
		assertEquals(AllowancesHelper.sanetizeSpecialCharacters("a|sample|string|to|cehck|under|socres"),
				"a_sample_string_to_cehck_under_socres");
		assertEquals(AllowancesHelper.sanetizeSpecialCharacters("a<sample<string<to<cehck<under<socres"),
				"a_sample_string_to_cehck_under_socres");
		assertEquals(AllowancesHelper.sanetizeSpecialCharacters("a>sample>string>to>cehck>under<socres"),
				"a_sample_string_to_cehck_under_socres");
		assertEquals(AllowancesHelper.sanetizeSpecialCharacters("a\\sample\\string\\to\\cehck\\under\\socres"),
				"a_sample_string_to_cehck_under_socres");
		assertEquals(AllowancesHelper.sanetizeSpecialCharacters("a/sample/string/to/cehck/under/socres"),
				"a_sample_string_to_cehck_under_socres");
		assertEquals(AllowancesHelper.sanetizeSpecialCharacters("a*sample*string*to*cehck*under*socres"),
				"a_sample_string_to_cehck_under_socres");
		assertEquals(AllowancesHelper.sanetizeSpecialCharacters("a?sample?string?to?cehck?under/socres"),
				"a_sample_string_to_cehck_under_socres");
		assertEquals(AllowancesHelper.sanetizeSpecialCharacters("a:sample:string:to:cehck:under:socres"),
				"a_sample_string_to_cehck_under_socres");
		String input = "\\|:*?<>| \" ";
		String output = "___________";
		String sanetizeSpecialCharacters = AllowancesHelper.sanetizeSpecialCharacters(input);
		assertEquals(sanetizeSpecialCharacters, output);
		assertEquals(sanetizeSpecialCharacters.length(), output.length());

	}

}
