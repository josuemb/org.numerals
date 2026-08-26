package org.cardinalnumbers.rules.pt

import org.junit.jupiter.api.Test
import org.numerals.CardinalUtil
import org.numerals.Number

import static org.junit.jupiter.api.Assertions.*

/**
 * Edge-case coverage for Portuguese cardinal generation, mirroring the es/en
 * suites plus the Portuguese-specific "e" connector rules.
 */
class CardinalEdgeCasesTest {

    private static final Locale PT = new Locale("pt", "BR")

    private static String words(String n) {
        return CardinalUtil.getCardinal(new Number(n), PT)
    }

    @Test
    void unitsTeensTens() {
        assertEquals("zero", words("0"))
        assertEquals("um", words("1"))
        assertEquals("dez", words("10"))
        assertEquals("onze", words("11"))
        assertEquals("quinze", words("15"))
        assertEquals("dezesseis", words("16"))
        assertEquals("dezenove", words("19"))
        assertEquals("vinte", words("20"))
    }

    @Test
    void tensWithUnitsUseEConnector() {
        assertEquals("vinte e um", words("21"))
        assertEquals("vinte e tres", words("23"))
        assertEquals("noventa e nove", words("99"))
    }

    @Test
    void hundredsCemVsCento() {
        assertEquals("cem", words("100"), "exact 100 is 'cem'")
        assertEquals("cento e um", words("101"), "100 + rest is 'cento e ...'")
        assertEquals("cento e vinte e tres", words("123"))
        assertEquals("duzentos", words("200"))
        assertEquals("quinhentos", words("500"))
        assertEquals("novecentos e noventa e nove", words("999"))
    }

    @Test
    void thousandIsMilNotUmMil() {
        assertEquals("mil", words("1000"))
        assertEquals("dois mil", words("2000"))
    }

    @Test
    void interGroupEConnector() {
        // "e" when the remainder is < 100 or an exact multiple of 100
        assertEquals("mil e um", words("1001"))
        assertEquals("mil e cem", words("1100"))
        assertEquals("dois mil e quinhentos", words("2500"))
        assertEquals("dois mil e quinze", words("2015"))
        assertEquals("um milhão e um", words("1000001"))
        assertEquals("um milhão e cem", words("1000100"))
        // plain space (no "e") when the remainder has hundreds + more
        assertEquals("mil duzentos e trinta e quatro", words("1234"))
        assertEquals("um milhão duzentos e trinta e quatro", words("1000234"))
        assertEquals(
            "um milhão duzentos e trinta e quatro mil quinhentos e sessenta e sete",
            words("1234567"))
    }

    @Test
    void scaleWordsAndPlurals() {
        assertEquals("um milhão", words("1000000"))
        assertEquals("vinte e um milhões", words("21000000"))
        assertEquals("um bilhão", words("1000000000000"))
        assertEquals("um trilhão", words("1000000000000000000"))
    }

    @Test
    void inputValidation() {
        assertThrows(NumberFormatException.class, { new Number(null) })
        assertThrows(NumberFormatException.class, { new Number("") })
        assertThrows(NumberFormatException.class, { new Number("abc") })
        assertThrows(NumberFormatException.class, { new Number("-5") })
    }
}
