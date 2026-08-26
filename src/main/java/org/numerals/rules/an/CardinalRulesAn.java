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

package org.numerals.rules.an;

import java.util.Map;
import java.util.MissingResourceException;
import java.util.function.Function;

import org.numerals.NumberValue;
import org.numerals.RuleSet;

/**
 * Reglas de cardinals ta l'aragonés (an).
 *
 * <p>Puerto directo d'a clase Groovy {@code org.numerals.rules.an.CardinalRules}.
 * A estructura (tablas d'unidaz/decenas/centenas + tabla de sufixos por rango de
 * posicions + composición recursiva) se conserva; lo que cambia ye a forma: as
 * closures de Groovy son agora lambdas {@link org.numerals.CardinalRule} y os Map
 * literals son {@link java.util.Map#of}.
 *
 * <p>Gramatica seguntes a Biquipedia, articlo "Cardinals en aragonés" (Academia de
 * l'Aragonés, EDACAR 7, 2010):
 * <ul>
 *   <li>unidaz 8=ueito, 9=nueu,</li>
 *   <li>decenas sisanta(60), setanta(70), uitanta(80), novanta(90),</li>
 *   <li>conector "y" entre decenas y unidaz (trenta y tres),</li>
 *   <li>formas soldadas 20-29 con tono grafico (ventidós, ventitrés, ventiséis),</li>
 *   <li>apocope uno -> un debant d'un sustantivo d'escala (un milión), y
 *       ventiuno -> ventiún,</li>
 *   <li>"mil" (no "un mil") ta 1000,</li>
 *   <li>"cient" ta 100.</li>
 * </ul>
 *
 * <p>Convención d'o proyecto: os identificadors de cada luenga van en a suya propia
 * luenga (unidaz, decenas, sufixos, getSufixo...).
 */
public final class CardinalRulesAn {

    private static final int POSICION_MIN = 1;
    private static final int POSICION_MAX = 24;
    private static final int DICHITO_MIN = 0;
    private static final int DICHITO_MAX = 9;
    private static final String SEPARADOR_DECENAS = " y ";

    // Unidaz de 0 a 9. "ueito" (8) y "nueu" (9) son as formas aragonesas.
    private static final Map<Integer, String> unidaz = Map.of(
            0, "zero", 1, "uno", 2, "dos", 3, "tres", 4, "cuatro",
            5, "cinco", 6, "seis", 7, "siete", 8, "ueito", 9, "nueu");

    // Decenas exautas: diez, vente, trenta... Formas normativas de l'Academia.
    private static final Map<Integer, String> decenas = Map.of(
            1, "diez", 2, "vente", 3, "trenta", 4, "cuaranta", 5, "cincuanta",
            6, "sisanta", 7, "setanta", 8, "uitanta", 9, "novanta");

    // Prefixo ta os numeros de 21 a 29: "venti-" (ventiun, ventidós...).
    private static final Map<Integer, String> decenasEspecials = Map.of(2, "venti");

    // Unidaz con o tono grafico en as formas soldadas venti- (ventidós, ventitrés,
    // ventiséis), seguntes a Biquipedia. As unidaz sueltas van sin tono.
    private static final Map<Integer, String> unidazSoldadas = Map.of(
            2, "d\u00F3s", 3, "tr\u00E9s", 6, "s\u00E9is");

    // Numeros de 11 a 19: formas irregulars/soldadas en aragonés. 11-15 son formas
    // simples; 16-19 leva o prefixo "deci-".
    private static final Map<Integer, String> decenasEspecialsUno = Map.of(
            1, "once", 2, "dotze", 3, "tretze", 4, "catorze", 5, "quince",
            6, "decis\u00E9is", 7, "decisiete", 8, "deciueito", 9, "decinueu");

    // Centenas: "cient" ta 100; ta 200-900 se gosa "X cientos".
    private static final Map<Integer, String> centenasEspecials = Map.of(1, "cient");

    /** Sufixo d'escala por rango de posicions (singular/plural an do aplica). */
    private record Sufixo(int desde, int hasta, String singular, String plural) {
        boolean cubre(int posicion) {
            return posicion >= desde && posicion <= hasta;
        }
    }

    private static final Sufixo[] sufixos = {
        new Sufixo(3, 3, " cientos", " cientos"),
        new Sufixo(4, 6, " mil", " mil"),
        new Sufixo(7, 12, " mili\u00F3n", " milions"),
        new Sufixo(13, 18, " bill\u00F3n", " billons"),
        new Sufixo(19, 24, " trill\u00F3n", " trillons"),
    };

    private CardinalRulesAn() {
    }

    /** Construye o conchunto de reglas ta todas as posicions soportadas. */
    public static RuleSet ruleSet() {
        RuleSet rules = new RuleSet();
        for (int posicion = POSICION_MIN; posicion <= POSICION_MAX; posicion++) {
            switch (posicion) {
                case 1 -> registrarUnidaz(rules);
                case 2 -> registrarDecenas(rules);
                case 3 -> registrarCentenas(rules);
                default -> registrarComun(rules, posicion);
            }
        }
        return rules;
    }

    private static Sufixo getSufixo(int posicion) {
        for (Sufixo sufixo : sufixos) {
            if (sufixo.cubre(posicion)) {
                return sufixo;
            }
        }
        throw new MissingResourceException(
                "Cannot found class suffix for position: " + posicion,
                CardinalRulesAn.class.getName(), "getSufixo");
    }

    // Unidaz (posición 1). O 0 nomás suena "zero" quan ye o numero completo.
    private static void registrarUnidaz(RuleSet rules) {
        for (int dichito = DICHITO_MIN; dichito <= DICHITO_MAX; dichito++) {
            int d = dichito;
            rules.put(1, dichito, (number, recurse) ->
                    (d == 0 && number.size() != 1) ? "" : unidaz.get(d));
        }
    }

    // Decenas (posición 2): 10-19 formas soldadas, venti-, y "X y unidat".
    private static void registrarDecenas(RuleSet rules) {
        for (int dichito = DICHITO_MIN; dichito <= DICHITO_MAX; dichito++) {
            int d = dichito;
            switch (dichito) {
                // Numeros de 10 a 19: formas irregulars soldadas, sin conector.
                case 1 -> rules.put(2, dichito, (number, recurse) -> {
                    int u = number.lastDigit();
                    return u == 0 ? decenas.get(d) : decenasEspecialsUno.get(u);
                });
                // Numeros de 20 a 29: prefixo "venti" soldau. As unidaz 2/3/6 leva
                // tono grafico en a forma soldada (ventidós, ventitrés, ventiséis).
                case 2 -> rules.put(2, dichito, (number, recurse) -> {
                    if (number.lastDigit() == 0) {
                        return decenas.get(d);
                    }
                    int u = number.lastDigit();
                    String unidat = unidazSoldadas.containsKey(u)
                            ? unidazSoldadas.get(u)
                            : recurse.apply(number.lastDigits(1));
                    return decenasEspecials.get(d) + unidat;
                });
                // Numeros de 30 a 99: decena, conector "y" y unidat.
                default -> rules.put(2, dichito, (number, recurse) ->
                        cardinalDecenas(number, recurse, d));
            }
        }
    }

    // "trenta y tres", "cuaranta", etc. (decenas 3-9).
    private static String cardinalDecenas(
            NumberValue number, Function<NumberValue, String> recurse, int decena) {
        StringBuilder cardinal = new StringBuilder(decenas.get(decena));
        if (number.lastDigit() != 0) {
            cardinal.append(SEPARADOR_DECENAS).append(recurse.apply(number.lastDigits(1)));
        }
        return cardinal.toString();
    }

    // Centenas (posición 3): 1 -> cient (u "cient" + resto), resto via caso comun.
    private static void registrarCentenas(RuleSet rules) {
        for (int dichito = DICHITO_MIN; dichito <= DICHITO_MAX; dichito++) {
            int d = dichito;
            switch (dichito) {
                // 100-199: "cient" (exauto) u "cient" + resto (cient uno, cient vente...).
                case 1 -> rules.put(3, dichito, (number, recurse) -> {
                    NumberValue numResto = number.lastDigits(2);
                    String cient = centenasEspecials.get(d);
                    return numResto.equalsInt(0) ? cient : cient + " " + recurse.apply(numResto);
                });
                // 200-999: "X cientos" (dos cientos, tres cientos...) + resto opcional.
                default -> rules.put(3, dichito, (number, recurse) -> cardinalComun(number, recurse));
            }
        }
    }

    // Posicions 4+ (millars, milions, ...): todos os dichitos usan o caso comun.
    private static void registrarComun(RuleSet rules, int posicion) {
        for (int dichito = DICHITO_MIN; dichito <= DICHITO_MAX; dichito++) {
            rules.put(posicion, dichito, (number, recurse) -> cardinalComun(number, recurse));
        }
    }

    /**
     * Caso comun d'escala larga: desepara o grupo d'orden alto d'o suyo resto,
     * aplica o sufixo (singular/plural), a apocope de "uno" -> "un" y a regla de
     * "mil" (un solo millar se diz "mil", no "un mil").
     */
    private static String cardinalComun(NumberValue number, Function<NumberValue, String> recurse) {
        StringBuilder cardinal = new StringBuilder();

        int posicion = number.size();
        Sufixo sufixo = getSufixo(posicion);

        // O grupo d'orden alto ocupa dende a posición actual dica l'inicio d'o rango;
        // o resto son os dichitos por debaixo d'ixe sufixo.
        int dichitosGrupo = posicion - sufixo.desde() + 1;
        NumberValue numGrupo = number.firstDigits(dichitosGrupo);
        NumberValue numResto = number.lastDigits(sufixo.desde() - 1);

        // Apocope: "ventiuno" -> "ventiún" (o tono se manién), "uno" final -> "un".
        String grupoCardinal = recurse.apply(numGrupo)
                .replaceAll("ventiuno", "venti\u00FAn")
                .replaceAll("uno\\b", "un");

        boolean esMilSingular = numGrupo.equalsInt(1) && sufixo.singular().trim().equals("mil");
        if (!esMilSingular) {
            cardinal.append(grupoCardinal);
        }

        // Plural d'o sufixo fueras que o grupo siga exautament 1.
        cardinal.append(numGrupo.equalsInt(1) ? sufixo.singular() : sufixo.plural());

        if (!numResto.equalsInt(0)) {
            cardinal.append(' ').append(recurse.apply(numResto));
        }

        return cardinal.toString().trim();
    }
}
