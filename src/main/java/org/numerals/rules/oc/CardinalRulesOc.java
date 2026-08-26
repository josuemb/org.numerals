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

package org.numerals.rules.oc;

import java.util.Map;
import java.util.MissingResourceException;
import java.util.function.Function;

import org.numerals.NumberValue;
import org.numerals.RuleSet;

/**
 * Reglas de cardinales para el idioma Occitano (oc), norma clasica /
 * lengadocian.
 *
 * <p>Puerto directo de la clase Groovy {@code org.numerals.rules.oc.CardinalRules}.
 * La estructura (tablas de unitats/desenas/centenas + tabla de sufixes por rango
 * de posiciones + composicion recursiva) se conserva; lo que cambia es la forma:
 * las closures de Groovy son ahora lambdas {@link org.numerals.CardinalRule} y los
 * Map literales son {@link java.util.Map#of}.
 *
 * <p>Particularidades del Occitano tratadas aqui (norma clasica / lengadocian):
 * <ul>
 *   <li>Los numeros de 17 a 29 se forman con el conector "-e-" entre la decena y
 *       la unidad: "detz-e-set" (17), "vint-e-un" (21), "vint-e-nou" (29).</li>
 *   <li>A partir de 30, la decena y la unidad se unen con un espacio simple, sin
 *       conector: "trenta un" (31), "cinquanta set" (57).</li>
 *   <li>Las decenas de 10 a 16 son palabras especificas.</li>
 *   <li>Cada centena se forma con la unidad delante de "cent"/"cents".</li>
 *   <li>1000 es "mila" (no "un mila").</li>
 *   <li>La escala occitana alterna las formas en -ion y en -iard cada 3 cifras.</li>
 * </ul>
 *
 * <p>Convencion del proyecto: los identificadores de cada idioma van en su propia
 * lengua (unitats, desenas, sufixes, getSufix...).
 */
public final class CardinalRulesOc {

    private static final String SEPARADOR_TEENS = "-e-";
    private static final String SEPARADOR_DESENAS = " ";
    // Lo limit de las desenas amb connector "-e-": de 17 a 29 (chifra de desena
    // 1 amb unitat, e chifra de desena 2). A partir de 30 s'emplega un espaci.
    private static final int DESENA_AMB_CONNECTOR_E = 2;
    private static final int POSICION_MIN = 1;
    private static final int POSICION_MAX = 24;
    private static final int CHIFRA_MIN = 0;
    private static final int CHIFRA_MAX = 9;

    private static final Map<Integer, String> unitats = Map.of(
            0, "zèro", 1, "un", 2, "dos", 3, "tres", 4, "quatre",
            5, "cinc", 6, "sièis", 7, "sèt", 8, "uèch", 9, "nòu");

    private static final Map<Integer, String> desenas = Map.of(
            1, "dètz", 2, "vint", 3, "trenta", 4, "quaranta", 5, "cinquanta",
            6, "seissanta", 7, "setanta", 8, "ochanta", 9, "nonanta");

    // Desenas de 10 a 16: mots especifics en Occitan. De 17 a 19 se compausan
    // (dètz-e-sèt...), doncas aicí sols se definisson 10 a 16.
    private static final Map<Integer, String> desenasEspecialas = Map.of(
            0, "dètz", 1, "onze", 2, "dotze", 3, "tretze",
            4, "catòrze", 5, "quinze", 6, "setze");

    // Cada centena: l'unitat davant "cent" (singular per 100) o "cents" (plural).
    private static final Map<Integer, String> centenas = Map.of(
            1, "cent", 2, "dos cents", 3, "tres cents", 4, "quatre cents",
            5, "cinc cents", 6, "sièis cents", 7, "sèt cents",
            8, "uèch cents", 9, "nòu cents");

    /** Sufix d'escala per interval de posicions (singular/plural ont s'aplica). */
    private record Sufix(int desde, int hasta, String singular, String plural) {
        boolean cubre(int posicion) {
            return posicion >= desde && posicion <= hasta;
        }
    }

    // Escala occitana: coma en italian, un mot d'escala cada 3 chifras en delà
    // dels milièrs, en alternant las formas en -ion (milion, bilion...) e en
    // -iard (miliard, biliard...).
    private static final Sufix[] sufixes = {
        new Sufix(4, 6, " mila", " mila"),
        new Sufix(7, 9, " milion", " milions"),
        new Sufix(10, 12, " miliard", " miliards"),
        new Sufix(13, 15, " bilion", " bilions"),
        new Sufix(16, 18, " biliard", " biliards"),
        new Sufix(19, 21, " trilion", " trilions"),
        new Sufix(22, 24, " triliard", " triliards"),
    };

    private CardinalRulesOc() {
    }

    /** Construís lo conjunt de reglas per totas las posicions suportadas. */
    public static RuleSet ruleSet() {
        RuleSet rules = new RuleSet();
        for (int posicion = POSICION_MIN; posicion <= POSICION_MAX; posicion++) {
            switch (posicion) {
                case 1 -> registrarUnitats(rules);
                case 2 -> registrarDesenas(rules);
                case 3 -> registrarCentenas(rules);
                default -> registrarComun(rules, posicion);
            }
        }
        return rules;
    }

    private static Sufix getSufix(int posicion) {
        for (Sufix sufix : sufixes) {
            if (sufix.cubre(posicion)) {
                return sufix;
            }
        }
        throw new MissingResourceException(
                "Cannot found class suffix for position:" + posicion,
                CardinalRulesOc.class.getName(), "getSufix");
    }

    // Unitats (posicion 1). Lo 0 sols sona "zèro" quand es lo nombre complet.
    private static void registrarUnitats(RuleSet rules) {
        for (int chifra = CHIFRA_MIN; chifra <= CHIFRA_MAX; chifra++) {
            int c = chifra;
            rules.put(1, chifra, (number, recurse) ->
                    (c == 0 && number.size() != 1) ? "" : unitats.get(c));
        }
    }

    /*
     * Desenas (posicion 2). De 10 a 16: mots especifics. De 17 a 29: connector
     * "-e-" entre desena e unitat. De 30 a 90: espaci simple entre desena e
     * unitat.
     */
    private static void registrarDesenas(RuleSet rules) {
        for (int chifra = CHIFRA_MIN; chifra <= CHIFRA_MAX; chifra++) {
            int c = chifra;
            if (chifra == 1) {
                // Desena 1 (10..19): 10 a 16 son mots especifics; 17 a 19 se
                // compausan amb "dètz" + "-e-" + unitat (dètz-e-sèt...).
                rules.put(2, chifra, (number, recurse) -> {
                    int unitat = number.lastDigit();
                    if (desenasEspecialas.containsKey(unitat)) {
                        return desenasEspecialas.get(unitat);
                    }
                    return desenas.get(c) + SEPARADOR_TEENS + recurse.apply(number.lastDigits(1));
                });
            } else {
                // Desenas 20 a 90: mot de la desena, opcionalament + unitat.
                // La vintena (20..29) emplega lo connector "-e-"; de 30 a 90
                // s'emplega un espaci simple.
                rules.put(2, chifra, (number, recurse) -> {
                    StringBuilder cardinal = new StringBuilder(desenas.get(c));
                    if (number.lastDigit() != 0) {
                        cardinal.append(c == DESENA_AMB_CONNECTOR_E ? SEPARADOR_TEENS : SEPARADOR_DESENAS);
                        cardinal.append(recurse.apply(number.lastDigits(1)));
                    }
                    return cardinal.toString();
                });
            }
        }
    }

    /*
     * Centenas (posicion 3). L'unitat se met davant "cent"/"cents", seguida del
     * rèst amb un espaci.
     */
    private static void registrarCentenas(RuleSet rules) {
        for (int chifra = CHIFRA_MIN; chifra <= CHIFRA_MAX; chifra++) {
            int c = chifra;
            rules.put(3, chifra, (number, recurse) -> {
                String centena = centenas.get(c);
                NumberValue numRest = number.lastDigits(2);
                return numRest.equalsInt(0) ? centena : centena + " " + recurse.apply(numRest);
            });
        }
    }

    // Posicions 4+ (milièrs, milions, ...): totas las chifras emplegan lo cas comun.
    private static void registrarComun(RuleSet rules, int posicion) {
        for (int chifra = CHIFRA_MIN; chifra <= CHIFRA_MAX; chifra++) {
            rules.put(posicion, chifra, (number, recurse) -> getCardinalComun(number, recurse));
        }
    }

    /**
     * Cas comun (dels milièrs en avant): separa lo grop d'orde naut de son rèst,
     * aplica lo sufix (singular/plural) e la regla de "mila" (un sol milièr se
     * ditz "mila", pas "un mila").
     */
    private static String getCardinalComun(NumberValue number, Function<NumberValue, String> recurse) {
        StringBuilder cardinal = new StringBuilder();

        int posicion = number.size();
        Sufix sufix = getSufix(posicion);

        // Lo grop d'orde naut ocupa de la posicion actuala fins a l'inici de
        // l'interval; lo rèst son las chifras en dejós d'aqueste sufix.
        int chifrasGrop = posicion - sufix.desde() + 1;
        NumberValue numGrop = number.firstDigits(chifrasGrop);
        NumberValue numRest = number.lastDigits(sufix.desde() - 1);

        boolean gropUn = numGrop.equalsInt(1);
        boolean sufixMila = sufix.singular().trim().equals("mila");

        String gropCardinal = recurse.apply(numGrop);

        // "1000" se ditz "mila" (pas "un mila"): s'omet lo grop quand val
        // exactament 1 e lo sufix es lo dels milièrs.
        if (!(gropUn && sufixMila)) {
            cardinal.append(gropCardinal);
        }

        // Plural del sufix franc que lo grop siá exactament 1.
        cardinal.append(gropUn ? sufix.singular() : sufix.plural());

        if (!numRest.equalsInt(0)) {
            cardinal.append(' ').append(recurse.apply(numRest));
        }

        return cardinal.toString().trim();
    }
}
