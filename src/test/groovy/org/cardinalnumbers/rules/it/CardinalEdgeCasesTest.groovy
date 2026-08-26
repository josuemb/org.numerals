package org.cardinalnumbers.rules.it

import org.junit.jupiter.api.Test
import org.numerals.CardinalUtil
import org.numerals.Number

import static org.junit.jupiter.api.Assertions.*

/**
 * Edge-case coverage for Italian cardinal generation, mirroring the es/pt
 * suites plus the Italian-specific rules:
 * - elision of the tens vowel before uno/otto (ventuno, ventotto, trentuno),
 * - irregular teens (undici .. diciannove),
 * - invariable "cento" hundreds,
 * - "mille" for 1000 and "mila" for multiples (duemila),
 * - "un milione" / "un miliardo" elision before space-separated scale words,
 * - input validation.
 */
class CardinalEdgeCasesTest {

    private static final Locale IT = new Locale("it", "IT")

    private static String words(String n) {
        return CardinalUtil.getCardinal(new Number(n), IT)
    }

    @Test
    void unitsTeensTens() {
        assertEquals("zero", words("0"))
        assertEquals("uno", words("1"))
        assertEquals("nove", words("9"))
        assertEquals("dieci", words("10"))
        assertEquals("undici", words("11"))
        assertEquals("dodici", words("12"))
        assertEquals("quindici", words("15"))
        assertEquals("sedici", words("16"))
        assertEquals("diciassette", words("17"))
        assertEquals("diciotto", words("18"))
        assertEquals("diciannove", words("19"))
        assertEquals("venti", words("20"))
    }

    @Test
    void tensElisionBeforeUnoAndOtto() {
        // The tens word drops its final vowel before uno and otto.
        assertEquals("ventuno", words("21"))
        assertEquals("ventotto", words("28"))
        assertEquals("trentuno", words("31"))
        assertEquals("trentotto", words("38"))
        assertEquals("novantuno", words("91"))
        assertEquals("novantotto", words("98"))
    }

    @Test
    void tensWithoutElision() {
        assertEquals("ventidue", words("22"))
        assertEquals("ventitre", words("23"))
        assertEquals("trentacinque", words("35"))
        assertEquals("novantanove", words("99"))
    }

    @Test
    void hundredsCentoIsInvariable() {
        assertEquals("cento", words("100"))
        assertEquals("cento uno", words("101"))
        assertEquals("cento ventitre", words("123"))
        assertEquals("duecento", words("200"))
        assertEquals("cinquecento", words("500"))
        assertEquals("novecento novantanove", words("999"))
    }

    @Test
    void thousandIsMilleNotUnoMille() {
        assertEquals("mille", words("1000"))
        assertEquals("duemila", words("2000"))
    }

    @Test
    void thousandsAttachToRemainder() {
        assertEquals("milleuno", words("1001"))
        assertEquals("duemilacinquecento", words("2500"))
    }

    @Test
    void scaleWordsAndUnElision() {
        // Isolated "uno" elides to "un" before milione/miliardo.
        assertEquals("un milione", words("1000000"))
        assertEquals("ventun milioni", words("21000000"))
        assertEquals("un miliardo", words("1000000000000"))
        assertEquals("un bilione", words("1000000000000000000"))
    }

    @Test
    void largeComposition() {
        assertEquals(
            "un milione duecento trentaquattromilacinquecento sessantasette",
            words("1234567"))
    }

    @Test
    void leadingZerosAreIgnored() {
        assertEquals("zero", words("000"))
        assertEquals("sette", words("007"))
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
