/*
 * Copyright 2007-2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.numerals.rules.ro;

import java.util.Map;
import java.util.MissingResourceException;
import java.util.function.Function;

import org.numerals.NumberValue;
import org.numerals.RuleSet;

/**
 * Reguli de cardinale pentru limba Romana (ro).
 *
 * <p>Port direct al clasei Groovy {@code org.numerals.rules.ro.CardinalRules}.
 * Structura (tabele de unitati/zeci/sute + tabela de sufixe pe intervale de
 * pozitii + compunere recursiva) se pastreaza; forma se schimba: closurile
 * Groovy devin lambda {@link org.numerals.CardinalRule} iar Map-urile literale
 * devin {@link java.util.Map#of}.
 *
 * <p>Particularitati ale limbii Romane tratate aici:
 * <ul>
 *   <li>Adolescentele (11-19) cu "spre", cu formele neregulate 14/16
 *       (paisprezece, saisprezece).</li>
 *   <li>Zecile plus unitatile legate cu conjunctia "si" (douazeci si unu).</li>
 *   <li>Sutele: o suta (100), doua sute (200), ...; restul se ataseaza cu spatiu,
 *       fara conector.</li>
 *   <li>Scala are un cuvant la fiecare 3 cifre (mie 10^3, milion 10^6, ...).</li>
 *   <li>Genul lui 1 inaintea scalelor: "o" inainte de mie (feminin), "un" inainte
 *       de milion/miliard/... (neutru).</li>
 *   <li>Regula "de": prepozitia "de" se insereaza intre grup si cuvantul de scala
 *       cand ultimele doua cifre ale grupului sunt 00 sau in intervalul 20-99.</li>
 * </ul>
 *
 * <p>Conventia proiectului: identificatorii fiecarei limbi raman in limba proprie
 * (unitati, zeci, sute, sufixe, getSufix...).
 */
public final class CardinalRulesRo {

    private static final int POZITIE_MIN = 1;
    private static final int POZITIE_MAX = 24;
    private static final int CIFRA_MIN = 0;
    private static final int CIFRA_MAX = 9;
    private static final String SEPARATOR_ZECI = " si ";
    private static final String CONECTOR_DE = " de ";
    private static final String SUFIX_MIE = "mie";

    // Unitatile in forma de numarare (masculina implicita): unu, doi, ...
    private static final Map<Integer, String> unitati = Map.of(
            0, "zero", 1, "unu", 2, "doi", 3, "trei", 4, "patru",
            5, "cinci", 6, "sase", 7, "sapte", 8, "opt", 9, "noua");

    // Unitatile in forma feminina, folosite inaintea sutelor si a cuvintelor de
    // scala (doua sute, doua mii). Restul cifrelor coincid cu forma de numarare.
    private static final Map<Integer, String> unitatiFeminine = Map.of(2, "doua");

    private static final Map<Integer, String> zeci = Map.of(
            1, "zece", 2, "douazeci", 3, "treizeci", 4, "patruzeci", 5, "cincizeci",
            6, "saizeci", 7, "saptezeci", 8, "optzeci", 9, "nouazeci");

    // Adolescentele 10-19: o singura palabra neregulata, formata cu "spre".
    // 14 (paisprezece) si 16 (saisprezece) nu urmeaza exact regula.
    private static final Map<Integer, String> zeciSpeciale = Map.of(
            0, "zece", 1, "unsprezece", 2, "doisprezece", 3, "treisprezece",
            4, "paisprezece", 5, "cincisprezece", 6, "saisprezece",
            7, "saptesprezece", 8, "optsprezece", 9, "nouasprezece");

    // Cuvantul pentru suta: "o suta" (exact 100) / forma feminina + "sute" (200-900).
    private static final String SUTA_SINGULAR = "o suta";
    private static final String SUTE_PLURAL = "sute";

    /** Sufix de scala pe interval de pozitii (singular/plural). */
    private record Sufix(int deLa, int panaLa, String singular, String plural) {
        boolean acopera(int pozitie) {
            return pozitie >= deLa && pozitie <= panaLa;
        }
    }

    // Scala romaneasca: un cuvant de scala la fiecare 3 cifre dincolo de mie.
    //   10^3  mie/mii        10^6  milion       10^9  miliard
    //   10^12 trilion        10^15 cvadrilion   10^18 cvintilion   10^21 sextilion
    private static final Sufix[] sufixe = {
        new Sufix(4, 6, " mie", " mii"),
        new Sufix(7, 9, " milion", " milioane"),
        new Sufix(10, 12, " miliard", " miliarde"),
        new Sufix(13, 15, " trilion", " trilioane"),
        new Sufix(16, 18, " cvadrilion", " cvadrilioane"),
        new Sufix(19, 21, " cvintilion", " cvintilioane"),
        new Sufix(22, 24, " sextilion", " sextilioane"),
    };

    private CardinalRulesRo() {
    }

    /** Construieste conjunctul de reguli pentru toate pozitiile suportate. */
    public static RuleSet ruleSet() {
        RuleSet rules = new RuleSet();
        for (int pozitie = POZITIE_MIN; pozitie <= POZITIE_MAX; pozitie++) {
            switch (pozitie) {
                case 1 -> registreazaUnitati(rules);
                case 2 -> registreazaZeci(rules);
                case 3 -> registreazaSute(rules);
                default -> registreazaComun(rules, pozitie);
            }
        }
        return rules;
    }

    private static Sufix getSufix(int pozitie) {
        for (Sufix sufix : sufixe) {
            if (sufix.acopera(pozitie)) {
                return sufix;
            }
        }
        throw new MissingResourceException(
                "Cannot found class suffix for position:" + pozitie,
                CardinalRulesRo.class.getName(), "getSufix");
    }

    // Unitati (pozitia 1). 0 suna "zero" doar cand este numarul complet.
    private static void registreazaUnitati(RuleSet rules) {
        for (int cifra = CIFRA_MIN; cifra <= CIFRA_MAX; cifra++) {
            int c = cifra;
            rules.put(1, cifra, (number, recurse) ->
                    (c == 0 && number.size() != 1) ? "" : unitati.get(c));
        }
    }

    // Zeci (pozitia 2): adolescentele 10-19 o singura palabra; 20-99 leaga cu "si".
    private static void registreazaZeci(RuleSet rules) {
        for (int cifra = CIFRA_MIN; cifra <= CIFRA_MAX; cifra++) {
            int c = cifra;
            switch (cifra) {
                // Zeci de la 10 la 19: o singura palabra neregulata, fara conector.
                case 1 -> rules.put(2, cifra, (number, recurse) ->
                        zeciSpeciale.get(number.lastDigit()));
                // 20 la 90: palabra zecii, optional conectorul "si" plus unitatea.
                default -> rules.put(2, cifra, (number, recurse) ->
                        cardinalZeci(number, recurse, c));
            }
        }
    }

    private static String cardinalZeci(
            NumberValue number, Function<NumberValue, String> recurse, int cifra) {
        StringBuilder cardinal = new StringBuilder(zeci.get(cifra));
        if (number.lastDigit() != 0) {
            cardinal.append(SEPARATOR_ZECI).append(recurse.apply(number.lastDigits(1)));
        }
        return cardinal.toString();
    }

    // Sute (pozitia 3): "o suta" (exact 100), forma feminina + "sute" (200-900).
    private static void registreazaSute(RuleSet rules) {
        for (int cifra = CIFRA_MIN; cifra <= CIFRA_MAX; cifra++) {
            switch (cifra) {
                case 1 -> rules.put(3, cifra, (number, recurse) -> {
                    NumberValue numRest = number.lastDigits(2);
                    return numRest.equalsInt(0)
                            ? SUTA_SINGULAR
                            : SUTA_SINGULAR + " " + recurse.apply(numRest);
                });
                default -> {
                    String cifraFeminina =
                            unitatiFeminine.getOrDefault(cifra, unitati.get(cifra));
                    String suta = cifraFeminina + " " + SUTE_PLURAL;
                    rules.put(3, cifra, (number, recurse) -> {
                        NumberValue numRest = number.lastDigits(2);
                        return numRest.equalsInt(0)
                                ? suta
                                : suta + " " + recurse.apply(numRest);
                    });
                }
            }
        }
    }

    // Pozitiile 4+ (mii, milioane, ...): toate cifrele aplica regula "de".
    private static void registreazaComun(RuleSet rules, int pozitie) {
        for (int cifra = CIFRA_MIN; cifra <= CIFRA_MAX; cifra++) {
            rules.put(pozitie, cifra, CardinalRulesRo::aplicaRegulaDe);
        }
    }

    /**
     * Caz comun de scala: separa grupul de ordin inalt de restul, aplica sufixul
     * (singular/plural), forma feminina "doua" inaintea scalei, si genul lui 1
     * ("o mie" / "un milion") plus "unu" -> "una" inaintea femininului "mie".
     */
    private static String getCardinalComun(
            NumberValue number, Function<NumberValue, String> recurse) {
        StringBuilder cardinal = new StringBuilder();

        int pozitie = number.size();
        Sufix sufix = getSufix(pozitie);

        // Grupul de ordin inalt ocupa de la pozitia curenta pana la inceputul
        // intervalului; restul sunt cifrele de sub acel sufix.
        int cifreGrup = pozitie - sufix.deLa() + 1;
        NumberValue numGrup = number.firstDigits(cifreGrup);
        NumberValue numRest = number.lastDigits(sufix.deLa() - 1);

        boolean grupUnu = numGrup.equalsInt(1);
        boolean sufixMie = sufix.singular().trim().equals(SUFIX_MIE);

        // Forma feminina "doua" inaintea oricarui cuvant de scala (mie feminin;
        // milion, miliard, ... neutre, deci feminine la plural): doua mii.
        String grupCardinal = recurse.apply(numGrup)
                .replaceAll("\\bdoi\\b", unitatiFeminine.get(2));

        if (grupUnu) {
            // Grupul exact 1: "o mie" (feminin) sau "un milion" (scala neutra).
            cardinal.append(sufixMie ? "o" : "un");
        } else {
            // "...unu" final devine "una" inaintea femininului "mie" (douazeci si
            // una de mii), dar ramane "unu" inaintea scalelor neutre.
            if (sufixMie) {
                grupCardinal = grupCardinal.replaceAll("\\bunu\\b", "una");
            }
            cardinal.append(grupCardinal);
        }

        cardinal.append(grupUnu ? sufix.singular() : sufix.plural());

        if (!numRest.equalsInt(0)) {
            cardinal.append(' ').append(recurse.apply(numRest));
        }

        return cardinal.toString().trim();
    }

    /**
     * Insereaza prepozitia "de" intre grup si cuvantul de scala cand regula o
     * cere, apoi deleaga calculul catre getCardinalComun.
     * Ultimele doua cifre ale grupului determina regula: 00 sau 20-99 cer "de";
     * 1-19 nu (doua mii, cinci milioane vs douazeci de mii, o suta de milioane).
     */
    private static String aplicaRegulaDe(
            NumberValue number, Function<NumberValue, String> recurse) {
        int pozitie = number.size();
        Sufix sufix = getSufix(pozitie);
        int cifreGrup = pozitie - sufix.deLa() + 1;
        NumberValue numGrup = number.firstDigits(cifreGrup);

        String cardinalBrut = getCardinalComun(number, recurse);

        if (!necesitaDe(numGrup)) {
            return cardinalBrut;
        }

        // Insereaza "de" imediat inainte de cuvantul de scala (primul termen dupa
        // grup). Sufixul are spatiu initial (ex. " mii"); il localizam pentru a
        // pune "de" inaintea lui fara a afecta restul cardinalului.
        String cuvantScala = numGrup.equalsInt(1) ? sufix.singular() : sufix.plural();

        int indice = cardinalBrut.indexOf(cuvantScala.trim());
        if (indice <= 0) {
            return cardinalBrut;
        }
        StringBuilder cardinal = new StringBuilder();
        cardinal.append(cardinalBrut.substring(0, indice).trim());
        cardinal.append(CONECTOR_DE);
        cardinal.append(cardinalBrut.substring(indice));
        return cardinal.toString();
    }

    /**
     * Determina daca grupul cere prepozitia "de" inaintea cuvantului de scala.
     * Adevarat cand ultimele doua cifre ale grupului sunt 00 sau in 20-99.
     */
    private static boolean necesitaDe(NumberValue numGrup) {
        String text = numGrup.toString();
        String ultimeleDoua = text.length() >= 2
                ? text.substring(text.length() - 2)
                : text;
        int valoare = Integer.parseInt(ultimeleDoua);
        return valoare == 0 || valoare >= 20;
    }
}
