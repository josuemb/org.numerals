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

package org.numerals.rules.it

import org.numerals.Number

/**
 * Classe che contiene le regole per ottenere i cardinali di un numero in
 * Italiano.<br/><br/>
 *
 * La proprieta statica "rules" restituisce le regole per ottenere i cardinali.
 *
 * Particolarita dell'Italiano trattate qui:
 * - Elisione della vocale finale della decina davanti a "uno" e "otto":
 *   venti + uno -> ventuno, venti + otto -> ventotto, trenta + uno -> trentuno.
 * - Le decine da 10 a 19 sono irregolari (undici, dodici, ... diciannove).
 * - Le centinaia: cento e invariabile (cento, duecento, trecento, ...),
 *   seguita dal resto con uno spazio (cento uno, duecento venti).
 * - 1000 e "mille" (non "unomille"); i multipli usano "mila" (duemila).
 * - Le migliaia si attaccano senza spazio (duemilacinquecento); milioni e
 *   miliardi usano uno spazio (un milione, due milioni).
 *
 * @author Josue Martinez Buenrrostro (@josuemb)
 */
class CardinalRules {

	private static final SEPARATORE_REGOLE = "/"
	private static final POSIZIONE_MIN = 1
	private static final POSIZIONE_MAX = 24
	private static final CIFRA_MIN = 0
	private static final CIFRA_MAX = 9
	private static final Map unita = [0:"zero", 1:"uno", 2:"due",3:"tre",4:"quattro",5:"cinque",6:"sei",7:"sette",8:"otto",9:"nove"]
	private static final Map decine = [1:"dieci", 2:"venti",3:"trenta",4:"quaranta",5:"cinquanta",6:"sessanta",7:"settanta",8:"ottanta",9:"novanta"]
	// Decine da 10 a 19: irregolari in Italiano.
	private static final Map decineSpeciali = [0:"dieci",1:"undici",2:"dodici",3:"tredici",4:"quattordici",5:"quindici",6:"sedici",7:"diciassette",8:"diciotto",9:"diciannove"]
	// Cifre delle unita che provocano l'elisione della vocale finale della decina.
	private static final List cifreElisione = [1, 8]
	// Ogni centinaio: "cento" e invariabile e si antepone (duecento, trecento, ...).
	private static final Map centinaia = [1:"cento",2:"duecento",3:"trecento",4:"quattrocento",5:"cinquecento",6:"seicento",7:"settecento",8:"ottocento",9:"novecento"]
	private static final List suffissi = [
		[intervallo:4..6,suffisso:"mille${SEPARATORE_REGOLE}mila"],
		[intervallo:7..12,suffisso:" milione$SEPARATORE_REGOLE milioni"],
		[intervallo:13..18,suffisso:" miliardo$SEPARATORE_REGOLE miliardi"],
		[intervallo:19..24,suffisso:" bilione$SEPARATORE_REGOLE bilioni"]
	]
	static final Map rules = [:]

	// Stabilisce le regole per ogni posizione.
	static {
		(POSIZIONE_MIN..POSIZIONE_MAX).each{ posizione ->
			switch( posizione ) {
				case 1: rules[posizione] = getRegoleUnita()
					break
				case 2: rules[posizione] = getRegoleDecine()
					break
				case 3: rules[posizione] = getRegoleCentinaia()
					break
				default: rules[posizione] = getRegoleComuni()
			}
		}
	}

	/**
	 * Consulta i suffissi per ogni intervallo di posizioni.
	 * @param posizione Posizione.
	 * @return Mappa con l'intervallo di ogni suffisso e il suffisso stesso.
	 */
	private static Map getSuffisso(int posizione){
		Map suffissiTmp = suffissi.find{it.intervallo.isCase(posizione)}
		if(!suffissiTmp) {
			throw new MissingResourceException("Cannot found class suffix for position:$posizione", CardinalRules.class.name, "getSuffisso")
		}
		return suffissiTmp
	}

	/**
	 * Ottiene le regole per le unita (caso speciale).
	 * @return Mappa con le regole per determinare i numeri.
	 */
	private static Map getRegoleUnita(){
		Map regoleUnita = [:]
		(CIFRA_MIN..CIFRA_MAX).each { cifra ->
			regoleUnita[cifra] = { number ->
				regoleUnita[cifra] = cifra == 0 && number.size()!=1?"":unita[cifra]
			}
		}
		return regoleUnita
	}

	/**
	 * Ottiene le regole per le decine (caso speciale).
	 * Applica l'elisione della vocale finale davanti a uno e otto.
	 * @return Mappa con le regole per determinare i numeri.
	 */
	private static Map getRegoleDecine(){
		Map regoleDecine = [:]
		(CIFRA_MIN..CIFRA_MAX).each { cifra ->
			switch ( cifra ) {
				// Decine da 10 a 19: una sola parola irregolare, senza unita separata.
				case 1: regoleDecine[cifra] = { number, getCardinal ->
					decineSpeciali[number[-1]]
				}
					break
				// 20 a 90: parola della decina, elisa davanti a uno/otto, piu l'unita attaccata.
				default: regoleDecine[cifra] = { number, getCardinal ->
					StringBuilder cardinal = new StringBuilder()
					String decina = decine[cifra]
					if( number[-1] != 0 ) {
						if( cifreElisione.contains(number[-1]) ) {
							decina = decina.substring(0, decina.length() - 1)
						}
						cardinal << decina
						cardinal << getCardinal(number[-1..-1])
					} else {
						cardinal << decina
					}
					return cardinal.toString()
				}
			}
		}
		return regoleDecine
	}

	/**
	 * Ottiene le regole per le centinaia (caso speciale).
	 * "cento" e invariabile e si antepone al resto con uno spazio.
	 * @return Mappa con le regole per calcolare le centinaia.
	 */
	private static Map getRegoleCentinaia() {
		Map regoleCentinaia = [:]
		(CIFRA_MIN..CIFRA_MAX).each { cifra ->
			def centinaio = centinaia[cifra]
			regoleCentinaia[cifra] = { number, getCardinal ->
				def numResto = number[-2..-1]
				numResto == 0?centinaio:"${centinaio} ${getCardinal(numResto)}"
			}
		}
		return regoleCentinaia
	}

	/**
	 * Ottiene le regole per i casi comuni (dalle migliaia in avanti).
	 * @param number Numero di cui si vuole ottenere il cardinale.
	 * @param getCardinal Closure iniettato per richiamare ricorsivamente il calcolo dei cardinali.
	 * @return Stringa con il cardinale.
	 * @see groovy.lang.Closure
	 */
	private static String getCardinaleComune ( Number number, Closure getCardinal ) {
		StringBuilder cardinal = new StringBuilder()

		def posizione = number.size()
		def suffisso = getSuffisso(posizione)
		def arrSuffissi = suffisso.suffisso.split(SEPARATORE_REGOLE)

		def numGruppo = number[-(posizione)..-(suffisso.intervallo.from)]
		def numResto = number[-(suffisso.intervallo.from-1)..-1]

		boolean gruppoUno = numGruppo == 1
		boolean suffissoMigliaia = suffisso.suffisso.contains("mille")

		def gruppoCardinale = getCardinal(numGruppo)

		// Elisione di "uno" -> "un" davanti a milione/miliardo/bilione
		// (un milione, un miliardo): vale solo per i suffissi separati da spazio,
		// non per le migliaia attaccate.
		if( !suffissoMigliaia ) {
			gruppoCardinale = gruppoCardinale.replaceAll(/uno\b/, "un")
		}

		// "1000" si dice "mille" (non "unomille"): si omette "uno" quando il gruppo
		// e esattamente 1 e il suffisso e quello delle migliaia.
		if(!(gruppoUno && suffissoMigliaia)) {
			cardinal << gruppoCardinale
		}

		if(arrSuffissi.size() > 1 && !gruppoUno) {
			cardinal <<  arrSuffissi[1]
		} else {
			cardinal <<  arrSuffissi[0]
		}

		if( numResto != 0 ){
			// Le migliaia si attaccano al resto senza spazio (duemilacinquecento);
			// milioni e miliardi lo separano con uno spazio (un milione cento).
			if( !suffissoMigliaia ){
				cardinal << " "
			}
			cardinal << getCardinal(numResto)
		}

		return cardinal.toString().trim()
	}

	/**
	 * Ottiene le regole per la maggior parte dei numeri.
	 * @return Mappa con le regole per determinare i numeri.
	 */
	private static Map getRegoleComuni() {
		Map regoleTmp = [:]
		(CIFRA_MIN..CIFRA_MAX).each { cifra ->
			regoleTmp[cifra] = { number, getCardinal ->
				getCardinaleComune(number, getCardinal)
			}
		}
		return regoleTmp
	}
}
