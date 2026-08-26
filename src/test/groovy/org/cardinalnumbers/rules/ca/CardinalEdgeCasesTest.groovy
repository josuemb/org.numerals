package org.cardinalnumbers.rules.ca

import org.junit.jupiter.api.Test
import org.numerals.CardinalUtil
import org.numerals.Number

import static org.junit.jupiter.api.Assertions.*

/**
 * Edge-case coverage for Catalan cardinal generation, mirroring the es/pt
 * suites plus the Catalan-specific rules:
 * - the "vint-i-" connector for the twenties vs the plain "-" for 30..90,
 * - the "u" numeral vs the apocopated "un" (before scale words and after 30..90),
 * - hundreds formed with a dash ("dos-cents"),
 * - the "un mil" -> "mil" omission,
 * - the long scale ("mil milions", "un bilio"),
 * - input validation and the documented scale ceiling.
 */
class CardinalEdgeCasesTest {

    private static final Locale CA = new Locale("ca", "ES")

    private static String words(String n) {
        return CardinalUtil.getCardinal(new Number(n), CA)
    }

    @Test
    void unitsTeensTens() {
        assertEquals("zero", words("0"))
        assertEquals("u", words("1"), "the bare numeral 1 is 'u'")
        assertEquals("deu", words("10"))
        assertEquals("onze", words("11"))
        assertEquals("quinze", words("15"))
        assertEquals("setze", words("16"))
        assertEquals("disset", words("17"))
        assertEquals("divuit", words("18"))
        assertEquals("dinou", words("19"))
        assertEquals("vint", words("20"))
    }

    @Test
    void twentiesUseIConnector() {
        assertEquals("vint-i-u", words("21"), "twenties keep the 'i': vint-i-u")
        assertEquals("vint-i-dos", words("22"))
        assertEquals("vint-i-tres", words("23"))
        assertEquals("vint-i-nou", words("29"))
    }

    @Test
    void tensThirtyToNinetyUseDashAndUn() {
        assertEquals("trenta-un", words("31"), "1 apocopates to 'un' after 30..90")
        assertEquals("trenta-dos", words("32"))
        assertEquals("quaranta-un", words("41"))
        assertEquals("noranta-nou", words("99"))
    }

    @Test
    void hundreds() {
        assertEquals("cent", words("100"), "exact 100 is 'cent', not 'un cent'")
        assertEquals("cent u", words("101"), "100 + rest keeps the bare 'u'")
        assertEquals("cent vint-i-tres", words("123"))
        assertEquals("dos-cents", words("200"), "hundreds use a dash: dos-cents")
        assertEquals("cinc-cents", words("500"))
        assertEquals("nou-cents noranta-nou", words("999"))
    }

    @Test
    void thousandIsMilNotUnMil() {
        assertEquals("mil", words("1000"), "1000 must be 'mil', not 'un mil'")
        assertEquals("mil u", words("1001"))
        assertEquals("dos mil", words("2000"))
        assertEquals("trenta-un mil", words("31000"))
    }

    @Test
    void apocopeUnBeforeScaleWords() {
        assertEquals("un milió", words("1000000"), "1 apocopates to 'un' before a scale word")
        assertEquals("un bilió", words("1000000000000"))
        assertEquals("vint-i-un milions", words("21000000"), "vint-i-u -> vint-i-un before 'milions'")
        assertEquals("vint-i-un bilions", words("21000000000000"))
    }

    @Test
    void scaleWordsAreNotOverApocopated() {
        // The 'u' inside "nou"/"vuit" must NOT be touched by the apocope.
        assertEquals("nou milions", words("9000000"))
        assertEquals("vuit milions", words("8000000"))
    }

    @Test
    void longScaleComposition() {
        assertEquals("mil milions", words("1000000000"), "10^9 is 'mil milions' (long scale)")
        assertEquals("mil bilions", words("1000000000000000"), "10^15 is 'mil bilions'")
        assertEquals(
            "un milió dos-cents trenta-quatre mil cinc-cents seixanta-set",
            words("1234567"))
    }

    @Test
    void leadingZerosAreIgnored() {
        assertEquals("zero", words("000"))
        assertEquals("set", words("007"))
    }

    @Test
    void inputValidation() {
        assertThrows(NumberFormatException.class, { new Number(null) })
        assertThrows(NumberFormatException.class, { new Number("") })
        assertThrows(NumberFormatException.class, { new Number("abc") })
        assertThrows(NumberFormatException.class, { new Number("12.5") })
        assertThrows(NumberFormatException.class, { new Number("-5") })
    }

    @Test
    void scaleUpToEighteenDigitsWorks() {
        // 10^18 = un trilio is the top defined scale word.
        assertEquals("un trilió", words("1000000000000000000"))
    }
}
