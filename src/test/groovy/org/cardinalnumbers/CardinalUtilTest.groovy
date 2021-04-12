package org.cardinalnumbers;

import static org.junit.Assert.*

import org.junit.Test
import org.numerals.CardinalUtil
import org.numerals.Number

class CardinalUtilTest {
	private String getCardinalOne(String language) {
		def cardinalOne = '';
		switch(language) {
			case 'en':
				cardinalOne = 'one';
			break
			case 'es':
				cardinalOne = 'uno'
			break
		}
		return cardinalOne
	}

	@Test
	public void testGetRulesInvalidClass() {
		boolean testOk = false
		try {
			CardinalUtil.getRules(String.class)
		} catch (MissingResourceException e) {
			testOk = true
		}
		assertTrue("Error testing CardinalUtil.getRules method (invalid class)",testOk)
	}
	@Test
	public void testGetRulesInvalidMethod() {
		boolean testOk = false
		try {
			CardinalUtil.getRules(InvalidRulesClass.class)
		} catch (ClassCastException e) {
			testOk = true
		}
		assertTrue("Error testing CardinalUtil.getRules method (invalid method)",testOk)
	}
	@Test
	public void testGetRulesOk() {
		def rules = CardinalUtil.getRules(ValidRulesClass.class)
		assertNotNull("Error testing CardinalUtil.getRules method (valid class)",rules)
	}
	@Test
	public void testGetRulesClassError() {
		boolean testOk = false
		try {
			CardinalUtil.getRulesClass(new Locale("RU"))
		} catch (MissingResourceException e) {
			testOk = true
		}
		assertTrue("Error testing Util.getRulesClass method (invalid Locale)",testOk)
	}
	@Test
	public void testGetRulesClassOk() {
		Class clazz = CardinalUtil.getRulesClass(new Locale("ES","MX"))
		assertNotNull("Error testing Util.getRulesClass method (valid Locale)",clazz)
	}
	@Test
	public void testGetCardinalWithRules() {
		def rules = [1:[1:"uno"]]
		def cardinal = CardinalUtil.getCardinal(new Number(1),rules)
		assertEquals("Error testing Util.getCardinal(Number number, Map rules)",cardinal,"uno")
	}
	@Test
	public void testGetCardinalWithClass() {
		def cardinal = CardinalUtil.getCardinal(new Number(1), org.numerals.rules.es.CardinalRules.class)
		assertEquals("Error testing Util.getCardinal(Object value, Class rulesClass)",cardinal,"uno")
	}
	@Test
	public void testGetCardinalWithLocale() {
		def cardinal = CardinalUtil.getCardinal(new Number(1),new Locale("es"))
		assertEquals("Error testing Util.getCardinal(Object value, Locale locale)",cardinal,"uno")
	}
	@Test
	public void testGetCardinal() {
		def cardinal = CardinalUtil.getCardinal(new Number(1))
		def language = Locale.getDefault().language;
		def cardinalOne = getCardinalOne(language);		
		assertEquals("Error testing Util.getCardinal(Object value)",cardinal,cardinalOne)
	}
}

class InvalidRulesClass {
	static String getRules() {
		return "Testing"
	}
}

class ValidRulesClass {
	static Map getRules() {
		return [1:[1:"uno"]]
	}
}
