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

package org.numerals.rules.en

import java.util.List;

import org.numerals.Number

/**
 * Class containing rules to get cardinals of a given number
 * in English languag.<br/><br/>
 *
 * The static property "rules" get the rules to get cardinals.
 *  
 * @author Josue Mart&iacute;nez Buenrrostro (@josuemb)
 */
class CardinalRules {

	private static final SEPARATOR_RULES = "/"
	private static final SEPARATOR_COMMON = " "
	private static final SEPARATOR_TENS = "-"
	private static final SEPARATOR_HUNDRED = " "
	private static final POSITION_MIN = 1
	private static final POSITION_MAX = 15
	private static final DIGIT_MIN = 0
	private static final DIGIT_MAX = 9

	private static final Map units = [0:"zero", 1:"one", 2:"two",3:"three",4:"four",5:"five",6:"six",7:"seven",8:"eight",9:"nine"]
	private static final Map tens = [1:"ten", 2:"twenty",3:"thirty",4:"forty",5:"fifty",6:"sixty",7:"seventy",8:"eighty",9:"ninety"]
	private static final Map specialTens = [1:"teen"]
	private static final Map specialTensOne = [1:"eleven",2:"twelve",3:"thirteen",5:"fifteen",8:"eighteen"]

	static final Map rules = [:]

	// Establece las reglas para cada posicion.
	static {
		(POSITION_MIN..POSITION_MAX).each{ position ->
			switch( position ) {
				case 1: rules[position] = getRulesUnits()
					break
				case 2: rules[position] = getRulesTens()
					break
				default: rules[position] = getRulesCommon()
			}
		}
	}

	// Establece las reglas para cada posicion.
	private static final List suffixes = [
		[range:3..3,suffix:"hundred"],
		[range:4..6,suffix:"thousand"],
		[range:7..9,suffix:"million"],
		[range:10..12,suffix:"billion"],
		[range:13..15,suffix:"trillion"]
	]


	/**
	 * Consulta los sufijos para cada range de posiciones.
	 * @param position Posici&oacute;n.
	 * @return Mapa que contiene el range de cada sufijo y el propio sufijo.
	 */
	private static Map getSuffix(int position){
		Map suffixesTmp = suffixes.find{it.range.isCase(position)}
		if(!suffixesTmp) {
			throw new MissingResourceException("Cannot found class suffix for position:$position", CardinalRules.class.name, "getSuffix")
		}
		return suffixesTmp
	}

	/**
	 * Obtiene las reglas para establecer los cardinales para las unidades (caso especial).
	 * @return Mapa con las reglas para determinar los n&uacute;meros.
	 */
	private static Map getRulesUnits(){
		Map rulesUnits = [:]
		(DIGIT_MIN..DIGIT_MAX).each { digit ->
			rulesUnits[digit] = { number ->
				digit == 0 && number.size()!=1?"":units[digit]
			}
		}
		return rulesUnits
	}

	/**
	 * Obtiene el cardinal para las decenas.
	 * @param number Numero (solo decenas) del que se desea obtener el cardinal.
	 * @param getCardinal Closure que se inyecta automáticamente para llamar recursivamente al Closure que determina los cardinales.
	 * @param unidades Unidades.
	 * @return Cadena con el Cardinal.
	 * @see groovy.lang.Closure
	 */
	private static String getCardinalTens ( Number number, Closure getCardinal, int units ) {
		StringBuilder cardinal = new StringBuilder()
		cardinal << tens[units]
		if( number[-1] != 0 ) {
			cardinal << SEPARATOR_TENS
			cardinal << getCardinal(number[-1..-1])
		}
		return cardinal.toString()
	}

	/**
	 * Obtiene las reglas para establecer los cardinales para las unidades (caso especial).
	 * @return Mapa con las reglas para determinar los n&uacute;meros.
	 */
	private static Map getRulesTens(){
		Map rulesTens = [:]
		(DIGIT_MIN..DIGIT_MAX).each { digit ->
			switch ( digit ) {
				case 1: rulesTens[digit] = { number, getCardinal ->
					String cardinal
					// Verifica el valor de las unidades
					switch (number[-1]) {
						case 0: cardinal = tens[digit]
							break
						case [1, 2, 3, 5, 8]: cardinal = specialTensOne[number[-1]]
							break
						default:
							cardinal = "${getCardinal(number[-1..-1])}${specialTens[digit]}"
					}
					return cardinal
				}
					break
				default: rulesTens[digit] = { number, getCardinal ->
					getCardinalTens(number, getCardinal, digit)
				}
			}
		}
		return rulesTens
	}

	/**
	 * Obtiene las reglas para establecer los cardinales para el los casos comunes (de millares en adelante).
	 * @param number Numero del que se desea obtener el cardinal.
	 * @param getCardinal Closure que se inyecta automáticamente para llamar recursivamente al Closure que determina los cardinales.
	 * @return Cadena con el Cardinal.
	 * @see groovy.lang.Closure
	 */
	private static String getCardinalCommon ( Number number, Closure getCardinal ) {
		StringBuilder cardinal = new StringBuilder()

		def pos = number.size()
		def suffix = getSuffix(pos)

		def numGrupo = number[-(pos)..-(suffix.range.from)]
		def numResto = number[-(suffix.range.from-1)..-1]

		cardinal << getCardinal(numGrupo)
		cardinal << SEPARATOR_COMMON
		cardinal << suffix.suffix

		if( numResto != 0 ){
			cardinal <<  SEPARATOR_COMMON
			cardinal << getCardinal(numResto)
		}

		return cardinal.toString()
	}

	/**
	 * Obtiene las reglas para establecer los cardinales de la mayor&iacute;a de los n&uacute;meros.
	 * @return Mapa con las reglas para determinar los n&uacute;meros.
	 */
	private static Map getRulesCommon() {
		Map rulesTmp = [:]
		(DIGIT_MIN..DIGIT_MAX).each { digit ->
			switch ( digit ) {
				default:
					rulesTmp[digit] = { number, getCardinal ->
						getCardinalCommon(number, getCardinal)
					}
			}
		}
		return rulesTmp
	}
}
