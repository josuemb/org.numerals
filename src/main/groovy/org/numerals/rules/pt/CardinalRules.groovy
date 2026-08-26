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

package org.numerals.rules.pt

import org.numerals.Number

/**
 * Class holding the rules to obtain the cardinals of a number in Portuguese.
 *
 * The static property "rules" returns the rules to obtain the cardinals.
 *
 * Portuguese specifics handled here:
 * - The connector "e" joins tens and units (vinte e tres), hundreds and the
 *   rest (cento e vinte), and a group and its remainder (mil e um).
 * - "cem" for exactly 100, "cento" when followed by more (cento e um).
 * - Every hundred has its own word (duzentos, trezentos, ...), unlike Spanish
 *   which composes most of them.
 * - 1000 is "mil" (not "um mil"), same rule as Spanish.
 *
 * @author Josue Martinez Buenrrostro (@josuemb)
 */
class CardinalRules {

	private static final SEPARADOR_REGLAS = "/"
	private static final SEPARADOR_DECENAS = " e "
	private static final POSICION_MIN = 1
	private static final POSICION_MAX = 24
	private static final DIGITO_MIN = 0
	private static final DIGITO_MAX = 9
	private static final Map unidades = [0:"zero", 1:"um", 2:"dois",3:"tres",4:"quatro",5:"cinco",6:"seis",7:"sete",8:"oito",9:"nove"]
	private static final Map decenas = [1:"dez", 2:"vinte",3:"trinta",4:"quarenta",5:"cinquenta",6:"sessenta",7:"setenta",8:"oitenta",9:"noventa"]
	// Teens (10-19) are irregular in Portuguese: dez + e + unit contracted forms.
	private static final Map dezenasEspeciais = [0:"dez",1:"onze",2:"doze",3:"treze",4:"catorze",5:"quinze",6:"dezesseis",7:"dezessete",8:"dezoito",9:"dezenove"]
	// Every hundred has its own word. 1 is special: cem (exact) / cento (with rest).
	private static final Map centenas = [1:"cem${SEPARADOR_REGLAS}cento",2:"duzentos",3:"trezentos",4:"quatrocentos",5:"quinhentos",6:"seiscentos",7:"setecentos",8:"oitocentos",9:"novecentos"]
	private static final List sufijos = [
		[rango:4..6,sufijo:" mil"],
		[rango:7..12,sufijo:" milh\u00E3o$SEPARADOR_REGLAS milh\u00F5es"],
		[rango:13..18,sufijo:" bilh\u00E3o$SEPARADOR_REGLAS bilh\u00F5es"],
		[rango:19..24,sufijo:" trilh\u00E3o$SEPARADOR_REGLAS trilh\u00F5es"]
	]
	static final Map rules = [:]

	static {
		(POSICION_MIN..POSICION_MAX).each{ position ->
			switch( position ) {
				case 1: rules[position] = getRulesUnidades()
					break
				case 2: rules[position] = getRulesDecenas()
					break
				case 3: rules[position] = getRulesCentenas()
					break
				default: rules[position] = getRulesCommon()
			}
		}
	}

	private static Map getSufijo(int posicion){
		Map sufijosTmp = sufijos.find{it.rango.isCase(posicion)}
		if(!sufijosTmp) {
			throw new MissingResourceException("Cannot found class suffix for position:$posicion", CardinalRules.class.name, "getSufijo")
		}
		return sufijosTmp
	}

	private static Map getRulesUnidades(){
		Map rulesUnidades = [:]
		(DIGITO_MIN..DIGITO_MAX).each { digit ->
			rulesUnidades[digit] = { number ->
				rulesUnidades[digit] = digit == 0 && number.size()!=1?"":unidades[digit]
			}
		}
		return rulesUnidades
	}

	private static Map getRulesDecenas(){
		Map rulesDecenas = [:]
		(DIGITO_MIN..DIGITO_MAX).each { digit ->
			switch ( digit ) {
				// Teens (10-19): a single irregular word, no "e" connector.
				case 1: rulesDecenas[digit] = { number, getCardinal ->
					dezenasEspeciais[number[-1]]
				}
					break
				// 20..90: word, optionally "e" + unit.
				default: rulesDecenas[digit] = { number, getCardinal ->
					StringBuilder cardinal = new StringBuilder()
					cardinal << decenas[digit]
					if( number[-1] != 0 ) {
						cardinal << SEPARADOR_DECENAS
						cardinal << getCardinal(number[-1..-1])
					}
					return cardinal.toString()
				}
			}
		}
		return rulesDecenas
	}

	private static Map getRulesCentenas() {
		Map rulesCentenas = [:]
		(DIGITO_MIN..DIGITO_MAX).each { digit ->
			switch ( digit ) {
				case 1:
					def cem = centenas[digit].split(SEPARADOR_REGLAS)
					rulesCentenas[digit] = { number, getCardinal ->
						def numResto = number[-2..-1]
						numResto == 0?cem[0]:"${cem[1]}${SEPARADOR_DECENAS}${getCardinal(numResto)}"
					}
					break
				default:
					def centena = centenas[digit]
					rulesCentenas[digit] = { number, getCardinal ->
						def numResto = number[-2..-1]
						numResto == 0?centena:"${centena}${SEPARADOR_DECENAS}${getCardinal(numResto)}"
					}
			}
		}
		return rulesCentenas
	}

	private static String getCardinalCommon ( Number number, Closure getCardinal ) {
		StringBuilder cardinal = new StringBuilder()

		def pos = number.size()
		def sufijo = getSufijo(pos)
		def arrSufijos = sufijo.sufijo.split(SEPARADOR_REGLAS)

		def numGrupo = number[-(pos)..-(sufijo.rango.from)]
		def numResto = number[-(sufijo.rango.from-1)..-1]

		def grupoCardinal = getCardinal(numGrupo)

		// "1 mil" is "mil" (not "um mil"): omit "um" when the group is exactly 1
		// and the suffix is the singular "mil".
		if(!(numGrupo == 1 && sufijo.sufijo.trim() == "mil")) {
			cardinal << grupoCardinal
		}

		if(arrSufijos.size() > 1 && numGrupo != 1) {
			cardinal <<  arrSufijos[1]
		} else {
			cardinal <<  arrSufijos[0]
		}

		if( numResto != 0 ){
			// Portuguese joins the last group with "e" only when that remainder is
			// below 100, or is an exact multiple of 100 (cem/duzentos...). Otherwise
			// (e.g. 234, 567) a plain space is used: "um milhao duzentos e trinta...".
			def restoStr = numResto.toString()
			def below100 = restoStr.length() <= 2
			def exactHundredMultiple = restoStr.length() >= 3 && restoStr.substring(1).every { it == '0' }
			cardinal << (below100 || exactHundredMultiple ? SEPARADOR_DECENAS : " ")
			cardinal << getCardinal(numResto)
		}

		return cardinal.toString().trim()
	}

	private static Map getRulesCommon() {
		Map rulesTmp = [:]
		(DIGITO_MIN..DIGITO_MAX).each { digit ->
			rulesTmp[digit] = { number, getCardinal ->
				getCardinalCommon(number, getCardinal)
			}
		}
		return rulesTmp
	}
}
