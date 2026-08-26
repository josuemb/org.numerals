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

package org.numerals.rules.pt;

import java.util.Map;
import java.util.MissingResourceException;
import java.util.function.Function;

import org.numerals.NumberValue;
import org.numerals.RuleSet;

/**
 * Regras de cardinais para o idioma Portugues (pt).
 *
 * <p>Porte directo da classe Groovy {@code org.numerals.rules.pt.CardinalRules}.
 * A estrutura (tabelas de unidades/dezenas/centenas + tabela de sufixos por
 * intervalo de posicoes + composicao recursiva) conserva-se; o que muda e a
 * forma: as closures de Groovy sao agora lambdas {@link org.numerals.CardinalRule}
 * e os Map literais sao {@link java.util.Map#of}.
 *
 * <p>Particularidades do Portugues tratadas aqui:
 * <ul>
 *   <li>O conector "e" liga dezenas e unidades (vinte e tres), centenas e o resto
 *       (cento e vinte), e um grupo e o seu resto (mil e um).</li>
 *   <li>"cem" para exatamente 100, "cento" quando seguido de mais (cento e um).</li>
 *   <li>Cada centena tem a sua propria palavra (duzentos, trezentos, ...).</li>
 *   <li>1000 e "mil" (nao "um mil").</li>
 * </ul>
 *
 * <p>Convencao do projeto: os identificadores de cada idioma vao na sua propria
 * lingua (dezenas, sufixos, getSufixo, getRegras..., getCardinalComum...).
 */
public final class CardinalRulesPt {

    private static final int POSICAO_MIN = 1;
    private static final int POSICAO_MAX = 24;
    private static final int DIGITO_MIN = 0;
    private static final int DIGITO_MAX = 9;
    private static final String SEPARADOR_REGRAS = "/";
    private static final String SEPARADOR_DEZENAS = " e ";

    private static final Map<Integer, String> unidades = Map.of(
            0, "zero", 1, "um", 2, "dois", 3, "tres", 4, "quatro",
            5, "cinco", 6, "seis", 7, "sete", 8, "oito", 9, "nove");

    private static final Map<Integer, String> dezenas = Map.of(
            1, "dez", 2, "vinte", 3, "trinta", 4, "quarenta", 5, "cinquenta",
            6, "sessenta", 7, "setenta", 8, "oitenta", 9, "noventa");

    // Dezenas de 10 a 19: irregulares em Portugues (dez, onze, ... dezenove).
    private static final Map<Integer, String> dezenasEspeciais = Map.of(
            0, "dez", 1, "onze", 2, "doze", 3, "treze", 4, "catorze",
            5, "quinze", 6, "dezesseis", 7, "dezessete", 8, "dezoito", 9, "dezenove");

    // Cada centena tem a sua palavra. O 1 e especial: cem (exato) / cento (com resto).
    private static final Map<Integer, String> centenas = Map.of(
            1, "cem" + SEPARADOR_REGRAS + "cento", 2, "duzentos", 3, "trezentos",
            4, "quatrocentos", 5, "quinhentos", 6, "seiscentos", 7, "setecentos",
            8, "oitocentos", 9, "novecentos");

    /** Sufixo de escala por intervalo de posicoes (singular/plural onde aplica). */
    private record Sufixo(int desde, int hasta, String sufixo) {
        boolean cubre(int posicao) {
            return posicao >= desde && posicao <= hasta;
        }
    }

    private static final Sufixo[] sufixos = {
        new Sufixo(4, 6, " mil"),
        new Sufixo(7, 12, " milh\u00E3o" + SEPARADOR_REGRAS + " milh\u00F5es"),
        new Sufixo(13, 18, " bilh\u00E3o" + SEPARADOR_REGRAS + " bilh\u00F5es"),
        new Sufixo(19, 24, " trilh\u00E3o" + SEPARADOR_REGRAS + " trilh\u00F5es"),
    };

    private CardinalRulesPt() {
    }

    /** Constroi o conjunto de regras para todas as posicoes suportadas. */
    public static RuleSet ruleSet() {
        RuleSet rules = new RuleSet();
        for (int posicao = POSICAO_MIN; posicao <= POSICAO_MAX; posicao++) {
            switch (posicao) {
                case 1 -> registrarUnidades(rules);
                case 2 -> registrarDezenas(rules);
                case 3 -> registrarCentenas(rules);
                default -> registrarComum(rules, posicao);
            }
        }
        return rules;
    }

    private static Sufixo getSufixo(int posicao) {
        for (Sufixo sufixo : sufixos) {
            if (sufixo.cubre(posicao)) {
                return sufixo;
            }
        }
        throw new MissingResourceException(
                "Cannot found class suffix for position:" + posicao,
                CardinalRulesPt.class.getName(), "getSufixo");
    }

    // Unidades (posicao 1). O 0 so soa "zero" quando e o numero completo.
    private static void registrarUnidades(RuleSet rules) {
        for (int digito = DIGITO_MIN; digito <= DIGITO_MAX; digito++) {
            int d = digito;
            rules.put(1, digito, (number, recurse) ->
                    (d == 0 && number.size() != 1) ? "" : unidades.get(d));
        }
    }

    // Dezenas (posicao 2): 10-19 palavra irregular unica; 20-90 palavra + opcional "e" + unidade.
    private static void registrarDezenas(RuleSet rules) {
        for (int digito = DIGITO_MIN; digito <= DIGITO_MAX; digito++) {
            int d = digito;
            switch (digito) {
                // Dezenas de 10 a 19: uma unica palavra irregular, sem conector "e".
                case 1 -> rules.put(2, digito, (number, recurse) ->
                        dezenasEspeciais.get(number.lastDigit()));
                // 20 a 90: palavra, opcionalmente "e" + unidade.
                default -> rules.put(2, digito, (number, recurse) ->
                        cardinalDezenas(number, recurse, d));
            }
        }
    }

    private static String cardinalDezenas(
            NumberValue number, Function<NumberValue, String> recurse, int dezena) {
        StringBuilder cardinal = new StringBuilder(dezenas.get(dezena));
        if (number.lastDigit() != 0) {
            cardinal.append(SEPARADOR_DEZENAS).append(recurse.apply(number.lastDigits(1)));
        }
        return cardinal.toString();
    }

    // Centenas (posicao 3): 1 -> cem/cento, resto -> palavra propria, opcional "e" + resto.
    private static void registrarCentenas(RuleSet rules) {
        for (int digito = DIGITO_MIN; digito <= DIGITO_MAX; digito++) {
            int d = digito;
            switch (digito) {
                case 1 -> {
                    String[] cem = centenas.get(d).split(SEPARADOR_REGRAS);
                    rules.put(3, digito, (number, recurse) -> {
                        NumberValue numResto = number.lastDigits(2);
                        return numResto.equalsInt(0)
                                ? cem[0]
                                : cem[1] + SEPARADOR_DEZENAS + recurse.apply(numResto);
                    });
                }
                default -> {
                    String centena = centenas.get(d);
                    rules.put(3, digito, (number, recurse) -> {
                        NumberValue numResto = number.lastDigits(2);
                        return numResto.equalsInt(0)
                                ? centena
                                : centena + SEPARADOR_DEZENAS + recurse.apply(numResto);
                    });
                }
            }
        }
    }

    // Posicoes 4+ (milhares, milhoes, ...): todos os digitos usam o caso comum.
    private static void registrarComum(RuleSet rules, int posicao) {
        for (int digito = DIGITO_MIN; digito <= DIGITO_MAX; digito++) {
            rules.put(posicao, digito, (number, recurse) -> getCardinalComum(number, recurse));
        }
    }

    /**
     * Caso comum de escala longa: separa o grupo de ordem alta do seu resto,
     * aplica o sufixo (singular/plural), a regra de "mil" (um so milhar diz-se
     * "mil", nao "um mil") e o conector "e" entre grupo e resto.
     */
    private static String getCardinalComum(
            NumberValue number, Function<NumberValue, String> recurse) {
        StringBuilder cardinal = new StringBuilder();

        int posicao = number.size();
        Sufixo sufixo = getSufixo(posicao);
        String[] arrSufixos = sufixo.sufixo().split(SEPARADOR_REGRAS);

        // O grupo de ordem alta ocupa desde a posicao atual ate ao inicio do intervalo;
        // o resto sao os digitos abaixo desse sufixo.
        int digitosGrupo = posicao - sufixo.desde() + 1;
        NumberValue numGrupo = number.firstDigits(digitosGrupo);
        NumberValue numResto = number.lastDigits(sufixo.desde() - 1);

        String grupoCardinal = recurse.apply(numGrupo);

        // "1 mil" e "mil" (nao "um mil"): omitir "um" quando o grupo e exatamente 1
        // e o sufixo e o singular "mil".
        if (!(numGrupo.equalsInt(1) && sufixo.sufixo().trim().equals("mil"))) {
            cardinal.append(grupoCardinal);
        }

        if (arrSufixos.length > 1 && !numGrupo.equalsInt(1)) {
            cardinal.append(arrSufixos[1]);
        } else {
            cardinal.append(arrSufixos[0]);
        }

        if (!numResto.equalsInt(0)) {
            // O Portugues liga o ultimo grupo com "e" apenas quando esse resto e
            // menor que 100, ou e um multiplo exato de 100 (cem/duzentos...). Caso
            // contrario (ex. 234, 567) usa-se um espaco: "um milhao duzentos e trinta...".
            String restoStr = numResto.toString();
            boolean menorQue100 = restoStr.length() <= 2;
            boolean multiploExatoDe100 =
                    restoStr.length() >= 3 && soZerosDepoisDoPrimeiro(restoStr);
            cardinal.append(menorQue100 || multiploExatoDe100 ? SEPARADOR_DEZENAS : " ");
            cardinal.append(recurse.apply(numResto));
        }

        return cardinal.toString().trim();
    }

    // True quando todos os digitos apos o primeiro sao '0' (multiplo exato de 100+).
    private static boolean soZerosDepoisDoPrimeiro(String digitos) {
        for (int i = 1; i < digitos.length(); i++) {
            if (digitos.charAt(i) != '0') {
                return false;
            }
        }
        return true;
    }
}
