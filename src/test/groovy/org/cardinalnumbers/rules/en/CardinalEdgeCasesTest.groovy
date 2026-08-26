package org.cardinalnumbers.rules.en

import org.junit.jupiter.api.Test
import org.numerals.CardinalUtil
import org.numerals.Number

import static org.junit.jupiter.api.Assertions.*

/**
 * Edge-case coverage for English cardinal generation, including the extended
 * short-scale range (up to 10^23 / 24 digits) that now matches Spanish.
 */
class CardinalEdgeCasesTest {

    private static final Locale EN = new Locale("en", "US")

    private static String words(String n) {
        return CardinalUtil.getCardinal(new Number(n), EN)
    }

    @Test
    void unitsTeensTens() {
        assertEquals("zero", words("0"))
        assertEquals("one", words("1"))
        assertEquals("eleven", words("11"))
        assertEquals("fifteen", words("15"))
        assertEquals("twenty-one", words("21"))
    }

    @Test
    void hundredsAndThousands() {
        assertEquals("one hundred", words("100"))
        assertEquals("one hundred one", words("101"))
        assertEquals("one thousand", words("1000"))
        assertEquals("two thousand", words("2000"))
    }

    @Test
    void millionsAndComposition() {
        assertEquals("one million", words("1000000"))
        assertEquals("twenty-one million", words("21000000"))
        assertEquals(
            "one million two hundred thirty-four thousand five hundred sixty-seven",
            words("1234567"))
    }

    @Test
    void extendedShortScale() {
        assertEquals("one billion", words("1000000000"))
        assertEquals("one trillion", words("1000000000000"))
        assertEquals("one quadrillion", words("1000000000000000"))
        assertEquals("one quintillion", words("1000000000000000000"))
        assertEquals("one sextillion", words("1000000000000000000000"))
    }

    @Test
    void maxRange24Digits() {
        // 24 nines must render fully (range now matches Spanish).
        String out = words("999999999999999999999999")
        assertTrue(out.startsWith("nine hundred ninety-nine sextillion"),
            "24-digit max should render up to sextillion: " + out)
    }

    @Test
    void inputValidation() {
        assertThrows(NumberFormatException.class, { new Number(null) })
        assertThrows(NumberFormatException.class, { new Number("") })
        assertThrows(NumberFormatException.class, { new Number("abc") })
        assertThrows(NumberFormatException.class, { new Number("-5") })
    }
}
