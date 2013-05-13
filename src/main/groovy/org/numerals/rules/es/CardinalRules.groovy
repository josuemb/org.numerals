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

package org.numerals.rules.es

import org.numerals.Number
import org.numerals.rules.es.CardinalRules;

/**
 * Clase que contiene las reglas para obtener los cardinales de un n&uacute;mero en
 * lenguaje Espa&ntilde;ol.<br/><br/>
 *
 * La propiedad est&aacute;tica "rules" devuelve las reglas para obtener
 * los cardinales.
 *  
 * @author Josue Mart&iacute;nez Buenrrostro (@josuemb)
 */
class CardinalRules {

	private static final SEPARADOR_REGLAS = "/"
	private static final SEPARADOR_DECENAS = " y "
	private static final POSICION_MIN = 1
	private static final POSICION_MAX = 24
	private static final DIGITO_MIN = 0
	private static final DIGITO_MAX = 9
	private static final Map unidades = [0:"cero", 1:"uno", 2:"dos",3:"tres",4:"cuatro",5:"cinco",6:"seis",7:"siete",8:"ocho",9:"nueve"]
	private static final Map decenas = [1:"diez", 2:"veinte",3:"treinta",4:"cuarenta",5:"cincuenta",6:"sesenta",7:"setenta",8:"ochenta",9:"noventa"]
	private static final Map decenasEspeciales = [1:"dieci",2:"veinti"]
	private static final Map decenasEspecialesUno = [1:"once",2:"doce",3:"trece",4:"catorce",5:"quince"]
	private static final Map centenasEspeciales = [1:"cien${SEPARADOR_REGLAS}ciento",5:"quinientos",7:"setecientos",9:"novecientos"]
	private static final List sufijos = [
		[rango:3..3,sufijo:"cientos"],
		[rango:4..6,sufijo:" mil"],
		[rango:7..12,sufijo:" mill\u00F3n$SEPARADOR_REGLAS millones"],
		[rango:13..18,sufijo:" bill\u00F3n$SEPARADOR_REGLAS billones"],
		[rango:19..24,sufijo:" trill\u00F3n$SEPARADOR_REGLAS trillones"]
	]
	static final Map rules = [:]

	// Establece las reglas para cada posicion.
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

	/**
	 * Consulta los sufijos para cada rango de posiciones.
	 * @param posicion Posici&oacute;n.
	 * @return Mapa que contiene el rango de cada sufijo y el propio sufijo.
	 */
	private static Map getSufijo(int posicion){
		Map sufijosTmp = sufijos.find{it.rango.isCase(posicion)}
		if(!sufijosTmp) {
			throw new MissingResourceException("Cannot found class suffix for position:$posicion", CardinalRules.class.name, "getSufijo")
		}
		return sufijosTmp
	}

	/**
	 * Obtiene las reglas para establecer los cardinales para las unidades (caso especial).
	 * @return Mapa con las reglas para determinar los n&uacute;meros.
	 */
	private static Map getRulesUnidades(){
		Map rulesUnidades = [:]
		(DIGITO_MIN..DIGITO_MAX).each { digit ->
			rulesUnidades[digit] = { number ->
				rulesUnidades[digit] = digit == 0 && number.size()==1?"":unidades[digit]
			}
		}
		return rulesUnidades
	}

	/**
	 * Obtiene el cardinal para las decenas.
	 * @param number Numero (solo decenas) del que se desea obtener el cardinal.
	 * @param getCardinal Closure que se inyecta automáticamente para llamar recursivamente al Closure que determina los cardinales.
	 * @param unidades Unidades.
	 * @return Cadena con el Cardinal.
	 * @see groovy.lang.Closure
	 */
	private static String getCardinalDecenas ( Number number, Closure getCardinal, int unidades ) {
		StringBuilder cardinal = new StringBuilder()
		cardinal << decenas[unidades]
		if( number[-1] != 0 ) {
			cardinal << SEPARADOR_DECENAS
			cardinal << getCardinal(number[-1..-1])
		}
		return cardinal.toString()
	}

	/**
	 * Obtiene las reglas para establecer los cardinales para las unidades (caso especial).
	 * @return Mapa con las reglas para determinar los n&uacute;meros.
	 */
	private static Map getRulesDecenas(){
		Map rulesDecenas = [:]
		(DIGITO_MIN..DIGITO_MAX).each { digit ->
			switch ( digit ) {
				case 1: rulesDecenas[digit] = { number, getCardinal ->
					String cardinal
					switch (number[-1]) {
						case 0: cardinal = decenas[digit]
							break
						case 1..5: cardinal = decenasEspecialesUno[number[-1]]
							break
						default:
							cardinal = "${decenasEspeciales[digit]}${getCardinal(number[-1..-1])}"
					}
					return cardinal
				}
					break
				case 2: rulesDecenas[digit] = { number, getCardinal ->
					number[-1] == 0?decenas[digit]:"${decenasEspeciales[digit]}${getCardinal(number[-1..-1])}"
				}
					break
				default: rulesDecenas[digit] = { number, getCardinal ->
					getCardinalDecenas(number, getCardinal, digit)
				}
			}
		}
		return rulesDecenas
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
		def sufijo = getSufijo(pos)

		def arrSufijos = sufijo.sufijo.split(SEPARADOR_REGLAS)

		def numGrupo = number[-(pos)..-(sufijo.rango.from)]
		def numResto = number[-(sufijo.rango.from-1)..-1]

		cardinal << getCardinal(numGrupo).replaceAll("uno","un")

		if(arrSufijos.size() > 1 && numGrupo != 1) {
			cardinal <<  arrSufijos[1]
		} else {
			cardinal <<  arrSufijos[0]
		}

		if( numResto != 0 ){
			cardinal <<  " "
			cardinal << getCardinal(numResto)
		}

		return cardinal.toString()
	}

	/**
	 * Obtiene las reglas para establecer los cardinales para las centenas (caso especial).
	 * @return Mapa con las reglas para calcular las centenas.
	 */
	private static Map getRulesCentenas() {
		Map rulesCentenas = [:]
		(DIGITO_MIN..DIGITO_MAX).each { digit ->
			switch ( digit ) {
				case 1:
					def centenas = centenasEspeciales[digit].split(SEPARADOR_REGLAS)
					rulesCentenas[digit] = { number, getCardinal ->
						def numResto = number[-2..-1]
						numResto == 0?centenas[0]:"${centenas[1]} ${getCardinal(numResto)}"
					}
					break
				case [5, 7, 9]:
					def centenas = centenasEspeciales[digit]
					rulesCentenas[digit] = { number, getCardinal ->
						def numResto = number[-2..-1]
						numResto == 0?centenas:"${centenas} ${getCardinal(numResto)}"
					}
					break
				default:
					rulesCentenas[digit] = { number, getCardinal ->
						getCardinalCommon(number, getCardinal)
					}
			}
		}
		return rulesCentenas
	}

	/**
	 * Obtiene las reglas para establecer los cardinales de la mayor&iacute;a de los n&uacute;meros.
	 * @return Mapa con las reglas para determinar los n&uacute;meros.
	 */
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
