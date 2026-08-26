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

package org.numerals.rules.ast

import org.numerals.Number

/**
 * Clas que contien les regles pa obtener los cardinales d'un numberu
 * en llingua Asturiana.<br/><br/>
 *
 * La propied&aacute; est&aacute;tica "rules" devuelve les regles pa
 * obtener los cardinales.
 *
 * Formes normatives (Academia de la Llingua Asturiana / ALLA):
 * - Unidaes: ceru, un/unu, dos, tres, cuatro, cinco, seis, siete, ocho, nueve.
 * - El 1 aisll&aacute;u ye "unu"; como determinante o en composicion: "un".
 *   Na composicion de millares/millones aplica apocope "un" (un mill&oacute;n).
 * - Teens (16-19): diecis&eacute;is, diecisiete, deciocho, diecinueve
 *   (forma Wiktionary/ALLA; diecis&eacute;is con tilde na &eacute;).
 * - Decenes: diez, venti, trenta, cuarenta, cincuenta, sesenta, setenta,
 *   ochenta, noventa.
 * - Vientigrupu (21-29): venti&uacute;n, ventid&oacute;s, ventitr&eacute;s,
 *   venticuatro, venticinco, ventis&eacute;is, ventisiete, ventiocho,
 *   ventinueve. Nota: a diferencia del espa&ntilde;ol, 21 aisll&aacute;u
 *   ye "venti&uacute;n" (siempre apocopau con tilde), non "ventiuno".
 * - Centenes: cien (exactu 100), cientu (con restu); doscientos, trescientos,
 *   cuatrocientos, quinientos, seiscientos, setecientos, ochocientos,
 *   novecientos.
 * - Escala llarga europea: mill&oacute;n (10^6), bill&oacute;n (10^12),
 *   trill&oacute;n (10^18). 1000 = "mil" (non "un mil").
 * - Conector "y" ente decenes y unidaes (trenta y un, cuarenta y dos).
 *
 * Referencies:
 * - Wiktionary Module:number_list/data/ast (consultau 26 ago 2026).
 * - languagesandnumbers.com/how-to-count-in-asturian (consultau 26 ago 2026).
 *
 * @author Josue Mart&iacute;nez Buenrrostro (@josuemb)
 */
class CardinalRules {

	private static final SEPARADOR_REGLES = "/"
	private static final SEPARADOR_DECENES = " y "
	private static final POSICION_MIN = 1
	private static final POSICION_MAX = 24
	private static final DIXITU_MIN = 0
	private static final DIXITU_MAX = 9
	private static final Map unidaes = [0:"ceru", 1:"unu", 2:"dos", 3:"tres", 4:"cuatro", 5:"cinco", 6:"seis", 7:"siete", 8:"ocho", 9:"nueve"]
	private static final Map decenes = [1:"diez", 2:"venti", 3:"trenta", 4:"cuarenta", 5:"cincuenta", 6:"sesenta", 7:"setenta", 8:"ochenta", 9:"noventa"]
	// Teens 11-15: formes propies (iguales al espanol en grafía, normatives n'asturianu).
	private static final Map decenesEspecialesUnu = [1:"once", 2:"doce", 3:"trece", 4:"catorce", 5:"quince"]
	// Teens 16-19: aglutinación "dieci-" + dixitu (con tilde en 16: dieciséis).
	private static final Map decenesEspecialesDieci = [6:"diecis\u00E9is", 7:"diecisiete", 8:"dieciocho", 9:"diecinueve"]
	// 20s: prefixu "venti" + dixitu (con tildes: ventiún, ventidós, ventitrés, ventiséis).
	private static final Map ventigrupu = [1:"venti\u00FAn", 2:"ventid\u00F3s", 3:"ventitr\u00E9s", 4:"venticuatro", 5:"venticinco", 6:"ventis\u00E9is", 7:"ventisiete", 8:"ventiocho", 9:"ventinueve"]
	// Centenes: 1 ye especial (cien exactu / cientu con restu), 5 ye "quinientos".
	private static final Map centenesEspeciales = [1:"cien${SEPARADOR_REGLES}cientu", 5:"quinientos", 7:"setecientos", 9:"novecientos"]
	private static final List sufixos = [
		[rangu:3..3, sufixu:"cientos"],
		[rangu:4..6, sufixu:" mil"],
		[rangu:7..12, sufixu:" mill\u00F3n$SEPARADOR_REGLES millones"],
		[rangu:13..18, sufixu:" bill\u00F3n$SEPARADOR_REGLES billones"],
		[rangu:19..24, sufixu:" trill\u00F3n$SEPARADOR_REGLES trillones"]
	]
	static final Map rules = [:]

	// Establez les regles pa cada posicion.
	static {
		(POSICION_MIN..POSICION_MAX).each{ posicion ->
			switch( posicion ) {
				case 1: rules[posicion] = getReglesUnidaes()
					break
				case 2: rules[posicion] = getReglesDecenes()
					break
				case 3: rules[posicion] = getReglesCentenes()
					break
				default: rules[posicion] = getReglesComun()
			}
		}
	}

	/**
	 * Consulta los sufixos pa cada rangu de posiciones.
	 * @param posicion Posicion.
	 * @return Mapa col rangu de cada sufixu y el propiu sufixu.
	 */
	private static Map getSufixu(int posicion){
		Map sufixosTmp = sufixos.find{it.rangu.isCase(posicion)}
		if(!sufixosTmp) {
			throw new MissingResourceException("Cannot found class suffix for position:$posicion", CardinalRules.class.name, "getSufixu")
		}
		return sufixosTmp
	}

	/**
	 * Obtien les regles pa les unidaes (casu especial).
	 * @return Mapa coles regles pa determinar los numberos.
	 */
	private static Map getReglesUnidaes(){
		Map reglesUnidaes = [:]
		(DIXITU_MIN..DIXITU_MAX).each { dixitu ->
			reglesUnidaes[dixitu] = { number ->
				reglesUnidaes[dixitu] = dixitu == 0 && number.size()!=1?"":unidaes[dixitu]
			}
		}
		return reglesUnidaes
	}

	/**
	 * Obtien el cardinal pa les decenes.
	 * @param number Numberu (solo decenes) del que se quier obtener el cardinal.
	 * @param getCardinal Closure que s'inyecta automaticamente pa llamar recursivamente.
	 * @param decena Valor de la decena.
	 * @return Cadena col Cardinal.
	 * @see groovy.lang.Closure
	 */
	private static String getCardinalDecenes ( Number number, Closure getCardinal, int decena ) {
		StringBuilder cardinal = new StringBuilder()
		cardinal << decenes[decena]
		if( number[-1] != 0 ) {
			cardinal << SEPARADOR_DECENES
			// N'asturianu, "unu" aislau se caltien como "unu" nes unidaes,
			// pero na composicion "trenta y un" aplica apocope (el motor
			// devuelve "unu" y aqui facemos replace pa "un").
			def unidaCardin = getCardinal(number[-1..-1])
			unidaCardin = unidaCardin.replaceAll(/\bunu\b/, "un")
			cardinal << unidaCardin
		}
		return cardinal.toString()
	}

	/**
	 * Obtien les regles pa les decenes (casu especial).
	 * @return Mapa coles regles pa determinar los numberos.
	 */
	private static Map getReglesDecenes(){
		Map reglesDecenes = [:]
		(DIXITU_MIN..DIXITU_MAX).each { dixitu ->
			switch ( dixitu ) {
				// 1x: teens (10..19) - formen propies
				case 1: reglesDecenes[dixitu] = { number, getCardinal ->
					String cardinal
					switch (number[-1]) {
						case 0: cardinal = decenes[dixitu]
							break
						case 1..5: cardinal = decenesEspecialesUnu[number[-1]]
							break
						default:
							cardinal = decenesEspecialesDieci[number[-1]]
					}
					return cardinal
				}
					break
				// 2x: vientigrupu (20..29) - una sola pallabra compuesta
				case 2: reglesDecenes[dixitu] = { number, getCardinal ->
					number[-1] == 0 ? decenes[dixitu] : ventigrupu[number[-1]]
				}
					break
				// 3x-9x: decena + " y " + unida (con apocope unu->un)
				default: reglesDecenes[dixitu] = { number, getCardinal ->
					getCardinalDecenes(number, getCardinal, dixitu)
				}
			}
		}
		return reglesDecenes
	}

	/**
	 * Obtien les regles pa los casos comunes (de millares p'alantre).
	 * @param number Numberu del que se quier obtener el cardinal.
	 * @param getCardinal Closure pa llamar recursivamente al calculo.
	 * @return Cadena col Cardinal.
	 * @see groovy.lang.Closure
	 */
	private static String getCardinalComun ( Number number, Closure getCardinal ) {
		StringBuilder cardinal = new StringBuilder()

		def pos = number.size()
		def sufixu = getSufixu(pos)

		def arrSufixos = sufixu.sufixu.split(SEPARADOR_REGLES)

		def numGrupu = number[-(pos)..-(sufixu.rangu.from)]
		def numRestu = number[-(sufixu.rangu.from-1)..-1]

		// Apocope de "unu" -> "un" en composicion (un millon, ventiun mil).
		def grupuCardinal = getCardinal(numGrupu)
		grupuCardinal = grupuCardinal.replaceAll(/\bunu\b/, "un")
		// Na composicion de 20s, "ventiún" se caltien tal cual
		// (yá ta apocopau y con tilde nel ventigrupu).

		// "1 mil" dizse "mil" (non "un mil"): omitir "un" cuando'l grupu
		// ye exactamente 1 y el sufixu ye'l singular "mil".
		if(!(numGrupu == 1 && sufixu.sufixu.trim() == "mil")) {
			cardinal << grupuCardinal
		}

		if(arrSufixos.size() > 1 && numGrupu != 1) {
			cardinal <<  arrSufixos[1]
		} else {
			cardinal <<  arrSufixos[0]
		}

		if( numRestu != 0 ){
			cardinal <<  " "
			cardinal << getCardinal(numRestu)
		}

		return cardinal.toString().trim()
	}

	/**
	 * Obtien les regles pa les centenes (casu especial).
	 * @return Mapa coles regles pa calcular les centenes.
	 */
	private static Map getReglesCentenes() {
		Map reglesCentenes = [:]
		(DIXITU_MIN..DIXITU_MAX).each { dixitu ->
			switch ( dixitu ) {
				case 1:
					def centenes = centenesEspeciales[dixitu].split(SEPARADOR_REGLES)
					reglesCentenes[dixitu] = { number, getCardinal ->
						def numRestu = number[-2..-1]
						numRestu == 0 ? centenes[0] : "${centenes[1]} ${getCardinal(numRestu)}"
					}
					break
				case [5, 7, 9]:
					def centena = centenesEspeciales[dixitu]
					reglesCentenes[dixitu] = { number, getCardinal ->
						def numRestu = number[-2..-1]
						numRestu == 0 ? centena : "${centena} ${getCardinal(numRestu)}"
					}
					break
				default:
					reglesCentenes[dixitu] = { number, getCardinal ->
						getCardinalComun(number, getCardinal)
					}
			}
		}
		return reglesCentenes
	}

	/**
	 * Obtien les regles pa la mayoria de los numberos.
	 * @return Mapa coles regles pa determinar los numberos.
	 */
	private static Map getReglesComun() {
		Map reglesTmp = [:]
		(DIXITU_MIN..DIXITU_MAX).each { dixitu ->
			reglesTmp[dixitu] = { number, getCardinal ->
				getCardinalComun(number, getCardinal)
			}
		}
		return reglesTmp
	}
}
