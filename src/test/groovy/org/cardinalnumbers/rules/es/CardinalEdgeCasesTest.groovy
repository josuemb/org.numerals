package org.cardinalnumbers.rules.es

import org.junit.jupiter.api.Test
import org.numerals.CardinalUtil
import org.numerals.Number

import static org.junit.jupiter.api.Assertions.*

/**
 * Edge-case coverage for Spanish cardinal generation:
 * - the "un mil" -> "mil" fix,
 * - the apocope accent "veintiuno" -> "veintiún" in composed groups,
 * - boundaries (zero, single digit, hundreds, scale jumps),
 * - input validation (null, empty, non-digits),
 * - and the documented scale ceiling.
 */
class CardinalEdgeCasesTest {

    private static final Locale ES = new Locale("es", "MX")

    private static String words(String n) {
        return CardinalUtil.getCardinal(new Number(n), ES)
    }

    // ----- Fixed bugs -----

    @Test
    void milIsNotUnMil() {
        assertEquals("mil", words("1000"), "1000 must be 'mil', not 'un mil'")
    }

    @Test
    void milComposesWithRemainder() {
        assertEquals("mil uno", words("1001"))
        assertEquals("dos mil", words("2000"))
    }

    @Test
    void apocopeAccentInComposedGroups() {
        assertEquals("veintiún millones", words("21000000"), "apocope must carry the accent: veintiún")
        assertEquals("veintiún mil", words("21000"))
        assertEquals("treinta y un mil", words("31000"))
    }

    @Test
    void unKeptForMillonAndBillon() {
        assertEquals("un millón", words("1000000"))
        assertEquals("un billón", words("1000000000000"))
    }

    // ----- Standalone (no apocope) -----

    @Test
    void standaloneUnitsAndTeensAndTens() {
        assertEquals("cero", words("0"))
        assertEquals("uno", words("1"))
        assertEquals("dieciseis", words("16"))
        assertEquals("veintiuno", words("21"), "standalone 21 stays 'veintiuno' (no apocope)")
        assertEquals("treinta y uno", words("31"))
    }

    @Test
    void hundreds() {
        assertEquals("cien", words("100"))
        assertEquals("ciento uno", words("101"))
    }

    @Test
    void leadingZerosAreIgnored() {
        assertEquals("cero", words("000"))
        assertEquals("siete", words("007"))
    }

    @Test
    void largeComposition() {
        assertEquals(
            "un millón doscientos treinta y cuatro mil quinientos sesenta y siete",
            words("1234567"))
    }

    // ----- Input validation -----

    @Test
    void nullIsRejected() {
        assertThrows(NumberFormatException.class, { new Number(null) })
    }

    @Test
    void emptyIsRejected() {
        assertThrows(NumberFormatException.class, { new Number("") })
    }

    @Test
    void nonDigitsAreRejected() {
        assertThrows(NumberFormatException.class, { new Number("abc") })
        assertThrows(NumberFormatException.class, { new Number("12.5") })
        assertThrows(NumberFormatException.class, { new Number("-5") })
    }

    // ----- Scale ceiling (documents current supported range) -----

    @Test
    void scaleUpToEighteenDigitsWorks() {
        // 10^18 = un trillón is the top defined scale word.
        assertEquals("un trillón", words("1000000000000000000"))
    }
}
