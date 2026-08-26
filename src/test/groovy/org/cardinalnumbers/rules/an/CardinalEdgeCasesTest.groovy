package org.cardinalnumbers.rules.an

import org.junit.jupiter.api.Test
import org.numerals.CardinalUtil
import org.numerals.Number

import static org.junit.jupiter.api.Assertions.*

/**
 * Edge-case coverage for Aragonese (an) cardinal generation. Every expected string
 * here was verified empirically by running the "numerals" CLI with
 * -Duser.language=an. Grammar follows the Biquipedia article "Cardinals en
 * aragonés" (Academia de l'Aragonés, EDACAR 7, 2010):
 * - units 8=ueito, 9=nueu,
 * - tens sisanta(60), setanta(70), uitanta(80), novanta(90),
 * - "y" connector between tens and units (trenta y tres),
 * - joined 20-29 forms with graphic accent (ventidós, ventitrés, ventiséis),
 * - apocope uno -> un before a scale word (un milión), and ventiuno -> ventiún,
 * - "mil" (not "un mil") for 1000,
 * - "cient" for 100 (attested Aragonese form).
 */
class CardinalEdgeCasesTest {

    private static final Locale AN = new Locale("an")

    private static String words(String n) {
        return CardinalUtil.getCardinal(new Number(n), AN)
    }

    // ----- Units and teens (verified Aragonese forms) -----

    @Test
    void unitsUseAragoneseForms() {
        assertEquals("zero", words("0"))
        assertEquals("uno", words("1"))
        assertEquals("ueito", words("8"), "8 is 'ueito' in Aragonese, not 'ocho'")
        assertEquals("nueu", words("9"), "9 is 'nueu' in Aragonese, not 'nueve'")
    }

    @Test
    void teens() {
        assertEquals("once", words("11"))
        assertEquals("quince", words("15"))
        assertEquals("deciséis", words("16"), "16 is the soldered 'deciséis'")
    }

    // ----- Tens (verified normative forms) -----

    @Test
    void tensUseAcademiaForms() {
        assertEquals("vente", words("20"))
        assertEquals("trenta", words("30"))
        assertEquals("sisanta", words("60"))
        assertEquals("setanta", words("70"))
        assertEquals("uitanta", words("80"), "80 is 'uitanta' (Academia), not 'ueitanta'")
        assertEquals("novanta", words("90"), "90 is 'novanta' (Academia), not 'nobanta'")
    }

    // ----- 20-29: joined with graphic accent -----

    @Test
    void twentiesAreJoinedWithAccent() {
        assertEquals("ventiuno", words("21"), "standalone 21 stays 'ventiuno' (no apocope)")
        assertEquals("ventidós", words("22"))
        assertEquals("ventitrés", words("23"))
        assertEquals("ventiséis", words("26"))
    }

    // ----- 30-99: tens + "y" + unit, no accent on the separated unit -----

    @Test
    void thirtiesUseYConnectorWithoutAccent() {
        assertEquals("trenta y tres", words("33"))
        assertEquals("trenta y seis", words("36"))
        assertEquals("novanta y nueu", words("99"))
    }

    // ----- Hundreds: cient / X cientos -----

    @Test
    void hundreds() {
        assertEquals("cient", words("100"))
        assertEquals("cient uno", words("101"))
        assertEquals("cient ventitrés", words("123"))
        assertEquals("dos cientos", words("200"))
        assertEquals("cinco cientos", words("500"))
        assertEquals("nueu cientos novanta y nueu", words("999"))
    }

    // ----- "mil" (not "un mil") and thousands -----

    @Test
    void milIsNotUnMil() {
        assertEquals("mil", words("1000"), "1000 must be 'mil', not 'un mil'")
    }

    @Test
    void thousandsCompose() {
        assertEquals("mil uno", words("1001"))
        assertEquals("dos mil", words("2000"))
    }

    // ----- Apocope uno -> un / ventiuno -> ventiún in composed groups -----

    @Test
    void apocopeBeforeScaleWords() {
        assertEquals("ventiún mil", words("21000"), "apocope keeps the accent: ventiún")
        assertEquals("trenta y un mil", words("31000"))
        assertEquals("un milión", words("1000000"))
        assertEquals("un billón", words("1000000000000"))
    }

    // ----- Large composition -----

    @Test
    void largeComposition() {
        assertEquals(
            "un milión dos cientos trenta y cuatro mil cinco cientos sisanta y siete",
            words("1234567"))
    }

    // ----- Leading zeros are ignored -----

    @Test
    void leadingZerosAreIgnored() {
        assertEquals("zero", words("000"))
        assertEquals("siete", words("007"))
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
