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

package org.numerals.rules.es;

import java.util.Map;
import java.util.MissingResourceException;
import java.util.function.Function;

import org.numerals.NumberValue;
import org.numerals.RuleSet;

/**
 * Reglas de cardinales para el idioma Espanol (es).
 *
 * <p>Puerto directo de la clase Groovy {@code org.numerals.rules.es.CardinalRules}.
 * La estructura (tablas de unidades/decenas/centenas + tabla de sufijos por rango
 * de posiciones + composicion recursiva) se conserva; lo que cambia es la forma:
 * las closures de Groovy son ahora lambdas {@link org.numerals.CardinalRule} y los
 * Map literales son {@link java.util.Map#of}.
 *
 * <p>Convencion del proyecto: los identificadores de cada idioma van en su propia
 * lengua (unidades, decenas, sufijos, getSufijo...).
 */
public final class CardinalRulesEs {

    private static final int POSICION_MIN = 1;
    private static final int POSICION_MAX = 24;
    private static final int DIGITO_MIN = 0;
    private static final int DIGITO_MAX = 9;
    private static final String SEPARADOR_DECENAS = " y ";

    private static final Map<Integer, String> unidades = Map.of(
            0, "cero", 1, "uno", 2, "dos", 3, "tres", 4, "cuatro",
            5, "cinco", 6, "seis", 7, "siete", 8, "ocho", 9, "nueve");

    private static final Map<Integer, String> decenas = Map.of(
            1, "diez", 2, "veinte", 3, "treinta", 4, "cuarenta", 5, "cincuenta",
            6, "sesenta", 7, "setenta", 8, "ochenta", 9, "noventa");

    private static final Map<Integer, String> decenasEspeciales = Map.of(1, "dieci", 2, "veinti");

    // Formas contraidas que llevan tilde por ser agudas terminadas en vocal/-s:
    // dieciseis->dieciséis, veintidos->veintidós, veintitres->veintitrés, veintiseis->veintiséis.
    // Solo aplican al COMPONER con dieci-/veinti-; en aislado "dos"/"tres"/"seis" no llevan tilde.
    private static final Map<Integer, String> unidadesContraidas = Map.of(2, "dós", 3, "trés", 6, "séis");

    private static String unidadContraida(int digito) {
        return unidadesContraidas.getOrDefault(digito, unidades.get(digito));
    }

    private static final Map<Integer, String> decenasEspecialesUno = Map.of(
            1, "once", 2, "doce", 3, "trece", 4, "catorce", 5, "quince");

    // "cien" cuando el resto es 0, "ciento" en composicion; comparten el digito 1.
    private static final String CIEN = "cien";
    private static final String CIENTO = "ciento";
    private static final Map<Integer, String> centenasEspeciales = Map.of(
            5, "quinientos", 7, "setecientos", 9, "novecientos");

    /** Sufijo de escala por rango de posiciones (singular/plural donde aplica). */
    private record Sufijo(int desde, int hasta, String singular, String plural) {
        boolean cubre(int posicion) {
            return posicion >= desde && posicion <= hasta;
        }
    }

    private static final Sufijo[] sufijos = {
        new Sufijo(3, 3, "cientos", "cientos"),
        new Sufijo(4, 6, " mil", " mil"),
        new Sufijo(7, 12, " millón", " millones"),
        new Sufijo(13, 18, " billón", " billones"),
        new Sufijo(19, 24, " trillón", " trillones"),
    };

    private CardinalRulesEs() {
    }

    /** Construye el conjunto de reglas para todas las posiciones soportadas. */
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

    private static Sufijo getSufijo(int posicion) {
        for (Sufijo sufijo : sufijos) {
            if (sufijo.cubre(posicion)) {
                return sufijo;
            }
        }
        throw new MissingResourceException(
                "No hay sufijo para la posicion: " + posicion,
                CardinalRulesEs.class.getName(), "getSufijo");
    }

    // Unidades (posicion 1). El 0 solo suena "cero" cuando es el numero completo.
    private static void registrarUnidades(RuleSet rules) {
        for (int digito = DIGITO_MIN; digito <= DIGITO_MAX; digito++) {
            int d = digito;
            rules.put(1, digito, (number, recurse) ->
                    (d == 0 && number.size() != 1) ? "" : unidades.get(d));
        }
    }

    // Decenas (posicion 2): teens (10-15 palabra propia, 16-19 dieci-), veinti-, y "X y unidad".
    private static void registrarDecenas(RuleSet rules) {
        for (int digito = DIGITO_MIN; digito <= DIGITO_MAX; digito++) {
            int d = digito;
            switch (digito) {
                case 1 -> rules.put(2, digito, (number, recurse) -> {
                    int u = number.lastDigit();
                    if (u == 0) {
                        return decenas.get(d);
                    }
                    if (u >= 1 && u <= 5) {
                        return decenasEspecialesUno.get(u);
                    }
                    // dieci- + unidad (6-9): dieciseis lleva tilde -> dieciséis.
                    return decenasEspeciales.get(d) + unidadContraida(u);
                });
                case 2 -> rules.put(2, digito, (number, recurse) -> {
                    int u = number.lastDigit();
                    // veinti- + unidad (1-9): veintidos/veintitres/veintiseis llevan tilde.
                    return u == 0 ? decenas.get(d) : decenasEspeciales.get(d) + unidadContraida(u);
                });
                default -> rules.put(2, digito, (number, recurse) ->
                        cardinalDecenas(number, recurse, d));
            }
        }
    }

    // "treinta y tres", "cuarenta", etc. (decenas 3-9).
    private static String cardinalDecenas(
            NumberValue number, Function<NumberValue, String> recurse, int decena) {
        StringBuilder cardinal = new StringBuilder(decenas.get(decena));
        if (number.lastDigit() != 0) {
            cardinal.append(SEPARADOR_DECENAS).append(recurse.apply(number.lastDigits(1)));
        }
        return cardinal.toString();
    }

    // Centenas (posicion 3): 1 -> cien/ciento, {5,7,9} irregulares, resto via caso comun.
    private static void registrarCentenas(RuleSet rules) {
        for (int digito = DIGITO_MIN; digito <= DIGITO_MAX; digito++) {
            int d = digito;
            switch (digito) {
                case 1 -> rules.put(3, digito, (number, recurse) -> {
                    NumberValue resto = number.lastDigits(2);
                    return resto.equalsInt(0) ? CIEN : CIENTO + " " + recurse.apply(resto);
                });
                case 5, 7, 9 -> rules.put(3, digito, (number, recurse) -> {
                    NumberValue resto = number.lastDigits(2);
                    String centena = centenasEspeciales.get(d);
                    return resto.equalsInt(0) ? centena : centena + " " + recurse.apply(resto);
                });
                default -> rules.put(3, digito, (number, recurse) -> cardinalComun(number, recurse));
            }
        }
    }

    // Posiciones 4+ (millares, millones, ...): todos los digitos usan el caso comun.
    private static void registrarComun(RuleSet rules, int posicion) {
        for (int digito = DIGITO_MIN; digito <= DIGITO_MAX; digito++) {
            rules.put(posicion, digito, (number, recurse) -> cardinalComun(number, recurse));
        }
    }

    /**
     * Caso comun de escala larga: separa el grupo de orden alto de su resto,
     * aplica el sufijo (singular/plural), la apocope de "uno" -> "un" y la regla
     * de "mil" (un solo millar se dice "mil", no "un mil").
     */
    private static String cardinalComun(NumberValue number, Function<NumberValue, String> recurse) {
        StringBuilder cardinal = new StringBuilder();

        int pos = number.size();
        Sufijo sufijo = getSufijo(pos);

        // El grupo de orden alto ocupa desde la posicion actual hasta el inicio del rango;
        // el resto son los digitos por debajo de ese sufijo.
        int digitosGrupo = pos - sufijo.desde() + 1;
        NumberValue numGrupo = number.firstDigits(digitosGrupo);
        NumberValue numResto = number.lastDigits(sufijo.desde() - 1);

        // Apocope: "veintiuno" -> "veintiun", "uno" final -> "un".
        String grupoCardinal = recurse.apply(numGrupo)
                .replaceAll("veintiuno", "veintiún")
                .replaceAll("uno\\b", "un");

        boolean esMilSingular = numGrupo.equalsInt(1) && sufijo.singular().trim().equals("mil");
        if (!esMilSingular) {
            cardinal.append(grupoCardinal);
        }

        // Plural del sufijo salvo que el grupo sea exactamente 1.
        cardinal.append(numGrupo.equalsInt(1) ? sufijo.singular() : sufijo.plural());

        if (!numResto.equalsInt(0)) {
            cardinal.append(' ').append(recurse.apply(numResto));
        }

        return cardinal.toString().trim();
    }
}
