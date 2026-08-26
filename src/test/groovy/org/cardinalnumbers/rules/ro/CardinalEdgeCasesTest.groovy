package org.cardinalnumbers.rules.ro

import org.junit.jupiter.api.Test
import org.numerals.CardinalUtil
import org.numerals.Number

import static org.junit.jupiter.api.Assertions.*

/**
 * Edge-case coverage for Romanian cardinal generation, mirroring the es/pt/it
 * suites plus the Romanian-specific rules:
 * - "spre" teens (unsprezece .. nouasprezece), with irregular 14/16 (paisprezece, saisprezece),
 * - "si" connector between tens and units (douazeci si unu),
 * - hundreds (o suta, doua sute), attached remainder without a connector,
 * - the "de" rule before scale words (douazeci de mii, o suta de milioane),
 * - scale words at their real magnitudes (mie 10^3, milion 10^6, miliard 10^9, trilion 10^12, ...),
 * - gender agreement of 1 before scale words (o mie / un milion / una de mii),
 * - input validation.
 *
 * All expected strings were verified empirically via the numerals CLI under
 * -Duser.language=ro before being recorded here.
 */
class CardinalEdgeCasesTest {

    private static final Locale RO = new Locale("ro", "RO")

    private static String words(String n) {
        return CardinalUtil.getCardinal(new Number(n), RO)
    }

    @Test
    void units() {
        assertEquals("zero", words("0"))
        assertEquals("unu", words("1"))
        assertEquals("doi", words("2"))
        assertEquals("trei", words("3"))
        assertEquals("cinci", words("5"))
        assertEquals("noua", words("9"))
        assertEquals("zece", words("10"))
    }

    @Test
    void spreTeens() {
        assertEquals("unsprezece", words("11"))
        assertEquals("doisprezece", words("12"))
        assertEquals("treisprezece", words("13"))
        // 14 and 16 are irregular: paisprezece (not patrusprezece), saisprezece (not sasesprezece).
        assertEquals("paisprezece", words("14"))
        assertEquals("cincisprezece", words("15"))
        assertEquals("saisprezece", words("16"))
        assertEquals("saptesprezece", words("17"))
        assertEquals("optsprezece", words("18"))
        assertEquals("nouasprezece", words("19"))
    }

    @Test
    void siConnectorTens() {
        assertEquals("douazeci", words("20"))
        assertEquals("douazeci si unu", words("21"))
        assertEquals("douazeci si doi", words("22"))
        assertEquals("treizeci", words("30"))
        assertEquals("treizeci si cinci", words("35"))
        assertEquals("saizeci", words("60"))
        assertEquals("nouazeci si noua", words("99"))
    }

    @Test
    void hundreds() {
        assertEquals("o suta", words("100"))
        assertEquals("o suta unu", words("101"))
        assertEquals("o suta douazeci si trei", words("123"))
        assertEquals("doua sute", words("200"))
        assertEquals("trei sute", words("300"))
        assertEquals("cinci sute", words("500"))
        assertEquals("noua sute nouazeci si noua", words("999"))
    }

    @Test
    void thousands() {
        // 1000 is "o mie" (mie is feminine), not "un mie" nor "unu mie".
        assertEquals("o mie", words("1000"))
        assertEquals("o mie unu", words("1001"))
        // 2..19 thousands take no "de": doua mii, trei mii.
        assertEquals("doua mii", words("2000"))
        assertEquals("trei mii", words("3000"))
    }

    @Test
    void deRuleBeforeScaleWords() {
        // Groups whose last two digits are 00 or 20-99 take "de" before the scale word.
        assertEquals("douazeci de mii", words("20000"))
        assertEquals("o suta de milioane", words("100000000"))
        // Groups 1-19 do NOT take "de".
        assertEquals("doua mii", words("2000"))
        assertEquals("cinci milioane", words("5000000"))
        // 999 thousands -> 99 in range 20-99 -> takes "de".
        assertEquals(
            "noua sute nouazeci si noua de mii noua sute nouazeci si noua",
            words("999999"))
    }

    @Test
    void genderOfOneBeforeScaleWords() {
        // 1 before feminine "mie" -> "o"; before neuter scales -> "un".
        assertEquals("o mie", words("1000"))
        assertEquals("un milion", words("1000000"))
        assertEquals("un miliard", words("1000000000"))
        // Trailing "unu" -> "una" before mie (feminine), stays "unu" before milioane (neuter).
        assertEquals("douazeci si una de mii", words("21000"))
        assertEquals("douazeci si unu de milioane", words("21000000"))
        // The 01-exception: last two digits 01 are in 1-19, so no "de" (o suta una mii).
        assertEquals("o suta una mii", words("101000"))
    }

    @Test
    void scaleWordsRealMagnitudes() {
        // Romanian scale increments every 3 digits with its real magnitudes.
        assertEquals("un milion", words("1000000"))              // 10^6
        assertEquals("doua milioane", words("2000000"))          // 10^6 plural
        assertEquals("un miliard", words("1000000000"))          // 10^9
        assertEquals("un trilion", words("1000000000000"))       // 10^12
    }

    @Test
    void largeComposition() {
        assertEquals(
            "un milion doua sute treizeci si patru de mii cinci sute saizeci si sapte",
            words("1234567"))
    }

    @Test
    void leadingZerosAreIgnored() {
        assertEquals("zero", words("000"))
        assertEquals("sapte", words("007"))
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
