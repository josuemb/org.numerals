package org.cardinalnumbers.rules.ast

import org.junit.jupiter.api.Test
import org.numerals.CardinalUtil
import org.numerals.Number

import static org.junit.jupiter.api.Assertions.*

/**
 * Edge-case coverage for Asturian cardinal generation:
 * - the "un mil" -> "mil" fix,
 * - the apocope "unu" -> "un" in composed groups (trenta y un, un millon),
 * - the always-apocopated "ventiun" 20s form (differs from Spanish "veintiuno"),
 * - boundaries (zero, single digit, hundreds, scale jumps),
 * - input validation (null, empty, non-digits),
 * - and the documented scale ceiling.
 *
 * Toles cadenes verificaronse empiricamente col binariu instalau
 * (JAVA_TOOL_OPTIONS='-Duser.language=ast') enantes d'escribiles equi.
 */
class CardinalEdgeCasesTest {

    private static final Locale AST = new Locale("ast", "ES")

    private static String words(String n) {
        return CardinalUtil.getCardinal(new Number(n), AST)
    }

    // ----- Fixed / scale-specific behaviour -----

    @Test
    void milIsNotUnMil() {
        assertEquals("mil", words("1000"), "1000 has de ser 'mil', non 'un mil'")
    }

    @Test
    void milComposesWithRemainder() {
        assertEquals("mil unu", words("1001"))
        assertEquals("dos mil", words("2000"))
    }

    @Test
    void apocopeInComposedGroups() {
        assertEquals("ventiún millones", words("21000000"))
        assertEquals("ventiún mil", words("21000"))
        assertEquals("trenta y un mil", words("31000"))
    }

    @Test
    void unKeptForMillonAndBillon() {
        assertEquals("un millón", words("1000000"))
        assertEquals("un billón", words("1000000000000"))
    }

    // ----- Standalone units, teens, tens -----

    @Test
    void standaloneUnitsAndTeensAndTens() {
        assertEquals("ceru", words("0"))
        assertEquals("unu", words("1"), "1 aislau ye 'unu'")
        assertEquals("dieciséis", words("16"))
        assertEquals("ventiún", words("21"), "21 n'asturianu ye siempre 'ventiún' (apocopau con tilde)")
        assertEquals("trenta y un", words("31"))
    }

    @Test
    void tensWordForms() {
        assertEquals("cuarenta", words("40"))
        assertEquals("setenta", words("70"))
    }

    @Test
    void hundreds() {
        assertEquals("cien", words("100"))
        assertEquals("cientu unu", words("101"))
        assertEquals("cientu ventitrés", words("123"))
        assertEquals("doscientos", words("200"))
        assertEquals("quinientos", words("500"))
    }

    @Test
    void leadingZerosAreIgnored() {
        assertEquals("ceru", words("000"))
        assertEquals("siete", words("007"))
    }

    @Test
    void largeComposition() {
        assertEquals(
            "un millón doscientos trenta y cuatro mil quinientos sesenta y siete",
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
        // 10^18 = un trillón ye la pallabra d'escala mas alta definida.
        assertEquals("un trillón", words("1000000000000000000"))
    }
}
