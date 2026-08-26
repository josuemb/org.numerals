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
 * Classe que contem as regras para obter os cardinais de um numero em
 * Portugues.<br/><br/>
 *
 * A propriedade estatica "rules" devolve as regras para obter os cardinais.
 *
 * Particularidades do Portugues tratadas aqui:
 * - O conector "e" liga dezenas e unidades (vinte e tres), centenas e o resto
 *   (cento e vinte), e um grupo e o seu resto (mil e um).
 * - "cem" para exatamente 100, "cento" quando seguido de mais (cento e um).
 * - Cada centena tem a sua propria palavra (duzentos, trezentos, ...).
 * - 1000 e "mil" (nao "um mil").
 *
 * @author Josue Martinez Buenrrostro (@josuemb)
 */
class CardinalRules {

	private static final SEPARADOR_REGRAS = "/"
	private static final SEPARADOR_DEZENAS = " e "
	private static final POSICAO_MIN = 1
	private static final POSICAO_MAX = 24
	private static final DIGITO_MIN = 0
	private static final DIGITO_MAX = 9
	private static final Map unidades = [0:"zero", 1:"um", 2:"dois",3:"tres",4:"quatro",5:"cinco",6:"seis",7:"sete",8:"oito",9:"nove"]
	private static final Map dezenas = [1:"dez", 2:"vinte",3:"trinta",4:"quarenta",5:"cinquenta",6:"sessenta",7:"setenta",8:"oitenta",9:"noventa"]
	// Dezenas de 10 a 19: irregulares em Portugues (dez, onze, ... dezenove).
	private static final Map dezenasEspeciais = [0:"dez",1:"onze",2:"doze",3:"treze",4:"catorze",5:"quinze",6:"dezesseis",7:"dezessete",8:"dezoito",9:"dezenove"]
	// Cada centena tem a sua palavra. O 1 e especial: cem (exato) / cento (com resto).
	private static final Map centenas = [1:"cem${SEPARADOR_REGRAS}cento",2:"duzentos",3:"trezentos",4:"quatrocentos",5:"quinhentos",6:"seiscentos",7:"setecentos",8:"oitocentos",9:"novecentos"]
	private static final List sufixos = [
		[intervalo:4..6,sufixo:" mil"],
		[intervalo:7..12,sufixo:" milh\u00E3o$SEPARADOR_REGRAS milh\u00F5es"],
		[intervalo:13..18,sufixo:" bilh\u00E3o$SEPARADOR_REGRAS bilh\u00F5es"],
		[intervalo:19..24,sufixo:" trilh\u00E3o$SEPARADOR_REGRAS trilh\u00F5es"]
	]
	static final Map rules = [:]

	// Estabelece as regras para cada posicao.
	static {
		(POSICAO_MIN..POSICAO_MAX).each{ posicao ->
			switch( posicao ) {
				case 1: rules[posicao] = getRegrasUnidades()
					break
				case 2: rules[posicao] = getRegrasDezenas()
					break
				case 3: rules[posicao] = getRegrasCentenas()
					break
				default: rules[posicao] = getRegrasComum()
			}
		}
	}

	/**
	 * Consulta os sufixos para cada intervalo de posicoes.
	 * @param posicao Posicao.
	 * @return Mapa com o intervalo de cada sufixo e o proprio sufixo.
	 */
	private static Map getSufixo(int posicao){
		Map sufixosTmp = sufixos.find{it.intervalo.isCase(posicao)}
		if(!sufixosTmp) {
			throw new MissingResourceException("Cannot found class suffix for position:$posicao", CardinalRules.class.name, "getSufixo")
		}
		return sufixosTmp
	}

	/**
	 * Obtem as regras para as unidades (caso especial).
	 * @return Mapa com as regras para determinar os numeros.
	 */
	private static Map getRegrasUnidades(){
		Map regrasUnidades = [:]
		(DIGITO_MIN..DIGITO_MAX).each { digito ->
			regrasUnidades[digito] = { number ->
				regrasUnidades[digito] = digito == 0 && number.size()!=1?"":unidades[digito]
			}
		}
		return regrasUnidades
	}

	/**
	 * Obtem as regras para as dezenas (caso especial).
	 * @return Mapa com as regras para determinar os numeros.
	 */
	private static Map getRegrasDezenas(){
		Map regrasDezenas = [:]
		(DIGITO_MIN..DIGITO_MAX).each { digito ->
			switch ( digito ) {
				// Dezenas de 10 a 19: uma unica palavra irregular, sem conector "e".
				case 1: regrasDezenas[digito] = { number, getCardinal ->
					dezenasEspeciais[number[-1]]
				}
					break
				// 20 a 90: palavra, opcionalmente "e" + unidade.
				default: regrasDezenas[digito] = { number, getCardinal ->
					StringBuilder cardinal = new StringBuilder()
					cardinal << dezenas[digito]
					if( number[-1] != 0 ) {
						cardinal << SEPARADOR_DEZENAS
						cardinal << getCardinal(number[-1..-1])
					}
					return cardinal.toString()
				}
			}
		}
		return regrasDezenas
	}

	/**
	 * Obtem as regras para as centenas (caso especial).
	 * @return Mapa com as regras para calcular as centenas.
	 */
	private static Map getRegrasCentenas() {
		Map regrasCentenas = [:]
		(DIGITO_MIN..DIGITO_MAX).each { digito ->
			switch ( digito ) {
				case 1:
					def cem = centenas[digito].split(SEPARADOR_REGRAS)
					regrasCentenas[digito] = { number, getCardinal ->
						def numResto = number[-2..-1]
						numResto == 0?cem[0]:"${cem[1]}${SEPARADOR_DEZENAS}${getCardinal(numResto)}"
					}
					break
				default:
					def centena = centenas[digito]
					regrasCentenas[digito] = { number, getCardinal ->
						def numResto = number[-2..-1]
						numResto == 0?centena:"${centena}${SEPARADOR_DEZENAS}${getCardinal(numResto)}"
					}
			}
		}
		return regrasCentenas
	}

	/**
	 * Obtem as regras para os casos comuns (de milhares em diante).
	 * @param number Numero do qual se deseja obter o cardinal.
	 * @param getCardinal Closure injetado para chamar recursivamente o calculo dos cardinais.
	 * @return Cadeia com o cardinal.
	 * @see groovy.lang.Closure
	 */
	private static String getCardinalComum ( Number number, Closure getCardinal ) {
		StringBuilder cardinal = new StringBuilder()

		def posicao = number.size()
		def sufixo = getSufixo(posicao)
		def arrSufixos = sufixo.sufixo.split(SEPARADOR_REGRAS)

		def numGrupo = number[-(posicao)..-(sufixo.intervalo.from)]
		def numResto = number[-(sufixo.intervalo.from-1)..-1]

		def grupoCardinal = getCardinal(numGrupo)

		// "1 mil" e "mil" (nao "um mil"): omitir "um" quando o grupo e exatamente 1
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
			// O Portugues liga o ultimo grupo com "e" apenas quando esse resto e
			// menor que 100, ou e um multiplo exato de 100 (cem/duzentos...). Caso
			// contrario (ex. 234, 567) usa-se um espaco: "um milhao duzentos e trinta...".
			def restoStr = numResto.toString()
			def menorQue100 = restoStr.length() <= 2
			def multiploExatoDe100 = restoStr.length() >= 3 && restoStr.substring(1).every { it == '0' }
			cardinal << (menorQue100 || multiploExatoDe100 ? SEPARADOR_DEZENAS : " ")
			cardinal << getCardinal(numResto)
		}

		return cardinal.toString().trim()
	}

	/**
	 * Obtem as regras para a maioria dos numeros.
	 * @return Mapa com as regras para determinar os numeros.
	 */
	private static Map getRegrasComum() {
		Map regrasTmp = [:]
		(DIGITO_MIN..DIGITO_MAX).each { digito ->
			regrasTmp[digito] = { number, getCardinal ->
				getCardinalComum(number, getCardinal)
			}
		}
		return regrasTmp
	}
}
