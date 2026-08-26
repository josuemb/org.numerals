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

package org.numerals.rules.ast;

import java.util.Map;
import java.util.MissingResourceException;
import java.util.function.Function;

import org.numerals.NumberValue;
import org.numerals.RuleSet;

/**
 * Regles de cardinales pa la llingua Asturiana (ast).
 *
 * <p>Puertu directu de la clase Groovy {@code org.numerals.rules.ast.CardinalRules}.
 * Consérvase la estructura (tables d'unidaes/decenes/centenes + tabla de sufixos
 * por rangu de posiciones + composicion recursiva); lo que camuda ye la forma: les
 * closures de Groovy son agora lambdes {@link org.numerals.CardinalRule} y los Map
 * lliterales son {@link java.util.Map#of}.
 *
 * <p>Formes normatives (Academia de la Llingua Asturiana / ALLA):
 * <ul>
 *   <li>Unidaes: ceru, unu, dos, tres, cuatro, cinco, seis, siete, ocho, nueve.
 *       El 1 aisllau ye "unu"; na composicion aplica apocope "un".</li>
 *   <li>Teens 11-15 formes propies; 16-19 aglutinacion "dieci-" (dieciseis con tilde).</li>
 *   <li>Vientigrupu (21-29): pallabra compuesta "venti..." siempres apocopada con
 *       tilde (21 ye "ventiun", non "ventiuno").</li>
 *   <li>Centenes: cien (exactu 100), cientu (con restu).</li>
 *   <li>Escala llarga europea: millon (10^6), billon (10^12), trillon (10^18).
 *       1000 = "mil" (non "un mil").</li>
 * </ul>
 *
 * <p>Convencion del proyeutu: los identificadores de cada llingua van na so propia
 * llingua (unidaes, decenes, sufixos, getSufixu...).
 */
public final class CardinalRulesAst {

    private static final int POSICION_MIN = 1;
    private static final int POSICION_MAX = 24;
    private static final int DIXITU_MIN = 0;
    private static final int DIXITU_MAX = 9;
    private static final String SEPARADOR_DECENES = " y ";

    private static final Map<Integer, String> unidaes = Map.of(
            0, "ceru", 1, "unu", 2, "dos", 3, "tres", 4, "cuatro",
            5, "cinco", 6, "seis", 7, "siete", 8, "ocho", 9, "nueve");

    private static final Map<Integer, String> decenes = Map.of(
            1, "diez", 2, "venti", 3, "trenta", 4, "cuarenta", 5, "cincuenta",
            6, "sesenta", 7, "setenta", 8, "ochenta", 9, "noventa");

    // Teens 11-15: formes propies (iguales al espanol en grafia, normatives n'asturianu).
    private static final Map<Integer, String> decenesEspecialesUnu = Map.of(
            1, "once", 2, "doce", 3, "trece", 4, "catorce", 5, "quince");

    // Teens 16-19: aglutinacion "dieci-" + dixitu (con tilde en 16: dieciseis).
    private static final Map<Integer, String> decenesEspecialesDieci = Map.of(
            6, "diecis\u00E9is", 7, "diecisiete", 8, "dieciocho", 9, "diecinueve");

    // 20s: prefixu "venti" + dixitu (con tildes: ventiun, ventidos, ventitres, ventiseis).
    private static final Map<Integer, String> ventigrupu = Map.of(
            1, "venti\u00FAn", 2, "ventid\u00F3s", 3, "ventitr\u00E9s", 4, "venticuatro",
            5, "venticinco", 6, "ventis\u00E9is", 7, "ventisiete", 8, "ventiocho",
            9, "ventinueve");

    // Centenes: "cien" cuando'l restu ye 0, "cientu" en composicion; comparten el dixitu 1.
    private static final String CIEN = "cien";
    private static final String CIENTU = "cientu";
    private static final Map<Integer, String> centenesEspeciales = Map.of(
            5, "quinientos", 7, "setecientos", 9, "novecientos");

    /** Sufixu d'escala por rangu de posiciones (singular/plural onde aplica). */
    private record Sufixu(int desde, int hasta, String singular, String plural) {
        boolean cubre(int posicion) {
            return posicion >= desde && posicion <= hasta;
        }
    }

    private static final Sufixu[] sufixos = {
        new Sufixu(3, 3, "cientos", "cientos"),
        new Sufixu(4, 6, " mil", " mil"),
        new Sufixu(7, 12, " mill\u00F3n", " millones"),
        new Sufixu(13, 18, " bill\u00F3n", " billones"),
        new Sufixu(19, 24, " trill\u00F3n", " trillones"),
    };

    private CardinalRulesAst() {
    }

    /** Constrúi'l conxuntu de regles pa toles posiciones sofitaes. */
    public static RuleSet ruleSet() {
        RuleSet rules = new RuleSet();
        for (int posicion = POSICION_MIN; posicion <= POSICION_MAX; posicion++) {
            switch (posicion) {
                case 1 -> registrarUnidaes(rules);
                case 2 -> registrarDecenes(rules);
                case 3 -> registrarCentenes(rules);
                default -> registrarComun(rules, posicion);
            }
        }
        return rules;
    }

    private static Sufixu getSufixu(int posicion) {
        for (Sufixu sufixu : sufixos) {
            if (sufixu.cubre(posicion)) {
                return sufixu;
            }
        }
        throw new MissingResourceException(
                "Cannot found class suffix for position:" + posicion,
                CardinalRulesAst.class.getName(), "getSufixu");
    }

    // Unidaes (posicion 1). El 0 solo suena "ceru" cuando ye'l numberu completu.
    private static void registrarUnidaes(RuleSet rules) {
        for (int dixitu = DIXITU_MIN; dixitu <= DIXITU_MAX; dixitu++) {
            int d = dixitu;
            rules.put(1, dixitu, (number, recurse) ->
                    (d == 0 && number.size() != 1) ? "" : unidaes.get(d));
        }
    }

    // Decenes (posicion 2): teens (10-15 pallabra propia, 16-19 dieci-), ventigrupu, y "X y unida".
    private static void registrarDecenes(RuleSet rules) {
        for (int dixitu = DIXITU_MIN; dixitu <= DIXITU_MAX; dixitu++) {
            int d = dixitu;
            switch (dixitu) {
                case 1 -> rules.put(2, dixitu, (number, recurse) -> {
                    int u = number.lastDigit();
                    if (u == 0) {
                        return decenes.get(d);
                    }
                    if (u >= 1 && u <= 5) {
                        return decenesEspecialesUnu.get(u);
                    }
                    return decenesEspecialesDieci.get(u);
                });
                case 2 -> rules.put(2, dixitu, (number, recurse) ->
                        number.lastDigit() == 0 ? decenes.get(d) : ventigrupu.get(number.lastDigit()));
                default -> rules.put(2, dixitu, (number, recurse) ->
                        cardinalDecenes(number, recurse, d));
            }
        }
    }

    // "trenta y un", "cuarenta", etc. (decenes 3-9), con apocope unu->un.
    private static String cardinalDecenes(
            NumberValue number, Function<NumberValue, String> recurse, int decena) {
        StringBuilder cardinal = new StringBuilder(decenes.get(decena));
        if (number.lastDigit() != 0) {
            String unidaCardin = recurse.apply(number.lastDigits(1)).replaceAll("\\bunu\\b", "un");
            cardinal.append(SEPARADOR_DECENES).append(unidaCardin);
        }
        return cardinal.toString();
    }

    // Centenes (posicion 3): 1 -> cien/cientu, {5,7,9} irregulares, restu via casu comun.
    private static void registrarCentenes(RuleSet rules) {
        for (int dixitu = DIXITU_MIN; dixitu <= DIXITU_MAX; dixitu++) {
            int d = dixitu;
            switch (dixitu) {
                case 1 -> rules.put(3, dixitu, (number, recurse) -> {
                    NumberValue restu = number.lastDigits(2);
                    return restu.equalsInt(0) ? CIEN : CIENTU + " " + recurse.apply(restu);
                });
                case 5, 7, 9 -> rules.put(3, dixitu, (number, recurse) -> {
                    NumberValue restu = number.lastDigits(2);
                    String centena = centenesEspeciales.get(d);
                    return restu.equalsInt(0) ? centena : centena + " " + recurse.apply(restu);
                });
                default -> rules.put(3, dixitu, (number, recurse) -> cardinalComun(number, recurse));
            }
        }
    }

    // Posiciones 4+ (millares, millones, ...): tolos dixitos usen el casu comun.
    private static void registrarComun(RuleSet rules, int posicion) {
        for (int dixitu = DIXITU_MIN; dixitu <= DIXITU_MAX; dixitu++) {
            rules.put(posicion, dixitu, (number, recurse) -> cardinalComun(number, recurse));
        }
    }

    /**
     * Casu comun d'escala llarga: separa'l grupu d'orde altu del so restu,
     * aplica'l sufixu (singular/plural), la apocope de "unu" -> "un" y la regla
     * de "mil" (un solu millar dizse "mil", non "un mil").
     */
    private static String cardinalComun(NumberValue number, Function<NumberValue, String> recurse) {
        StringBuilder cardinal = new StringBuilder();

        int pos = number.size();
        Sufixu sufixu = getSufixu(pos);

        // El grupu d'orde altu ocupa dende la posicion actual hasta l'entamu del rangu;
        // el restu son los dixitos per baxo d'esi sufixu.
        int dixitosGrupu = pos - sufixu.desde() + 1;
        NumberValue numGrupu = number.firstDigits(dixitosGrupu);
        NumberValue numRestu = number.lastDigits(sufixu.desde() - 1);

        // Apocope: "unu" final -> "un" (un millon, ventiun mil). "ventiun" caltiense tal cual.
        String grupuCardinal = recurse.apply(numGrupu).replaceAll("\\bunu\\b", "un");

        boolean esMilSingular = numGrupu.equalsInt(1) && sufixu.singular().trim().equals("mil");
        if (!esMilSingular) {
            cardinal.append(grupuCardinal);
        }

        // Plural del sufixu sacantes que'l grupu seya exactamente 1.
        cardinal.append(numGrupu.equalsInt(1) ? sufixu.singular() : sufixu.plural());

        if (!numRestu.equalsInt(0)) {
            cardinal.append(' ').append(recurse.apply(numRestu));
        }

        return cardinal.toString().trim();
    }
}
