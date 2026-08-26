package org.cardinalnumbers.rules.en.GB

import org.junit.jupiter.api.Test
import org.numerals.CardinalUtil
import org.numerals.Number

import static org.junit.jupiter.api.Assertions.*

/**
 * Edge-case coverage for British English (en_GB) cardinal generation. British
 * English is a locale variant of the base English (en) rules; the only
 * grammatical difference is the "and" connector inserted before a trailing
 * group below one hundred ("one hundred and twenty-three", "one thousand and
 * five", "one million and one"). These tests lock in that behaviour and confirm
 * the base English (en_US) rules remain unchanged (no "and").
 */
class CardinalEdgeCasesTest {

    private static final Locale EN_GB = new Locale("en", "GB")
    private static final Locale EN_US = new Locale("en", "US")

    private static String british(String n) {
        return CardinalUtil.getCardinal(new Number(n), EN_GB)
    }

    private static String american(String n) {
        return CardinalUtil.getCardinal(new Number(n), EN_US)
    }

    @Test
    void unitsTeensTensUnchanged() {
        // Below one hundred there is no trailing group, so "and" never appears.
        assertEquals("zero", british("0"))
        assertEquals("one", british("1"))
        assertEquals("sixteen", british("16"))
        assertEquals("twenty", british("20"))
        assertEquals("twenty-one", british("21"))
    }

    @Test
    void hundredsInsertAnd() {
        assertEquals("one hundred", british("100"))
        assertEquals("one hundred and one", british("101"))
        assertEquals("one hundred and five", british("105"))
        assertEquals("one hundred and twenty-three", british("123"))
        assertEquals("three hundred and forty-five", british("345"))
    }

    @Test
    void thousandsInsertAnd() {
        assertEquals("one thousand", british("1000"))
        assertEquals("one thousand and five", british("1005"))
        assertEquals("one thousand and twenty-three", british("1023"))
        assertEquals("two thousand and one", british("2001"))
    }

    @Test
    void millionsInsertAnd() {
        assertEquals("one million", british("1000000"))
        assertEquals("one million and one", british("1000001"))
    }

    @Test
    void deepCompositionPlacesAndPerGroup() {
        // "and" appears within each hundreds group, not between scale groups.
        assertEquals(
            "one million two hundred and thirty-four thousand five hundred and sixty-seven",
            british("1234567"))
    }

    @Test
    void americanBaseRulesRemainWithoutAnd() {
        // The base English (en) rules must be untouched: no "and" connector.
        assertEquals("one hundred one", american("101"))
        assertEquals("one hundred twenty-three", american("123"))
        assertEquals("one thousand and twenty-three".replace(" and ", " "), american("1023"))
        assertEquals("one million one", american("1000001"))
        assertFalse(american("123").contains(" and "),
            "Base English (en_US) must not contain the British 'and' connector")
    }
}
