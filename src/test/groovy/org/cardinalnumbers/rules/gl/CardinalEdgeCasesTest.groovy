package org.cardinalnumbers.rules.gl

import org.junit.jupiter.api.Test
import org.numerals.CardinalUtil
import org.numerals.Number

import static org.junit.jupiter.api.Assertions.*

/**
 * Edge-case coverage for Galician cardinal generation, mirroring the es/pt
 * suites plus the Galician-specific "e" connector rules.
 */
class CardinalEdgeCasesTest {

    private static final Locale GL = new Locale("gl", "ES")

    private static String words(String n) {
        return CardinalUtil.getCardinal(new Number(n), GL)
    }

    @Test
    void unitsTeensTens() {
        assertEquals("cero", words("0"))
        assertEquals("un", words("1"))
        assertEquals("dez", words("10"))
        assertEquals("once", words("11"))
        assertEquals("quince", words("15"))
        assertEquals("dezaseis", words("16"))
        assertEquals("dezanove", words("19"))
        assertEquals("vinte", words("20"))
    }

    @Test
    void tensWithUnitsUseEConnector() {
        assertEquals("vinte e un", words("21"))
        assertEquals("trinta e dous", words("32"))
        assertEquals("noventa e nove", words("99"))
    }

    @Test
    void hundredsCenVsCento() {
        assertEquals("cen", words("100"), "exact 100 is 'cen'")
        assertEquals("cento e un", words("101"), "100 + rest is 'cento e ...'")
        assertEquals("cento e vinte e tres", words("123"))
        assertEquals("douscentos", words("200"))
        assertEquals("cincocentos", words("500"))
        assertEquals("novecentos e noventa e nove", words("999"))
    }

    @Test
    void thousandIsMilNotUnMil() {
        assertEquals("mil", words("1000"))
        assertEquals("dous mil", words("2000"))
    }

    @Test
    void interGroupEConnector() {
        // "e" when the remainder is < 100 or an exact multiple of 100
        assertEquals("mil e un", words("1001"))
        assertEquals("mil e cen", words("1100"))
        assertEquals("dous mil e quince", words("2015"))
        assertEquals("dous mil e cincocentos", words("2500"))
        assertEquals("un millón e un", words("1000001"))
        assertEquals("un millón e cen", words("1000100"))
        // plain space (no "e") when the remainder has hundreds + more
        assertEquals("un millón douscentos e trinta e catro", words("1000234"))
        assertEquals(
            "un millón douscentos e trinta e catro mil cincocentos e sesenta e sete",
            words("1234567"))
    }

    @Test
    void scaleWordsAndPlurals() {
        assertEquals("un millón", words("1000000"))
        assertEquals("vinte e un millóns", words("21000000"))
        assertEquals("un billón", words("1000000000000"))
        assertEquals("un trillón", words("1000000000000000000"))
    }

    @Test
    void leadingZerosAreIgnored() {
        assertEquals("cero", words("000"))
        assertEquals("sete", words("007"))
    }

    @Test
    void inputValidation() {
        assertThrows(NumberFormatException.class, { new Number(null) })
        assertThrows(NumberFormatException.class, { new Number("") })
        assertThrows(NumberFormatException.class, { new Number("abc") })
        assertThrows(NumberFormatException.class, { new Number("-5") })
    }
}
