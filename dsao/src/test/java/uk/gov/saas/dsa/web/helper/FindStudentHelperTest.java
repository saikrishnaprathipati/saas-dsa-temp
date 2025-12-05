package uk.gov.saas.dsa.web.helper;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class FindStudentHelperTest {

	@Test
	void shouldReadNumberAsStringForTens() {
		Assertions.assertEquals("ten", FindStudentHelper.convertNumberToWord(10));
	}

	@Test
	void shouldReadNumberAsStringForZero() {
		Assertions.assertEquals("zero", FindStudentHelper.convertNumberToWord(0));
	}

	@Test
	void shouldReadNumberAsStringForHundreds() {
		Assertions.assertEquals("one hundred", FindStudentHelper.convertNumberToWord(100));
	}

	@Test
	void shouldReadNumberAsStringForThousands() {
		Assertions.assertEquals("one thousand ", FindStudentHelper.convertNumberToWord(1000));
	}

	@Test
	void shouldReadNumberAsStringFor10000() {
		Assertions.assertEquals("ten thousand ", FindStudentHelper.convertNumberToWord(10000));
	}

	@Test
	void shouldReadNumberAsStringFor1000000() {
		Assertions.assertEquals("one million ", FindStudentHelper.convertNumberToWord(1000000));
	}

	@Test
	void shouldReadNumberAsStringFor10000000() {
		Assertions.assertEquals("ten million ", FindStudentHelper.convertNumberToWord(10000000));
	}

	@Test
	void shouldReadNumberAsStringFor100000000() {
		Assertions.assertEquals("one hundred million ", FindStudentHelper.convertNumberToWord(100000000));
	}

	@Test
	void shouldReadNumberAsStringFor1000000000() {
		Assertions.assertEquals("one billion ", FindStudentHelper.convertNumberToWord(1000000000));
	}
}
