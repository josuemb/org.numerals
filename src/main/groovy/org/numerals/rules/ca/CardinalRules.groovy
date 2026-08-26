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

package org.numerals.rules.ca

import org.numerals.Number

/**
 * Classe que conte les regles per obtenir els cardinals d'un numero en
 * Catala.<br/><br/>
 *
 * La propietat estatica "rules" retorna les regles per obtenir els cardinals.
 *
 * Particularitats del Catala tractades aqui:
 * - Les desenes i les unitats s'uneixen amb un guio: "trenta-cinc" [35].
 *   L'excepcio son les vintenes, on es mante la conjuncio "i": "vint-i-cinc" [25].
 * - La unitat 1 es "u" darrere de "vint-i-" ("vint-i-u" [21]), pero "un"
 *   darrere de les altres desenes ("trenta-un" [31]) i davant d'una escala
 *   ("un milio").
 * - Les desenes de 10 a 19 son paraules irregulars (deu, onze, ... dinou).
 * - Cada centena te la seva propia paraula formada amb guio: "cent" [100],
 *   "dos-cents" [200], "tres-cents" [300]... El 100 exacte es "cent" (no "un cent").
 * - 1000 es "mil" (no "un mil").
 * - El Catala fa servir l'escala llarga: "un milio" (10^6), "mil milions" (10^9),
 *   "un bilio" (10^12), "un trilio" (10^18). "mil milions" surt de la recursio
 *   (el grup "mil" davant del sufix " milions").
 *
 * @author Josue Martinez Buenrrostro (@josuemb)
 */
class CardinalRules {

	private static final SEPARADOR_REGLES = "/"
	private static final SEPARADOR_DESENES = "-"
	private static final SEPARADOR_VINTENES = "-i-"
	private static final POSICIO_MIN = 1
	private static final POSICIO_MAX = 24
	private static final DIGIT_MIN = 0
	private static final DIGIT_MAX = 9
	// La unitat 1 te dues formes. Com a numeral pur (sola o al final d'un numero)
	// es "u": "u" [1], "cent u" [101], "vint-i-u" [21]. Pren la forma "un" nomes
	// en dos contextos: darrere de les desenes 30..90 ("trenta-un" [31]) i davant
	// d'una paraula d'escala ("un milio", "un bilio").
	private static final Map unitats = [0:"zero", 1:"u", 2:"dos",3:"tres",4:"quatre",5:"cinc",6:"sis",7:"set",8:"vuit",9:"nou"]
	private static final UNITAT_UN = "un"
	private static final Map desenes = [1:"deu", 2:"vint",3:"trenta",4:"quaranta",5:"cinquanta",6:"seixanta",7:"setanta",8:"vuitanta",9:"noranta"]
	// Desenes de 10 a 19: paraules irregulars en Catala.
	private static final Map desenesEspecials = [0:"deu",1:"onze",2:"dotze",3:"tretze",4:"catorze",5:"quinze",6:"setze",7:"disset",8:"divuit",9:"dinou"]
	// Cada centena te la seva paraula. L'1 es especial: cent (exacte) / cent (amb resta).
	// La resta es formen amb "<unitat>-cents" (dos-cents, tres-cents...).
	private static final Map centenes = [1:"cent",2:"dos-cents",3:"tres-cents",4:"quatre-cents",5:"cinc-cents",6:"sis-cents",7:"set-cents",8:"vuit-cents",9:"nou-cents"]
	private static final List sufixos = [
		[interval:4..6,sufix:" mil"],
		[interval:7..12,sufix:" mili\u00F3$SEPARADOR_REGLES milions"],
		[interval:13..18,sufix:" bili\u00F3$SEPARADOR_REGLES bilions"],
		[interval:19..24,sufix:" trili\u00F3$SEPARADOR_REGLES trilions"]
	]
	static final Map rules = [:]

	// Estableix les regles per a cada posicio.
	static {
		(POSICIO_MIN..POSICIO_MAX).each{ posicio ->
			switch( posicio ) {
				case 1: rules[posicio] = getReglesUnitats()
					break
				case 2: rules[posicio] = getReglesDesenes()
					break
				case 3: rules[posicio] = getReglesCentenes()
					break
				default: rules[posicio] = getReglesComu()
			}
		}
	}

	/**
	 * Consulta els sufixos per a cada interval de posicions.
	 * @param posicio Posicio.
	 * @return Mapa amb l'interval de cada sufix i el propi sufix.
	 */
	private static Map getSufix(int posicio){
		Map sufixosTmp = sufixos.find{it.interval.isCase(posicio)}
		if(!sufixosTmp) {
			throw new MissingResourceException("Cannot found class suffix for position:$posicio", CardinalRules.class.name, "getSufix")
		}
		return sufixosTmp
	}

	/**
	 * Obte les regles per a les unitats (cas especial).
	 * @return Mapa amb les regles per determinar els numeros.
	 */
	private static Map getReglesUnitats(){
		Map reglesUnitats = [:]
		(DIGIT_MIN..DIGIT_MAX).each { digit ->
			reglesUnitats[digit] = { number ->
				reglesUnitats[digit] = digit == 0 && number.size()!=1?"":unitats[digit]
			}
		}
		return reglesUnitats
	}

	/**
	 * Obte les regles per a les desenes (cas especial).
	 * @return Mapa amb les regles per determinar els numeros.
	 */
	private static Map getReglesDesenes(){
		Map reglesDesenes = [:]
		(DIGIT_MIN..DIGIT_MAX).each { digit ->
			switch ( digit ) {
				// Desenes de 10 a 19: una unica paraula irregular, sense unitat separada.
				case 1: reglesDesenes[digit] = { number, getCardinal ->
					desenesEspecials[number[-1]]
				}
					break
				// Vintenes: "vint", opcionalment "-i-" + unitat ("vint-i-u", "vint-i-tres").
				// La unitat 1 pren la forma per defecte "u".
				case 2: reglesDesenes[digit] = { number, getCardinal ->
					StringBuilder cardinal = new StringBuilder()
					cardinal << desenes[digit]
					if( number[-1] != 0 ) {
						cardinal << SEPARADOR_VINTENES
						cardinal << getCardinal(number[-1..-1])
					}
					return cardinal.toString()
				}
					break
				// 30 a 90: paraula, opcionalment "-" + unitat ("trenta-un", "trenta-dos").
				// La unitat 1 pren la forma "un" darrere d'aquestes desenes.
				default: reglesDesenes[digit] = { number, getCardinal ->
					StringBuilder cardinal = new StringBuilder()
					cardinal << desenes[digit]
					if( number[-1] != 0 ) {
						cardinal << SEPARADOR_DESENES
						cardinal << (number[-1] == 1 ? UNITAT_UN : getCardinal(number[-1..-1]))
					}
					return cardinal.toString()
				}
			}
		}
		return reglesDesenes
	}

	/**
	 * Obte les regles per a les centenes (cas especial).
	 * @return Mapa amb les regles per calcular les centenes.
	 */
	private static Map getReglesCentenes() {
		Map reglesCentenes = [:]
		(DIGIT_MIN..DIGIT_MAX).each { digit ->
			def centena = centenes[digit]
			reglesCentenes[digit] = { number, getCardinal ->
				def numResta = number[-2..-1]
				numResta == 0?centena:"${centena} ${getCardinal(numResta)}"
			}
		}
		return reglesCentenes
	}

	/**
	 * Obte les regles per als casos comuns (de milers en endavant).
	 * @param number Numero del qual es vol obtenir el cardinal.
	 * @param getCardinal Closure injectat per cridar recursivament el calcul dels cardinals.
	 * @return Cadena amb el cardinal.
	 * @see groovy.lang.Closure
	 */
	private static String getCardinalComu ( Number number, Closure getCardinal ) {
		StringBuilder cardinal = new StringBuilder()

		def posicio = number.size()
		def sufix = getSufix(posicio)
		def arrSufixos = sufix.sufix.split(SEPARADOR_REGLES)

		def numGrup = number[-(posicio)..-(sufix.interval.from)]
		def numResta = number[-(sufix.interval.from-1)..-1]

		def grupCardinal = getCardinal(numGrup)

		// Apocope davant d'una paraula d'escala (milio, bilio, trilio): la unitat
		// final "u" del grup pren la forma "un". Aixi "u" -> "un milio", i el
		// compost "vint-i-u" -> "vint-i-un milions". No s'aplica davant de "mil".
		if(sufix.sufix.trim() != "mil") {
			grupCardinal = grupCardinal.replaceAll(/\bu\b/, UNITAT_UN)
		}

		// "1 mil" es diu "mil" (no "un mil"): ometre el grup quan es exactament 1
		// i el sufix es el singular "mil".
		if(!(numGrup == 1 && sufix.sufix.trim() == "mil")) {
			cardinal << grupCardinal
		}

		if(arrSufixos.size() > 1 && numGrup != 1) {
			cardinal <<  arrSufixos[1]
		} else {
			cardinal <<  arrSufixos[0]
		}

		if( numResta != 0 ){
			cardinal <<  " "
			cardinal << getCardinal(numResta)
		}

		return cardinal.toString().trim()
	}

	/**
	 * Obte les regles per a la majoria dels numeros.
	 * @return Mapa amb les regles per determinar els numeros.
	 */
	private static Map getReglesComu() {
		Map reglesTmp = [:]
		(DIGIT_MIN..DIGIT_MAX).each { digit ->
			reglesTmp[digit] = { number, getCardinal ->
				getCardinalComu(number, getCardinal)
			}
		}
		return reglesTmp
	}
}
