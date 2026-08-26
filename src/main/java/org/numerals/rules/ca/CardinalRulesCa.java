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

package org.numerals.rules.ca;

import java.util.Map;
import java.util.MissingResourceException;
import java.util.function.Function;

import org.numerals.NumberValue;
import org.numerals.RuleSet;

/**
 * Regles de cardinals per a l'idioma Catala (ca).
 *
 * <p>Port directe de la classe Groovy {@code org.numerals.rules.ca.CardinalRules}.
 * L'estructura (taules d'unitats/desenes/centenes + taula de sufixos per rang de
 * posicions + composicio recursiva) es conserva; el que canvia es la forma: les
 * closures de Groovy son ara lambdes {@link org.numerals.CardinalRule} i els Map
 * literals son {@link java.util.Map#of}.
 *
 * <p>Particularitats del Catala tractades aqui:
 * <ul>
 *   <li>Les desenes i les unitats s'uneixen amb un guio: "trenta-cinc" [35].
 *       L'excepcio son les vintenes, on es mante la conjuncio "i":
 *       "vint-i-cinc" [25].
 *   <li>La unitat 1 es "u" darrere de "vint-i-" ("vint-i-u" [21]), pero "un"
 *       darrere de les altres desenes ("trenta-un" [31]) i davant d'una escala
 *       ("un milio").
 *   <li>Les desenes de 10 a 19 son paraules irregulars (deu, onze, ... dinou).
 *   <li>Cada centena te la seva propia paraula formada amb guio: "cent" [100],
 *       "dos-cents" [200]... El 100 exacte es "cent" (no "un cent").
 *   <li>1000 es "mil" (no "un mil").
 *   <li>El Catala fa servir l'escala llarga: "un milio" (10^6), "mil milions"
 *       (10^9), "un bilio" (10^12), "un trilio" (10^18). "mil milions" surt de la
 *       recursio (el grup "mil" davant del sufix " milions").
 * </ul>
 *
 * <p>Convencio del projecte: els identificadors de cada idioma van en la seva
 * propia llengua (unitats, desenes, sufixos, getSufix...).
 */
public final class CardinalRulesCa {

    private static final int POSICIO_MIN = 1;
    private static final int POSICIO_MAX = 24;
    private static final int DIGIT_MIN = 0;
    private static final int DIGIT_MAX = 9;
    private static final String SEPARADOR_DESENES = "-";
    private static final String SEPARADOR_VINTENES = "-i-";

    // La unitat 1 te dues formes. Com a numeral pur (sola o al final d'un numero)
    // es "u": "u" [1], "cent u" [101], "vint-i-u" [21]. Pren la forma "un" nomes
    // en dos contextos: darrere de les desenes 30..90 ("trenta-un" [31]) i davant
    // d'una paraula d'escala ("un milio", "un bilio").
    private static final Map<Integer, String> unitats = Map.of(
            0, "zero", 1, "u", 2, "dos", 3, "tres", 4, "quatre",
            5, "cinc", 6, "sis", 7, "set", 8, "vuit", 9, "nou");

    private static final String UNITAT_UN = "un";

    private static final Map<Integer, String> desenes = Map.of(
            1, "deu", 2, "vint", 3, "trenta", 4, "quaranta", 5, "cinquanta",
            6, "seixanta", 7, "setanta", 8, "vuitanta", 9, "noranta");

    // Desenes de 10 a 19: paraules irregulars en Catala.
    private static final Map<Integer, String> desenesEspecials = Map.of(
            0, "deu", 1, "onze", 2, "dotze", 3, "tretze", 4, "catorze",
            5, "quinze", 6, "setze", 7, "disset", 8, "divuit", 9, "dinou");

    // Cada centena te la seva paraula. L'1 es "cent" (exacte o amb resta). La
    // resta es formen amb "<unitat>-cents" (dos-cents, tres-cents...).
    private static final Map<Integer, String> centenes = Map.of(
            1, "cent", 2, "dos-cents", 3, "tres-cents", 4, "quatre-cents",
            5, "cinc-cents", 6, "sis-cents", 7, "set-cents", 8, "vuit-cents",
            9, "nou-cents");

    /**
     * Sufix d'escala per rang de posicions. On el Catala distingeix singular i
     * plural (mili\u00F3/milions...) es guarden les dues formes; per a "mil" totes
     * dues son iguals.
     */
    private record Sufix(int desde, int fins, String singular, String plural) {
        boolean cobreix(int posicio) {
            return posicio >= desde && posicio <= fins;
        }
    }

    private static final Sufix[] sufixos = {
        new Sufix(4, 6, " mil", " mil"),
        new Sufix(7, 12, " mili\u00F3", " milions"),
        new Sufix(13, 18, " bili\u00F3", " bilions"),
        new Sufix(19, 24, " trili\u00F3", " trilions"),
    };

    private CardinalRulesCa() {
    }

    /** Construeix el conjunt de regles per a totes les posicions suportades. */
    public static RuleSet ruleSet() {
        RuleSet rules = new RuleSet();
        for (int posicio = POSICIO_MIN; posicio <= POSICIO_MAX; posicio++) {
            switch (posicio) {
                case 1 -> registrarUnitats(rules);
                case 2 -> registrarDesenes(rules);
                case 3 -> registrarCentenes(rules);
                default -> registrarComu(rules, posicio);
            }
        }
        return rules;
    }

    private static Sufix getSufix(int posicio) {
        for (Sufix sufix : sufixos) {
            if (sufix.cobreix(posicio)) {
                return sufix;
            }
        }
        throw new MissingResourceException(
                "Cannot found class suffix for position:" + posicio,
                CardinalRulesCa.class.getName(), "getSufix");
    }

    // Unitats (posicio 1). El 0 nomes sona "zero" quan es el numero complet.
    private static void registrarUnitats(RuleSet rules) {
        for (int digit = DIGIT_MIN; digit <= DIGIT_MAX; digit++) {
            int d = digit;
            rules.put(1, digit, (number, recurse) ->
                    (d == 0 && number.size() != 1) ? "" : unitats.get(d));
        }
    }

    // Desenes (posicio 2): teens irregulars, vintenes amb "-i-", i 30..90 amb "-".
    private static void registrarDesenes(RuleSet rules) {
        for (int digit = DIGIT_MIN; digit <= DIGIT_MAX; digit++) {
            int d = digit;
            switch (digit) {
                // Desenes de 10 a 19: una unica paraula irregular, sense unitat separada.
                case 1 -> rules.put(2, digit, (number, recurse) ->
                        desenesEspecials.get(number.lastDigit()));
                // Vintenes: "vint", opcionalment "-i-" + unitat ("vint-i-u", "vint-i-tres").
                // La unitat 1 pren la forma per defecte "u".
                case 2 -> rules.put(2, digit, (number, recurse) -> {
                    StringBuilder cardinal = new StringBuilder(desenes.get(d));
                    if (number.lastDigit() != 0) {
                        cardinal.append(SEPARADOR_VINTENES)
                                .append(recurse.apply(number.lastDigits(1)));
                    }
                    return cardinal.toString();
                });
                // 30 a 90: paraula, opcionalment "-" + unitat ("trenta-un", "trenta-dos").
                // La unitat 1 pren la forma "un" darrere d'aquestes desenes.
                default -> rules.put(2, digit, (number, recurse) ->
                        cardinalDesenes(number, recurse, d));
            }
        }
    }

    private static String cardinalDesenes(
            NumberValue number, Function<NumberValue, String> recurse, int desena) {
        StringBuilder cardinal = new StringBuilder(desenes.get(desena));
        if (number.lastDigit() != 0) {
            cardinal.append(SEPARADOR_DESENES)
                    .append(number.lastDigit() == 1 ? UNITAT_UN : recurse.apply(number.lastDigits(1)));
        }
        return cardinal.toString();
    }

    // Centenes (posicio 3): "cent"/"<unitat>-cents", opcionalment " " + resta.
    private static void registrarCentenes(RuleSet rules) {
        for (int digit = DIGIT_MIN; digit <= DIGIT_MAX; digit++) {
            int d = digit;
            rules.put(3, digit, (number, recurse) -> {
                String centena = centenes.get(d);
                NumberValue numResta = number.lastDigits(2);
                return numResta.equalsInt(0) ? centena : centena + " " + recurse.apply(numResta);
            });
        }
    }

    // Posicions 4+ (milers, milions, ...): tots els digits fan servir el cas comu.
    private static void registrarComu(RuleSet rules, int posicio) {
        for (int digit = DIGIT_MIN; digit <= DIGIT_MAX; digit++) {
            rules.put(posicio, digit, (number, recurse) -> cardinalComu(number, recurse));
        }
    }

    /**
     * Cas comu d'escala llarga: separa el grup d'ordre alt del seu resta, aplica
     * el sufix (singular/plural), l'apocope de "u" -> "un" davant d'una paraula
     * d'escala i la regla de "mil" (un sol miler es diu "mil", no "un mil").
     */
    private static String cardinalComu(NumberValue number, Function<NumberValue, String> recurse) {
        StringBuilder cardinal = new StringBuilder();

        int posicio = number.size();
        Sufix sufix = getSufix(posicio);

        // El grup d'ordre alt ocupa des de la posicio actual fins a l'inici del rang;
        // el resta son els digits per sota d'aquest sufix.
        int digitsGrup = posicio - sufix.desde() + 1;
        NumberValue numGrup = number.firstDigits(digitsGrup);
        NumberValue numResta = number.lastDigits(sufix.desde() - 1);

        String grupCardinal = recurse.apply(numGrup);

        // Apocope davant d'una paraula d'escala (milio, bilio, trilio): la unitat
        // final "u" del grup pren la forma "un". Aixi "u" -> "un milio", i el
        // compost "vint-i-u" -> "vint-i-un milions". No s'aplica davant de "mil".
        if (!sufix.singular().trim().equals("mil")) {
            grupCardinal = grupCardinal.replaceAll("\\bu\\b", UNITAT_UN);
        }

        // "1 mil" es diu "mil" (no "un mil"): ometre el grup quan es exactament 1
        // i el sufix es el singular "mil".
        boolean esMilSingular = numGrup.equalsInt(1) && sufix.singular().trim().equals("mil");
        if (!esMilSingular) {
            cardinal.append(grupCardinal);
        }

        // Plural del sufix llevat que el grup sigui exactament 1.
        cardinal.append(numGrup.equalsInt(1) ? sufix.singular() : sufix.plural());

        if (!numResta.equalsInt(0)) {
            cardinal.append(' ').append(recurse.apply(numResta));
        }

        return cardinal.toString().trim();
    }
}
