package org.cardinalnumbers;

import static org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Test
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
		assertTrue(testOk, "Error testing CardinalUtil.getRules method (invalid class)")
	}
	@Test
	public void testGetRulesInvalidMethod() {
		boolean testOk = false
		try {
			CardinalUtil.getRules(InvalidRulesClass.class)
		} catch (ClassCastException e) {
			testOk = true
		}
		assertTrue(testOk, "Error testing CardinalUtil.getRules method (invalid method)")
	}
	@Test
	public void testGetRulesOk() {
		def rules = CardinalUtil.getRules(ValidRulesClass.class)
		assertNotNull(rules, "Error testing CardinalUtil.getRules method (valid class)")
	}
	@Test
	public void testGetRulesClassError() {
		boolean testOk = false
		try {
			CardinalUtil.getRulesClass(new Locale("RU"))
		} catch (MissingResourceException e) {
			testOk = true
		}
		assertTrue(testOk, "Error testing Util.getRulesClass method (invalid Locale)")
	}
	@Test
	public void testGetRulesClassOk() {
		Class clazz = CardinalUtil.getRulesClass(new Locale("ES","MX"))
		assertNotNull(clazz, "Error testing Util.getRulesClass method (valid Locale)")
	}
	@Test
	public void testGetCardinalWithRules() {
		def rules = [1:[1:"uno"]]
		def cardinal = CardinalUtil.getCardinal(new Number(1),rules)
		assertEquals(cardinal, "uno", "Error testing Util.getCardinal(Number number, Map rules)")
	}
	@Test
	public void testGetCardinalWithClass() {
		def cardinal = CardinalUtil.getCardinal(new Number(1), org.numerals.rules.es.CardinalRules.class)
		assertEquals(cardinal, "uno", "Error testing Util.getCardinal(Object value, Class rulesClass)")
	}
	@Test
	public void testGetCardinalWithLocale() {
		def cardinal = CardinalUtil.getCardinal(new Number(1),new Locale("es"))
		assertEquals(cardinal, "uno", "Error testing Util.getCardinal(Object value, Locale locale)")
	}
	@Test
	public void testGetCardinal() {
		def cardinal = CardinalUtil.getCardinal(new Number(1))
		def language = Locale.getDefault().language;
		def cardinalOne = getCardinalOne(language);		
		assertEquals(cardinal, cardinalOne, "Error testing Util.getCardinal(Object value)")
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
