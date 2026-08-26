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

package org.numerals.rules.gl

import org.numerals.Number

/**
 * Clase que conten as regras para obter os cardinais dun numero en Galego.<br/><br/>
 *
 * A propiedade estatica "rules" devolve as regras para obter os cardinais.
 *
 * Particularidades do Galego tratadas aqui:
 * - O conector "e" liga decenas e unidades (vinte e un), centenas e o resto
 *   (cento e vinte), e un grupo e o seu resto (mil e un).
 * - "cen" para exactamente 100, "cento" cando vai seguido de mais (cento e un).
 * - Cada centena ten a sua propia palabra (douscentos, trescentos, ...).
 * - 1000 e "mil" (non "un mil").
 * - Escala longa (millon = 10^6, billon = 10^12, trillon = 10^18).
 *
 * @author Josue Martinez Buenrrostro (@josuemb)
 */
class CardinalRules {

	private static final SEPARADOR_REGRAS = "/"
	private static final SEPARADOR_DECENAS = " e "
	private static final POSICION_MIN = 1
	private static final POSICION_MAX = 24
	private static final DIXITO_MIN = 0
	private static final DIXITO_MAX = 9
	private static final Map unidades = [0:"cero", 1:"un", 2:"dous",3:"tres",4:"catro",5:"cinco",6:"seis",7:"sete",8:"oito",9:"nove"]
	private static final Map decenas = [1:"dez", 2:"vinte",3:"trinta",4:"corenta",5:"cincuenta",6:"sesenta",7:"setenta",8:"oitenta",9:"noventa"]
	// Decenas de 10 a 19: irregulares en Galego (dez, once, ... dezanove).
	private static final Map decenasEspeciais = [0:"dez",1:"once",2:"doce",3:"trece",4:"catorce",5:"quince",6:"dezaseis",7:"dezasete",8:"dezaoito",9:"dezanove"]
	// Cada centena ten a sua palabra. O 1 e especial: cen (exacto) / cento (con resto).
	private static final Map centenas = [1:"cen${SEPARADOR_REGRAS}cento",2:"douscentos",3:"trescentos",4:"catrocentos",5:"cincocentos",6:"seiscentos",7:"setecentos",8:"oitocentos",9:"novecentos"]
	private static final List sufixos = [
		[intervalo:4..6,sufixo:" mil"],
		[intervalo:7..12,sufixo:" mill\u00F3n$SEPARADOR_REGRAS mill\u00F3ns"],
		[intervalo:13..18,sufixo:" bill\u00F3n$SEPARADOR_REGRAS bill\u00F3ns"],
		[intervalo:19..24,sufixo:" trill\u00F3n$SEPARADOR_REGRAS trill\u00F3ns"]
	]
	static final Map rules = [:]

	// Establece as regras para cada posicion.
	static {
		(POSICION_MIN..POSICION_MAX).each{ posicion ->
			switch( posicion ) {
				case 1: rules[posicion] = getRegrasUnidades()
					break
				case 2: rules[posicion] = getRegrasDecenas()
					break
				case 3: rules[posicion] = getRegrasCentenas()
					break
				default: rules[posicion] = getRegrasComun()
			}
		}
	}

	/**
	 * Consulta os sufixos para cada intervalo de posicions.
	 * @param posicion Posicion.
	 * @return Mapa co intervalo de cada sufixo e o propio sufixo.
	 */
	private static Map getSufixo(int posicion){
		Map sufixosTmp = sufixos.find{it.intervalo.isCase(posicion)}
		if(!sufixosTmp) {
			throw new MissingResourceException("Cannot found class suffix for position:$posicion", CardinalRules.class.name, "getSufixo")
		}
		return sufixosTmp
	}

	/**
	 * Obten as regras para as unidades (caso especial).
	 * @return Mapa coas regras para determinar os numeros.
	 */
	private static Map getRegrasUnidades(){
		Map regrasUnidades = [:]
		(DIXITO_MIN..DIXITO_MAX).each { dixito ->
			regrasUnidades[dixito] = { number ->
				regrasUnidades[dixito] = dixito == 0 && number.size()!=1?"":unidades[dixito]
			}
		}
		return regrasUnidades
	}

	/**
	 * Obten as regras para as decenas (caso especial).
	 * @return Mapa coas regras para determinar os numeros.
	 */
	private static Map getRegrasDecenas(){
		Map regrasDecenas = [:]
		(DIXITO_MIN..DIXITO_MAX).each { dixito ->
			switch ( dixito ) {
				// Decenas de 10 a 19: unha unica palabra irregular, sen conector "e".
				case 1: regrasDecenas[dixito] = { number, getCardinal ->
					decenasEspeciais[number[-1]]
				}
					break
				// 20 a 90: palabra, opcionalmente "e" + unidade.
				default: regrasDecenas[dixito] = { number, getCardinal ->
					StringBuilder cardinal = new StringBuilder()
					cardinal << decenas[dixito]
					if( number[-1] != 0 ) {
						cardinal << SEPARADOR_DECENAS
						cardinal << getCardinal(number[-1..-1])
					}
					return cardinal.toString()
				}
			}
		}
		return regrasDecenas
	}

	/**
	 * Obten as regras para as centenas (caso especial).
	 * @return Mapa coas regras para calcular as centenas.
	 */
	private static Map getRegrasCentenas() {
		Map regrasCentenas = [:]
		(DIXITO_MIN..DIXITO_MAX).each { dixito ->
			switch ( dixito ) {
				case 1:
					def cen = centenas[dixito].split(SEPARADOR_REGRAS)
					regrasCentenas[dixito] = { number, getCardinal ->
						def numResto = number[-2..-1]
						numResto == 0?cen[0]:"${cen[1]}${SEPARADOR_DECENAS}${getCardinal(numResto)}"
					}
					break
				default:
					def centena = centenas[dixito]
					regrasCentenas[dixito] = { number, getCardinal ->
						def numResto = number[-2..-1]
						numResto == 0?centena:"${centena}${SEPARADOR_DECENAS}${getCardinal(numResto)}"
					}
			}
		}
		return regrasCentenas
	}

	/**
	 * Obten as regras para os casos comuns (dos millares en diante).
	 * @param number Numero do que se desexa obter o cardinal.
	 * @param getCardinal Closure inxectado para chamar recursivamente ao calculo dos cardinais.
	 * @return Cadea co cardinal.
	 * @see groovy.lang.Closure
	 */
	private static String getCardinalComun ( Number number, Closure getCardinal ) {
		StringBuilder cardinal = new StringBuilder()

		def posicion = number.size()
		def sufixo = getSufixo(posicion)
		def arrSufixos = sufixo.sufixo.split(SEPARADOR_REGRAS)

		def numGrupo = number[-(posicion)..-(sufixo.intervalo.from)]
		def numResto = number[-(sufixo.intervalo.from-1)..-1]

		def grupoCardinal = getCardinal(numGrupo)

		// "1 mil" e "mil" (non "un mil"): omitir "un" cando o grupo e exactamente 1
		// e o sufixo e o singular "mil".
		if(!(numGrupo == 1 && sufixo.sufixo.trim() == "mil")) {
			cardinal << grupoCardinal
		}

		if(arrSufixos.size() > 1 && numGrupo != 1) {
			cardinal <<  arrSufixos[1]
		} else {
			cardinal <<  arrSufixos[0]
		}

		if( numResto != 0 ){
			// O Galego liga o ultimo grupo con "e" apenas cando ese resto e
			// menor que 100, ou e un multiplo exacto de 100 (cen/douscentos...). No
			// caso contrario (ex. 234, 567) usase un espazo: "un millon douscentos e trinta...".
			def restoStr = numResto.toString()
			def menorQue100 = restoStr.length() <= 2
			def multiploExactoDe100 = restoStr.length() >= 3 && restoStr.substring(1).every { it == '0' }
			cardinal << (menorQue100 || multiploExactoDe100 ? SEPARADOR_DECENAS : " ")
			cardinal << getCardinal(numResto)
		}

		return cardinal.toString().trim()
	}

	/**
	 * Obten as regras para a maioria dos numeros.
	 * @return Mapa coas regras para determinar os numeros.
	 */
	private static Map getRegrasComun() {
		Map regrasTmp = [:]
		(DIXITO_MIN..DIXITO_MAX).each { dixito ->
			regrasTmp[dixito] = { number, getCardinal ->
				getCardinalComun(number, getCardinal)
			}
		}
		return regrasTmp
	}
}
