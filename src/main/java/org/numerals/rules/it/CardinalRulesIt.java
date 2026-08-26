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

package org.numerals.rules.it;

import java.util.Map;
import java.util.MissingResourceException;
import java.util.function.Function;

import org.numerals.NumberValue;
import org.numerals.RuleSet;

/**
 * Regole di cardinali per l'idioma Italiano (it).
 *
 * <p>Porto diretto della classe Groovy {@code org.numerals.rules.it.CardinalRules}.
 * La struttura (tabelle di unita/decine/centinaia + tabella di suffissi per
 * intervallo di posizioni + composizione ricorsiva) si conserva; cambia la forma:
 * le closure Groovy sono ora lambda {@link org.numerals.CardinalRule} e i Map
 * letterali sono {@link java.util.Map#of}/{@link java.util.Map#ofEntries}.
 *
 * <p>Particolarita dell'Italiano trattate qui:
 * <ul>
 *   <li>Elisione della vocale finale della decina davanti a "uno" e "otto"
 *       (ventuno, ventotto, trentuno).</li>
 *   <li>Decine da 10 a 19 irregolari (undici .. diciannove).</li>
 *   <li>Centinaia con "cento" invariabile, seguita dal resto con uno spazio.</li>
 *   <li>1000 e "mille" (non "unomille"); i multipli usano "mila" (duemila),
 *       attaccati al resto senza spazio.</li>
 *   <li>Milioni/miliardi separati da uno spazio; "uno" -&gt; "un" davanti a loro.</li>
 *   <li>La scala alterna le forme in -ione e in -iardo ogni 3 cifre.</li>
 * </ul>
 *
 * <p>Convenzione del progetto: gli identificatori di ogni lingua restano nella
 * propria lingua (unita, decine, suffissi, getSuffisso...).
 */
public final class CardinalRulesIt {

    private static final String SEPARATORE_REGOLE = "/";
    private static final int POSIZIONE_MIN = 1;
    private static final int POSIZIONE_MAX = 24;
    private static final int CIFRA_MIN = 0;
    private static final int CIFRA_MAX = 9;

    private static final Map<Integer, String> unita = Map.of(
            0, "zero", 1, "uno", 2, "due", 3, "tre", 4, "quattro",
            5, "cinque", 6, "sei", 7, "sette", 8, "otto", 9, "nove");

    private static final Map<Integer, String> decine = Map.of(
            1, "dieci", 2, "venti", 3, "trenta", 4, "quaranta", 5, "cinquanta",
            6, "sessanta", 7, "settanta", 8, "ottanta", 9, "novanta");

    // Decine da 10 a 19: irregolari in Italiano.
    private static final Map<Integer, String> decineSpeciali = Map.ofEntries(
            Map.entry(0, "dieci"), Map.entry(1, "undici"), Map.entry(2, "dodici"),
            Map.entry(3, "tredici"), Map.entry(4, "quattordici"), Map.entry(5, "quindici"),
            Map.entry(6, "sedici"), Map.entry(7, "diciassette"), Map.entry(8, "diciotto"),
            Map.entry(9, "diciannove"));

    // Cifre delle unita che provocano l'elisione della vocale finale della decina.
    private static final java.util.List<Integer> cifreElisione = java.util.List.of(1, 8);

    // Ogni centinaio: "cento" e invariabile e si antepone (duecento, trecento, ...).
    private static final Map<Integer, String> centinaia = Map.of(
            1, "cento", 2, "duecento", 3, "trecento", 4, "quattrocento", 5, "cinquecento",
            6, "seicento", 7, "settecento", 8, "ottocento", 9, "novecento");

    /**
     * Suffisso di scala per intervallo di posizioni. Il valore {@code suffisso}
     * conserva il formato Groovy "singolare/plurale" e viene diviso su "/".
     */
    private record Suffisso(int da, int a, String suffisso) {
        boolean copre(int posizione) {
            return posizione >= da && posizione <= a;
        }
    }

    // Scala italiana: una parola di scala ogni 3 cifre oltre le migliaia,
    // alternando le forme in -ione (milione, bilione, ...) e in -iardo
    // (miliardo, biliardo, ...). Ogni intervallo copre 3 posizioni.
    private static final Suffisso[] suffissi = {
        new Suffisso(4, 6, "mille" + SEPARATORE_REGOLE + "mila"),
        new Suffisso(7, 9, " milione" + SEPARATORE_REGOLE + " milioni"),
        new Suffisso(10, 12, " miliardo" + SEPARATORE_REGOLE + " miliardi"),
        new Suffisso(13, 15, " bilione" + SEPARATORE_REGOLE + " bilioni"),
        new Suffisso(16, 18, " biliardo" + SEPARATORE_REGOLE + " biliardi"),
        new Suffisso(19, 21, " trilione" + SEPARATORE_REGOLE + " trilioni"),
        new Suffisso(22, 24, " triliardo" + SEPARATORE_REGOLE + " triliardi"),
    };

    private CardinalRulesIt() {
    }

    /** Costruisce il set di regole per tutte le posizioni supportate. */
    public static RuleSet ruleSet() {
        RuleSet rules = new RuleSet();
        for (int posizione = POSIZIONE_MIN; posizione <= POSIZIONE_MAX; posizione++) {
            switch (posizione) {
                case 1 -> registraUnita(rules);
                case 2 -> registraDecine(rules);
                case 3 -> registraCentinaia(rules);
                default -> registraComune(rules, posizione);
            }
        }
        return rules;
    }

    private static Suffisso getSuffisso(int posizione) {
        for (Suffisso suffisso : suffissi) {
            if (suffisso.copre(posizione)) {
                return suffisso;
            }
        }
        throw new MissingResourceException(
                "Cannot found class suffix for position:" + posizione,
                CardinalRulesIt.class.getName(), "getSuffisso");
    }

    // Unita (posizione 1). Lo 0 suona "zero" solo quando e il numero completo.
    private static void registraUnita(RuleSet rules) {
        for (int cifra = CIFRA_MIN; cifra <= CIFRA_MAX; cifra++) {
            int c = cifra;
            rules.put(1, cifra, (number, recurse) ->
                    (c == 0 && number.size() != 1) ? "" : unita.get(c));
        }
    }

    // Decine (posizione 2): teens irregolari (10-19), poi decina + unita attaccata,
    // con elisione della vocale finale davanti a uno/otto.
    private static void registraDecine(RuleSet rules) {
        for (int cifra = CIFRA_MIN; cifra <= CIFRA_MAX; cifra++) {
            int c = cifra;
            switch (cifra) {
                // Decine da 10 a 19: una sola parola irregolare, senza unita separata.
                case 1 -> rules.put(2, cifra, (number, recurse) ->
                        decineSpeciali.get(number.lastDigit()));
                // 20 a 90: parola della decina, elisa davanti a uno/otto, piu unita attaccata.
                default -> rules.put(2, cifra, (number, recurse) ->
                        cardinaleDecine(number, recurse, c));
            }
        }
    }

    private static String cardinaleDecine(
            NumberValue number, Function<NumberValue, String> recurse, int cifra) {
        StringBuilder cardinal = new StringBuilder();
        String decina = decine.get(cifra);
        if (number.lastDigit() != 0) {
            if (cifreElisione.contains(number.lastDigit())) {
                decina = decina.substring(0, decina.length() - 1);
            }
            cardinal.append(decina);
            cardinal.append(recurse.apply(number.lastDigits(1)));
        } else {
            cardinal.append(decina);
        }
        return cardinal.toString();
    }

    // Centinaia (posizione 3): "cento" invariabile, anteposta al resto con uno spazio.
    private static void registraCentinaia(RuleSet rules) {
        for (int cifra = CIFRA_MIN; cifra <= CIFRA_MAX; cifra++) {
            int c = cifra;
            rules.put(3, cifra, (number, recurse) -> {
                String centinaio = centinaia.get(c);
                NumberValue numResto = number.lastDigits(2);
                return numResto.equalsInt(0)
                        ? centinaio
                        : centinaio + " " + recurse.apply(numResto);
            });
        }
    }

    // Posizioni 4+ (migliaia, milioni, ...): tutte le cifre usano il caso comune.
    private static void registraComune(RuleSet rules, int posizione) {
        for (int cifra = CIFRA_MIN; cifra <= CIFRA_MAX; cifra++) {
            rules.put(posizione, cifra, (number, recurse) -> cardinaleComune(number, recurse));
        }
    }

    /**
     * Caso comune (dalle migliaia in avanti): separa il gruppo di ordine alto dal
     * suo resto, applica il suffisso (singolare/plurale), l'elisione "uno" -&gt; "un"
     * davanti ai suffissi separati da spazio e la regola di "mille" (un solo
     * migliaio si dice "mille", non "unomille").
     */
    private static String cardinaleComune(
            NumberValue number, Function<NumberValue, String> recurse) {
        StringBuilder cardinal = new StringBuilder();

        int posizione = number.size();
        Suffisso suffisso = getSuffisso(posizione);
        String[] arrSuffissi = suffisso.suffisso().split(SEPARATORE_REGOLE);

        // Il gruppo di ordine alto va dalla posizione corrente all'inizio dell'intervallo;
        // il resto sono le cifre sotto quel suffisso.
        int cifreGruppo = posizione - suffisso.da() + 1;
        NumberValue numGruppo = number.firstDigits(cifreGruppo);
        NumberValue numResto = number.lastDigits(suffisso.da() - 1);

        boolean gruppoUno = numGruppo.equalsInt(1);
        boolean suffissoMigliaia = suffisso.suffisso().contains("mille");

        String gruppoCardinale = recurse.apply(numGruppo);

        // Elisione di "uno" -> "un" davanti a milione/miliardo/bilione (un milione,
        // un miliardo): vale solo per i suffissi separati da spazio, non per le migliaia.
        if (!suffissoMigliaia) {
            gruppoCardinale = gruppoCardinale.replaceAll("uno\\b", "un");
        }

        // "1000" si dice "mille" (non "unomille"): si omette "uno" quando il gruppo
        // e esattamente 1 e il suffisso e quello delle migliaia.
        if (!(gruppoUno && suffissoMigliaia)) {
            cardinal.append(gruppoCardinale);
        }

        if (arrSuffissi.length > 1 && !gruppoUno) {
            cardinal.append(arrSuffissi[1]);
        } else {
            cardinal.append(arrSuffissi[0]);
        }

        if (!numResto.equalsInt(0)) {
            // Le migliaia si attaccano al resto senza spazio (duemilacinquecento);
            // milioni e miliardi lo separano con uno spazio (un milione cento).
            if (!suffissoMigliaia) {
                cardinal.append(" ");
            }
            cardinal.append(recurse.apply(numResto));
        }

        return cardinal.toString().trim();
    }
}
