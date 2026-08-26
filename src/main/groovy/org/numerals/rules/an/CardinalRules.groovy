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

package org.numerals.rules.an

import org.numerals.Number

/**
 * Clase que contién as reglas ta obtener os cardinals d'un numero en aragonés.
 *
 * A propiedat estatica "rules" torna as reglas ta obtener os cardinals.
 *
 * FUENTS (grammatica verificada):
 * - Biquipedia (l'enciclopedia en aragonés), articlo "Cardinals en aragonés",
 *   que sigue a "Propuesta ortografica de l'Academia de l'Aragonés" (EDACAR 7,
 *   Zaragoza, 2010). D'astí salen as unidaz, os numeros de 10 a 20 y as decenas.
 * - A forma "cient" ta 100 ye atestiguada en aragonés meyebal (Biquizionario,
 *   exemplo "cient florines de oro", sieglo XV, Tergüel).
 *
 * PARTICULARIDAZ DE L'ARAGONES tractadas aquí:
 * - Conector copulativo "y" entre as decenas y as unidaz (trenta y tres), seguntes
 *   a Biquipedia: dende trenta os cardinals s'escriben como sintagmas ligaus por a
 *   conchunción copulativa (trenta y un).
 * - Os numeros de 21 a 29 s'escriben chuntos (ventiun, ventidós, ventitrés...).
 * - Os numeros de 16 a 19 son formas soldadas (deciséis, decisiete, deciueito,
 *   decinueu), sin conector.
 * - "mil" ta 1000 (no "un mil").
 * - Escala larga europeya, como en as luengas circumvecinas: milión = 10^6,
 *   billón = 10^12, trillón = 10^18 (o mesmo esquema de bloques de 6 posicions
 *   que l'espanyol; verificau que ye correcto ta l'aragonés).
 *
 * TRIGA NORMATIVA (an ye una luenga con menos normalización, s'ha trigau una
 * variant coherent y documentada):
 * - unidaz: se gosa la forma "uno" ta lo cardinal suelto; a variant "ueito" ta 8
 *   y "nueu" ta 9 (formas cheneral-comuns d'a Biquipedia).
 * - decenas: sisanta (60), setanta (70), uitanta (80), novanta (90), que son as
 *   formas normativas d'a Biquipedia (l'Academia), no as variants "ueitanta" /
 *   "nobanta" que amaneixen en beluns dialectos.
 * - centenas: "cient" ta 100 y "X cientos" ta las atras (dos cientos, tres
 *   cientos...), con concordancia como adchectivo, seguntes o modelo d'as luengas
 *   romances circumvecinas y a fonetica aragonesa.
 *
 * @author Josue Martinez Buenrrostro (@josuemb)
 */
class CardinalRules {

	private static final SEPARADOR_REGLAS = "/"
	private static final SEPARADOR_DECENAS = " y "
	private static final POSICION_MIN = 1
	private static final POSICION_MAX = 24
	private static final DICHITO_MIN = 0
	private static final DICHITO_MAX = 9
	// Unidaz de 0 a 9. "ueito" (8) y "nueu" (9) son as formas aragonesas.
	private static final Map unidaz = [0:"zero", 1:"uno", 2:"dos",3:"tres",4:"cuatro",5:"cinco",6:"seis",7:"siete",8:"ueito",9:"nueu"]
	// Decenas exautas: diez, vente, trenta... Formas normativas de l'Academia.
	private static final Map decenas = [1:"diez", 2:"vente",3:"trenta",4:"cuaranta",5:"cincuanta",6:"sisanta",7:"setanta",8:"uitanta",9:"novanta"]
	// Prefixo ta os numeros de 21 a 29: "venti-" (ventiun, ventidós...).
	private static final Map decenasEspecials = [2:"venti"]
	// Unidaz con o tono grafico que amaneixen en as formas soldadas venti- (ventidós,
	// ventitrés, ventiséis), seguntes a Biquipedia. As unidaz sueltas van sin tono.
	private static final Map unidazSoldadas = [2:"d\u00F3s",3:"tr\u00E9s",6:"s\u00E9is"]
	// Numeros de 11 a 19: formas irregulars/soldadas en aragonés.
	// 11-15 son formas simples; 16-19 leva o prefixo "deci-".
	private static final Map decenasEspecialsUno = [1:"once",2:"dotze",3:"tretze",4:"catorze",5:"quince",6:"deciséis",7:"decisiete",8:"deciueito",9:"decinueu"]
	// Centenas: "cient" ta 100; ta 200-900 se gosa "X cientos" (adchectivo con concordancia).
	private static final Map centenasEspecials = [1:"cient"]
	private static final List sufixos = [
		[rango:3..3,sufixo:" cientos"],
		[rango:4..6,sufixo:" mil"],
		[rango:7..12,sufixo:" mili\u00F3n$SEPARADOR_REGLAS milions"],
		[rango:13..18,sufixo:" bill\u00F3n$SEPARADOR_REGLAS billons"],
		[rango:19..24,sufixo:" trill\u00F3n$SEPARADOR_REGLAS trillons"]
	]
	static final Map rules = [:]

	// Estableix as reglas ta cada posición.
	static {
		(POSICION_MIN..POSICION_MAX).each{ posicion ->
			switch( posicion ) {
				case 1: rules[posicion] = getReglasUnidaz()
					break
				case 2: rules[posicion] = getReglasDecenas()
					break
				case 3: rules[posicion] = getReglasCentenas()
					break
				default: rules[posicion] = getReglasComun()
			}
		}
	}

	/**
	 * Consulta os sufixos ta cada rango de posicions.
	 * @param posicion Posición.
	 * @return Mapa que contién o rango de cada sufixo y o propio sufixo.
	 */
	private static Map getSufixo(int posicion){
		Map sufixosTmp = sufixos.find{it.rango.isCase(posicion)}
		if(!sufixosTmp) {
			throw new MissingResourceException("Cannot found class suffix for position:$posicion", CardinalRules.class.name, "getSufixo")
		}
		return sufixosTmp
	}

	/**
	 * Obtién as reglas ta las unidaz (caso especial).
	 * @return Mapa con as reglas ta determinar os numeros.
	 */
	private static Map getReglasUnidaz(){
		Map reglasUnidaz = [:]
		(DICHITO_MIN..DICHITO_MAX).each { dichito ->
			reglasUnidaz[dichito] = { number ->
				reglasUnidaz[dichito] = dichito == 0 && number.size()!=1?"":unidaz[dichito]
			}
		}
		return reglasUnidaz
	}

	/**
	 * Obtién o cardinal ta las decenas de 30 a 90 (con conector "y" opcional).
	 * @param number Numero (nomás decenas) d'o que se quiere obtener o cardinal.
	 * @param getCardinal Closure inyectau ta clamar recursivament o calculo d'os cardinals.
	 * @param unidaz Dichito d'as decenas.
	 * @return Cadena con o cardinal.
	 * @see groovy.lang.Closure
	 */
	private static String getCardinalDecenas ( Number number, Closure getCardinal, int unidaz ) {
		StringBuilder cardinal = new StringBuilder()
		cardinal << decenas[unidaz]
		if( number[-1] != 0 ) {
			cardinal << SEPARADOR_DECENAS
			cardinal << getCardinal(number[-1..-1])
		}
		return cardinal.toString()
	}

	/**
	 * Obtién as reglas ta las decenas (caso especial).
	 * @return Mapa con as reglas ta determinar os numeros.
	 */
	private static Map getReglasDecenas(){
		Map reglasDecenas = [:]
		(DICHITO_MIN..DICHITO_MAX).each { dichito ->
			switch ( dichito ) {
				// Numeros de 10 a 19: formas irregulars soldadas, sin conector.
				case 1: reglasDecenas[dichito] = { number, getCardinal ->
					String cardinal
					switch (number[-1]) {
						case 0: cardinal = decenas[dichito]
							break
						default:
							cardinal = decenasEspecialsUno[number[-1]]
					}
					return cardinal
				}
					break
				// Numeros de 20 a 29: prefixo "venti" soldau (ventiun, ventidós...).
				// As unidaz 2/3/6 leva tono grafico en a forma soldada (ventidós, ventitrés, ventiséis).
				case 2: reglasDecenas[dichito] = { number, getCardinal ->
					if( number[-1] == 0 ) {
						return decenas[dichito]
					}
					def unidat = unidazSoldadas.containsKey(number[-1])?unidazSoldadas[number[-1]]:getCardinal(number[-1..-1])
					return "${decenasEspecials[dichito]}${unidat}"
				}
					break
				// Numeros de 30 a 99: decena, conector "y" y unidat.
				default: reglasDecenas[dichito] = { number, getCardinal ->
					getCardinalDecenas(number, getCardinal, dichito)
				}
			}
		}
		return reglasDecenas
	}

	/**
	 * Obtién as reglas ta os casos comuns (dende os millars entabant).
	 * @param number Numero d'o que se quiere obtener o cardinal.
	 * @param getCardinal Closure inyectau ta clamar recursivament o calculo d'os cardinals.
	 * @return Cadena con o cardinal.
	 * @see groovy.lang.Closure
	 */
	private static String getCardinalComun ( Number number, Closure getCardinal ) {
		StringBuilder cardinal = new StringBuilder()

		def posicion = number.size()
		def sufixo = getSufixo(posicion)

		def arrSufixos = sufixo.sufixo.split(SEPARADOR_REGLAS)

		def numGrupo = number[-(posicion)..-(sufixo.rango.from)]
		def numResto = number[-(sufixo.rango.from-1)..-1]

		def grupoCardinal = getCardinal(numGrupo)

		// Apocope de "uno" -> "un" debant d'un sustantivo d'escala (un milión, mil...),
		// y "ventiuno" -> "ventiún" en as formas compuestas soldadas (o tono se manién).
		grupoCardinal = grupoCardinal.replaceAll(/ventiuno/, "venti\u00FAn")
		grupoCardinal = grupoCardinal.replaceAll(/uno\b/, "un")

		// "1 mil" se diz "mil" (no "un mil"): sacar o "uno" quan o grupo ye
		// exautament 1 y o sufixo ye o singular "mil".
		if(!(numGrupo == 1 && sufixo.sufixo.trim() == "mil")) {
			cardinal << grupoCardinal
		}

		if(arrSufixos.size() > 1 && numGrupo != 1) {
			cardinal <<  arrSufixos[1]
		} else {
			cardinal <<  arrSufixos[0]
		}

		if( numResto != 0 ){
			cardinal <<  " "
			cardinal << getCardinal(numResto)
		}

		return cardinal.toString().trim()
	}

	/**
	 * Obtién as reglas ta las centenas (caso especial).
	 * @return Mapa con as reglas ta calcular as centenas.
	 */
	private static Map getReglasCentenas() {
		Map reglasCentenas = [:]
		(DICHITO_MIN..DICHITO_MAX).each { dichito ->
			switch ( dichito ) {
				// 100-199: "cient" (exauto) u "cient" + resto (cient uno, cient vente...).
				case 1:
					def cient = centenasEspecials[dichito]
					reglasCentenas[dichito] = { number, getCardinal ->
						def numResto = number[-2..-1]
						numResto == 0?cient:"${cient} ${getCardinal(numResto)}"
					}
					break
				// 200-999: "X cientos" (dos cientos, tres cientos...) + resto opcional.
				default:
					reglasCentenas[dichito] = { number, getCardinal ->
						getCardinalComun(number, getCardinal)
					}
			}
		}
		return reglasCentenas
	}

	/**
	 * Obtién as reglas ta la mayoría d'os numeros.
	 * @return Mapa con as reglas ta determinar os numeros.
	 */
	private static Map getReglasComun() {
		Map reglasTmp = [:]
		(DICHITO_MIN..DICHITO_MAX).each { dichito ->
			reglasTmp[dichito] = { number, getCardinal ->
				getCardinalComun(number, getCardinal)
			}
		}
		return reglasTmp
	}
}
