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

package org.numerals.rules.oc

import org.numerals.Number

/**
 * Classa que conten las reglas per obténer los cardinals d'un nombre en
 * Occitan (lengadocian, nòrma classica).<br/><br/>
 *
 * La proprietat estatica "rules" retorna las reglas per obténer los cardinals.
 *
 * Particularitats de l'Occitan tractadas aicí (nòrma classica / lengadocian,
 * segon languagesandnumbers.com e la Gramatica occitana d'Alibèrt):
 * - Los nombres de 17 a 29 se forman amb lo connector "-e-" entre la desena e
 *   l'unitat: "dètz-e-sèt" [17], "dètz-e-uèch" [18], "dètz-e-nòu" [19],
 *   "vint-e-un" [21], "vint-e-nòu" [29].
 * - A partir de 30, la desena e l'unitat se ligan amb un espaci simple, sens
 *   connector: "trenta un" [31], "cinquanta sèt" [57], "nonanta nòu" [99].
 * - Las desenas de 10 a 16 son mots especifics (dètz, onze, dotze, tretze,
 *   catòrze, quinze, setze).
 * - Cada centena se forma amb l'unitat davant lo mot "cent"/"cents", franc de
 *   100 exact: "cent" [100], "dos cents" [200], "tres cents" [300]...
 * - 1000 es "mila" (pas "un mila"); los multiples fan "dos mila", "tres mila"...
 * - L'escala occitana alterna las formas en -ion e en -iard cada 3 chifras,
 *   coma en italian: "milion" (10^6), "miliard" (10^9), "bilion" (10^12),
 *   "biliard" (10^15)... Sols "milion" (10^6) e "miliard" (10^9) son atestats
 *   dirèctament dins la referéncia; las posicions superioras seguisson la
 *   meteissa alternança -ion/-iard de la nòrma classica.
 * - Davant un mot d'escala, l'unitat "un" se manten "un" ("un milion"); es la
 *   forma per defaut de l'unitat 1 dins aqueste dialècte.
 *
 * @author Josue Martinez Buenrrostro (@josuemb)
 */
class CardinalRules {

	private static final SEPARADOR_REGLAS = "/"
	private static final SEPARADOR_TEENS = "-e-"
	private static final SEPARADOR_DESENAS = " "
	// Lo limit de las desenas amb connector "-e-": de 17 a 29 (chifra de desena
	// 1 amb unitat, e chifra de desena 2). A partir de 30 s'emplega un espaci.
	private static final DESENA_AMB_CONNECTOR_E = 2
	private static final POSICION_MIN = 1
	private static final POSICION_MAX = 24
	private static final CHIFRA_MIN = 0
	private static final CHIFRA_MAX = 9
	private static final Map unitats = [0:"zèro", 1:"un", 2:"dos",3:"tres",4:"quatre",5:"cinc",6:"sièis",7:"sèt",8:"uèch",9:"nòu"]
	private static final Map desenas = [1:"dètz", 2:"vint",3:"trenta",4:"quaranta",5:"cinquanta",6:"seissanta",7:"setanta",8:"ochanta",9:"nonanta"]
	// Desenas de 10 a 16: mots especifics en Occitan. De 17 a 19 se compausan
	// (dètz-e-sèt...), doncas aicí sols se definisson 10 a 16.
	private static final Map desenasEspecialas = [0:"dètz",1:"onze",2:"dotze",3:"tretze",4:"catòrze",5:"quinze",6:"setze"]
	// Cada centena: l'unitat davant "cent" (singular per 100) o "cents" (plural).
	private static final Map centenas = [1:"cent",2:"dos cents",3:"tres cents",4:"quatre cents",5:"cinc cents",6:"sièis cents",7:"sèt cents",8:"uèch cents",9:"nòu cents"]
	// Escala occitana: coma en italian, un mot d'escala cada 3 chifras en delà
	// dels milièrs, en alternant las formas en -ion (milion, bilion...) e en
	// -iard (miliard, biliard...). Cada interval cobrís doncas 3 posicions:
	//   10^3  mila           10^6  milion      10^9  miliard
	//   10^12 bilion         10^15 biliard     10^18 trilion   10^21 triliard
	private static final List sufixes = [
		[interval:4..6,sufix:" mila"],
		[interval:7..9,sufix:" milion$SEPARADOR_REGLAS milions"],
		[interval:10..12,sufix:" miliard$SEPARADOR_REGLAS miliards"],
		[interval:13..15,sufix:" bilion$SEPARADOR_REGLAS bilions"],
		[interval:16..18,sufix:" biliard$SEPARADOR_REGLAS biliards"],
		[interval:19..21,sufix:" trilion$SEPARADOR_REGLAS trilions"],
		[interval:22..24,sufix:" triliard$SEPARADOR_REGLAS triliards"]
	]
	static final Map rules = [:]

	// Establís las reglas per cada posicion.
	static {
		(POSICION_MIN..POSICION_MAX).each{ posicion ->
			switch( posicion ) {
				case 1: rules[posicion] = getReglasUnitats()
					break
				case 2: rules[posicion] = getReglasDesenas()
					break
				case 3: rules[posicion] = getReglasCentenas()
					break
				default: rules[posicion] = getReglasComun()
			}
		}
	}

	/**
	 * Consulta los sufixes per cada interval de posicions.
	 * @param posicion Posicion.
	 * @return Mapa amb l'interval de cada sufix e lo sufix meteis.
	 */
	private static Map getSufix(int posicion){
		Map sufixesTmp = sufixes.find{it.interval.isCase(posicion)}
		if(!sufixesTmp) {
			throw new MissingResourceException("Cannot found class suffix for position:$posicion", CardinalRules.class.name, "getSufix")
		}
		return sufixesTmp
	}

	/**
	 * Obten las reglas per las unitats (cas especial).
	 * @return Mapa amb las reglas per determinar los nombres.
	 */
	private static Map getReglasUnitats(){
		Map reglasUnitats = [:]
		(CHIFRA_MIN..CHIFRA_MAX).each { chifra ->
			reglasUnitats[chifra] = { number ->
				reglasUnitats[chifra] = chifra == 0 && number.size()!=1?"":unitats[chifra]
			}
		}
		return reglasUnitats
	}

	/**
	 * Obten las reglas per las desenas (cas especial).
	 * De 10 a 16: mots especifics. De 17 a 29: connector "-e-" entre desena e
	 * unitat. De 30 a 90: espaci simple entre desena e unitat.
	 * @return Mapa amb las reglas per determinar los nombres.
	 */
	private static Map getReglasDesenas(){
		Map reglasDesenas = [:]
		(CHIFRA_MIN..CHIFRA_MAX).each { chifra ->
			switch ( chifra ) {
				// Desena 1 (10..19): 10 a 16 son mots especifics; 17 a 19 se
				// compausan amb "dètz" + "-e-" + unitat (dètz-e-sèt...).
				case 1: reglasDesenas[chifra] = { number, getCardinal ->
					def unitat = number[-1]
					if( desenasEspecialas.containsKey(unitat) ) {
						return desenasEspecialas[unitat]
					}
					StringBuilder cardinal = new StringBuilder()
					cardinal << desenas[chifra]
					cardinal << SEPARADOR_TEENS
					cardinal << getCardinal(number[-1..-1])
					return cardinal.toString()
				}
					break
				// Desenas 20 a 90: mot de la desena, opcionalament + unitat.
				// La vintena (20..29) emplega lo connector "-e-"; de 30 a 90
				// s'emplega un espaci simple.
				default: reglasDesenas[chifra] = { number, getCardinal ->
					StringBuilder cardinal = new StringBuilder()
					cardinal << desenas[chifra]
					if( number[-1] != 0 ) {
						cardinal << (chifra == DESENA_AMB_CONNECTOR_E ? SEPARADOR_TEENS : SEPARADOR_DESENAS)
						cardinal << getCardinal(number[-1..-1])
					}
					return cardinal.toString()
				}
			}
		}
		return reglasDesenas
	}

	/**
	 * Obten las reglas per las centenas (cas especial).
	 * L'unitat se met davant "cent"/"cents", seguida del rèst amb un espaci.
	 * @return Mapa amb las reglas per calcular las centenas.
	 */
	private static Map getReglasCentenas() {
		Map reglasCentenas = [:]
		(CHIFRA_MIN..CHIFRA_MAX).each { chifra ->
			def centena = centenas[chifra]
			reglasCentenas[chifra] = { number, getCardinal ->
				def numRèst = number[-2..-1]
				numRèst == 0?centena:"${centena} ${getCardinal(numRèst)}"
			}
		}
		return reglasCentenas
	}

	/**
	 * Obten las reglas pels cases comuns (dels milièrs en avant).
	 * @param number Nombre del qual se vòl obténer lo cardinal.
	 * @param getCardinal Closure injectat per sonar recursivament lo calcul dels cardinals.
	 * @return Cadena amb lo cardinal.
	 * @see groovy.lang.Closure
	 */
	private static String getCardinalComun ( Number number, Closure getCardinal ) {
		StringBuilder cardinal = new StringBuilder()

		def posicion = number.size()
		def sufix = getSufix(posicion)
		def arrSufixes = sufix.sufix.split(SEPARADOR_REGLAS)

		def numGrop = number[-(posicion)..-(sufix.interval.from)]
		def numRèst = number[-(sufix.interval.from-1)..-1]

		boolean gropUn = numGrop == 1
		boolean sufixMila = sufix.sufix.trim() == "mila"

		def gropCardinal = getCardinal(numGrop)

		// "1000" se ditz "mila" (pas "un mila"): s'omet lo grop quand val
		// exactament 1 e lo sufix es lo dels milièrs.
		if(!(gropUn && sufixMila)) {
			cardinal << gropCardinal
		}

		if(arrSufixes.size() > 1 && !gropUn) {
			cardinal <<  arrSufixes[1]
		} else {
			cardinal <<  arrSufixes[0]
		}

		if( numRèst != 0 ){
			cardinal <<  " "
			cardinal << getCardinal(numRèst)
		}

		return cardinal.toString().trim()
	}

	/**
	 * Obten las reglas per la màger part dels nombres.
	 * @return Mapa amb las reglas per determinar los nombres.
	 */
	private static Map getReglasComun() {
		Map reglasTmp = [:]
		(CHIFRA_MIN..CHIFRA_MAX).each { chifra ->
			reglasTmp[chifra] = { number, getCardinal ->
				getCardinalComun(number, getCardinal)
			}
		}
		return reglasTmp
	}
}
