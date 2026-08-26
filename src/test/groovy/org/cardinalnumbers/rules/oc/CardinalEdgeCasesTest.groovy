package org.cardinalnumbers.rules.oc

import org.junit.jupiter.api.Test
import org.numerals.CardinalUtil
import org.numerals.Number

import static org.junit.jupiter.api.Assertions.*

/**
 * Edge-case coverage for Occitan (Languedocian, classical norm) cardinal
 * generation, mirroring the es/ca suites plus the Occitan-specific rules:
 * - the "-e-" connector for 17..29 vs the plain space for 30..90,
 * - the specific words for 10..16 and the composed forms for 17..19,
 * - hundreds formed with a space ("dos cents") and the plural "cents",
 * - the "un mila" -> "mila" omission,
 * - the -ion/-iard scale ("un milion", "un miliard"),
 * - input validation.
 *
 * All expected strings were verified empirically against the installed CLI with
 * -Duser.language=oc, and cross-checked with languagesandnumbers.com (Languedocian).
 */
class CardinalEdgeCasesTest {

    private static final Locale OC = new Locale("oc", "FR")

    private static String words(String n) {
        return CardinalUtil.getCardinal(new Number(n), OC)
    }

    @Test
    void unitsTeensTens() {
        assertEquals("zèro", words("0"))
        assertEquals("un", words("1"))
        assertEquals("sièis", words("6"))
        assertEquals("uèch", words("8"))
        assertEquals("nòu", words("9"))
        assertEquals("dètz", words("10"))
        assertEquals("onze", words("11"))
        assertEquals("catòrze", words("14"))
        assertEquals("quinze", words("15"))
        assertEquals("setze", words("16"))
        assertEquals("vint", words("20"))
    }

    @Test
    void teensSeventeenToNineteenUseEConnector() {
        assertEquals("dètz-e-sèt", words("17"), "17..19 compose dètz + -e- + digit")
        assertEquals("dètz-e-uèch", words("18"))
        assertEquals("dètz-e-nòu", words("19"))
    }

    @Test
    void twentiesUseEConnector() {
        assertEquals("vint-e-un", words("21"), "twenties keep the '-e-': vint-e-un")
        assertEquals("vint-e-tres", words("23"))
        assertEquals("vint-e-nòu", words("29"))
    }

    @Test
    void tensThirtyToNinetyUseSpace() {
        assertEquals("trenta", words("30"))
        assertEquals("trenta un", words("31"), "30..90 join with a space, no connector")
        assertEquals("cinquanta sèt", words("57"))
        assertEquals("nonanta nòu", words("99"))
    }

    @Test
    void hundreds() {
        assertEquals("cent", words("100"), "exact 100 is 'cent', not 'un cent'")
        assertEquals("cent un", words("101"))
        assertEquals("cent vint-e-tres", words("123"))
        assertEquals("dos cents", words("200"), "hundreds use plural 'cents' with a space")
        assertEquals("cinc cents", words("500"))
        assertEquals("nòu cents nonanta nòu", words("999"))
    }

    @Test
    void thousandIsMilaNotUnMila() {
        assertEquals("mila", words("1000"), "1000 must be 'mila', not 'un mila'")
        assertEquals("mila un", words("1001"))
        assertEquals("dos mila", words("2000"))
    }

    @Test
    void millionsAndMilliards() {
        assertEquals("un milion", words("1000000"))
        assertEquals("vint-e-un milions", words("21000000"), "vint-e-un before the plural 'milions'")
        assertEquals("un miliard", words("1000000000"), "10^9 is 'miliard' (not 'mila milions')")
    }

    @Test
    void compositeNumber() {
        assertEquals(
            "un milion dos cents trenta quatre mila cinc cents seissanta sèt",
            words("1234567"))
    }

    @Test
    void leadingZerosAreIgnored() {
        assertEquals("zèro", words("000"))
        assertEquals("sèt", words("007"))
    }

    @Test
    void inputValidation() {
        assertThrows(NumberFormatException.class, { new Number(null) })
        assertThrows(NumberFormatException.class, { new Number("") })
        assertThrows(NumberFormatException.class, { new Number("abc") })
        assertThrows(NumberFormatException.class, { new Number("12.5") })
        assertThrows(NumberFormatException.class, { new Number("-5") })
    }
}
