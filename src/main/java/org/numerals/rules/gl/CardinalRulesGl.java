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

package org.numerals.rules.gl;

import java.util.Map;
import java.util.MissingResourceException;
import java.util.function.Function;

import org.numerals.NumberValue;
import org.numerals.RuleSet;

/**
 * Regras de cardinais para o idioma Galego (gl).
 *
 * <p>Porto directo da clase Groovy {@code org.numerals.rules.gl.CardinalRules}.
 * A estrutura (taboas de unidades/decenas/centenas + taboa de sufixos por rango
 * de posicions + composicion recursiva) conservase; o que cambia e a forma: as
 * closures de Groovy son agora lambdas {@link org.numerals.CardinalRule} e os
 * Map literais son {@link java.util.Map#of}.
 *
 * <p>Particularidades do Galego tratadas aqui:
 * <ul>
 *   <li>O conector "e" liga decenas e unidades (vinte e un), centenas e o resto
 *       (cento e vinte), e un grupo e o seu resto (mil e un).</li>
 *   <li>"cen" para exactamente 100, "cento" cando vai seguido de mais (cento e un).</li>
 *   <li>Cada centena ten a sua propia palabra (douscentos, trescentos, ...).</li>
 *   <li>1000 e "mil" (non "un mil").</li>
 *   <li>Escala longa (millon = 10^6, billon = 10^12, trillon = 10^18).</li>
 * </ul>
 *
 * <p>Convencion do proxecto: os identificadores de cada idioma van na sua propia
 * lingua (unidades, decenas, sufixos, getSufixo...).
 */
public final class CardinalRulesGl {

    private static final int POSICION_MIN = 1;
    private static final int POSICION_MAX = 24;
    private static final int DIXITO_MIN = 0;
    private static final int DIXITO_MAX = 9;
    private static final String SEPARADOR_DECENAS = " e ";

    private static final Map<Integer, String> unidades = Map.of(
            0, "cero", 1, "un", 2, "dous", 3, "tres", 4, "catro",
            5, "cinco", 6, "seis", 7, "sete", 8, "oito", 9, "nove");

    private static final Map<Integer, String> decenas = Map.of(
            1, "dez", 2, "vinte", 3, "trinta", 4, "corenta", 5, "cincuenta",
            6, "sesenta", 7, "setenta", 8, "oitenta", 9, "noventa");

    // Decenas de 10 a 19: irregulares en Galego (dez, once, ... dezanove).
    private static final Map<Integer, String> decenasEspeciais = Map.of(
            0, "dez", 1, "once", 2, "doce", 3, "trece", 4, "catorce",
            5, "quince", 6, "dezaseis", 7, "dezasete", 8, "dezaoito", 9, "dezanove");

    // Cada centena ten a sua palabra. O 1 e especial: cen (exacto) / cento (con resto).
    private static final String CEN = "cen";
    private static final String CENTO = "cento";
    private static final Map<Integer, String> centenas = Map.of(
            2, "douscentos", 3, "trescentos", 4, "catrocentos", 5, "cincocentos",
            6, "seiscentos", 7, "setecentos", 8, "oitocentos", 9, "novecentos");

    /** Sufixo de escala por rango de posicions (singular/plural onde aplica). */
    private record Sufixo(int desde, int hasta, String singular, String plural) {
        boolean cubre(int posicion) {
            return posicion >= desde && posicion <= hasta;
        }
    }

    private static final Sufixo[] sufixos = {
        new Sufixo(4, 6, " mil", " mil"),
        new Sufixo(7, 12, " mill\u00F3n", " mill\u00F3ns"),
        new Sufixo(13, 18, " bill\u00F3n", " bill\u00F3ns"),
        new Sufixo(19, 24, " trill\u00F3n", " trill\u00F3ns"),
    };

    private CardinalRulesGl() {
    }

    /** Construe o conxunto de regras para todas as posicions soportadas. */
    public static RuleSet ruleSet() {
        RuleSet rules = new RuleSet();
        for (int posicion = POSICION_MIN; posicion <= POSICION_MAX; posicion++) {
            switch (posicion) {
                case 1 -> registrarUnidades(rules);
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
                CardinalRulesGl.class.getName(), "getSufixo");
    }

    // Unidades (posicion 1). O 0 so soa "cero" cando e o numero completo.
    private static void registrarUnidades(RuleSet rules) {
        for (int dixito = DIXITO_MIN; dixito <= DIXITO_MAX; dixito++) {
            int d = dixito;
            rules.put(1, dixito, (number, recurse) ->
                    (d == 0 && number.size() != 1) ? "" : unidades.get(d));
        }
    }

    // Decenas (posicion 2): teens (10-19 palabra propia irregular) e "X e unidade".
    private static void registrarDecenas(RuleSet rules) {
        for (int dixito = DIXITO_MIN; dixito <= DIXITO_MAX; dixito++) {
            int d = dixito;
            if (dixito == 1) {
                // Decenas de 10 a 19: unha unica palabra irregular, sen conector "e".
                rules.put(2, dixito, (number, recurse) -> decenasEspeciais.get(number.lastDigit()));
            } else {
                // 20 a 90: palabra, opcionalmente "e" + unidade.
                rules.put(2, dixito, (number, recurse) -> cardinalDecenas(number, recurse, d));
            }
        }
    }

    // "trinta e tres", "corenta", etc. (decenas 2-9).
    private static String cardinalDecenas(
            NumberValue number, Function<NumberValue, String> recurse, int decena) {
        StringBuilder cardinal = new StringBuilder(decenas.get(decena));
        if (number.lastDigit() != 0) {
            cardinal.append(SEPARADOR_DECENAS).append(recurse.apply(number.lastDigits(1)));
        }
        return cardinal.toString();
    }

    // Centenas (posicion 3): 1 -> cen/cento, resto cada quen a sua palabra propia.
    private static void registrarCentenas(RuleSet rules) {
        for (int dixito = DIXITO_MIN; dixito <= DIXITO_MAX; dixito++) {
            int d = dixito;
            if (dixito == 1) {
                rules.put(3, dixito, (number, recurse) -> {
                    NumberValue numResto = number.lastDigits(2);
                    return numResto.equalsInt(0)
                            ? CEN
                            : CENTO + SEPARADOR_DECENAS + recurse.apply(numResto);
                });
            } else {
                rules.put(3, dixito, (number, recurse) -> {
                    NumberValue numResto = number.lastDigits(2);
                    String centena = centenas.get(d);
                    return numResto.equalsInt(0)
                            ? centena
                            : centena + SEPARADOR_DECENAS + recurse.apply(numResto);
                });
            }
        }
    }

    // Posicions 4+ (millares, millons, ...): todos os dixitos usan o caso comun.
    private static void registrarComun(RuleSet rules, int posicion) {
        for (int dixito = DIXITO_MIN; dixito <= DIXITO_MAX; dixito++) {
            rules.put(posicion, dixito, (number, recurse) -> cardinalComun(number, recurse));
        }
    }

    /**
     * Caso comun de escala longa: separa o grupo de orde alta do seu resto,
     * aplica o sufixo (singular/plural), a regra de "mil" (un so millar dise
     * "mil", non "un mil") e o conector inter-grupo ("e" ou espazo).
     */
    private static String cardinalComun(NumberValue number, Function<NumberValue, String> recurse) {
        StringBuilder cardinal = new StringBuilder();

        int posicion = number.size();
        Sufixo sufixo = getSufixo(posicion);

        // O grupo de orde alta ocupa dende a posicion actual ate o inicio do rango;
        // o resto son os dixitos por debaixo dese sufixo.
        int dixitosGrupo = posicion - sufixo.desde() + 1;
        NumberValue numGrupo = number.firstDigits(dixitosGrupo);
        NumberValue numResto = number.lastDigits(sufixo.desde() - 1);

        String grupoCardinal = recurse.apply(numGrupo);

        // "1 mil" e "mil" (non "un mil"): omitir "un" cando o grupo e exactamente 1
        // e o sufixo e o singular "mil".
        boolean esMilSingular = numGrupo.equalsInt(1) && sufixo.singular().trim().equals("mil");
        if (!esMilSingular) {
            cardinal.append(grupoCardinal);
        }

        // Plural do sufixo salvo que o grupo sexa exactamente 1.
        cardinal.append(numGrupo.equalsInt(1) ? sufixo.singular() : sufixo.plural());

        if (!numResto.equalsInt(0)) {
            // O Galego liga o ultimo grupo con "e" apenas cando ese resto e
            // menor que 100, ou e un multiplo exacto de 100 (cen/douscentos...). No
            // caso contrario (ex. 234, 567) usase un espazo: "un millon douscentos e trinta...".
            String restoStr = numResto.toString();
            boolean menorQue100 = restoStr.length() <= 2;
            boolean multiploExactoDe100 =
                    restoStr.length() >= 3 && soCerosDespoisDoPrimeiro(restoStr);
            cardinal.append(menorQue100 || multiploExactoDe100 ? SEPARADOR_DECENAS : " ");
            cardinal.append(recurse.apply(numResto));
        }

        return cardinal.toString().trim();
    }

    /** True se todos os caracteres despois do primeiro son '0' (multiplo exacto de 100+). */
    private static boolean soCerosDespoisDoPrimeiro(String restoStr) {
        for (int i = 1; i < restoStr.length(); i++) {
            if (restoStr.charAt(i) != '0') {
                return false;
            }
        }
        return true;
    }
}
