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

package org.numerals.rules.ro

import org.numerals.Number

/**
 * Clasa care contine regulile pentru obtinerea cardinalelor unui numar in
 * limba Romana.<br/><br/>
 *
 * Proprietatea statica "rules" returneaza regulile pentru obtinerea cardinalelor.
 *
 * Particularitati ale limbii Romane tratate aici:
 * - Adolescentele (11-19) se formeaza cu "spre": unsprezece, doisprezece, ...,
 *   nouasprezece; formele pentru 14 si 16 sunt neregulate (paisprezece, saisprezece).
 * - Zecile plus unitatile se leaga cu conjunctia "si": douazeci si unu, treizeci si doi.
 * - Sutele: o suta (100), doua sute (200), ...; se ataseaza restul cu spatiu,
 *   fara conector (o suta douazeci si trei).
 * - Scala foloseste o palabra la fiecare 3 cifre (ca in Italiana, NU blocuri de 6):
 *   mie/mii (10^3), milion/milioane (10^6), miliard/miliarde (10^9),
 *   trilion/trilioane (10^12), cvadrilion (10^15), cvintilion (10^18), sextilion (10^21).
 * - Genul cuvintelor de scala: "mie" este feminin (o mie, doua mii), iar milion,
 *   miliard, trilion, ... sunt neutre (un milion, doua milioane). De aceea grupul
 *   egal cu 1 devine "o" inainte de mie si "un" inainte de milion/miliard/...
 * - Regula "de": prepozitia "de" se insereaza intre numarul de grupuri si cuvantul
 *   de scala cand ultimele doua cifre ale grupului sunt 00 sau in intervalul 20-99
 *   (douazeci de mii, o suta de milioane), dar nu pentru 1-19 (doua mii, cinci milioane).
 *
 * @author Josue Martinez Buenrrostro (@josuemb)
 */
class CardinalRules {

	private static final SEPARATOR_REGULI = "/"
	private static final SEPARATOR_ZECI = " si "
	private static final CONECTOR_DE = " de "
	private static final SUFIX_MIE = "mie"
	private static final POZITIE_MIN = 1
	private static final POZITIE_MAX = 24
	private static final CIFRA_MIN = 0
	private static final CIFRA_MAX = 9
	// Unitatile in forma de numarare (masculina implicita): unu, doi, ...
	private static final Map unitati = [0:"zero", 1:"unu", 2:"doi",3:"trei",4:"patru",5:"cinci",6:"sase",7:"sapte",8:"opt",9:"noua"]
	// Unitatile in forma feminina, folosite inaintea sutelor si a cuvintelor de scala
	// (doua sute, doua mii, doua milioane). Restul cifrelor coincid cu forma de numarare.
	private static final Map unitatiFeminine = [2:"doua"]
	private static final Map zeci = [1:"zece", 2:"douazeci",3:"treizeci",4:"patruzeci",5:"cincizeci",6:"saizeci",7:"saptezeci",8:"optzeci",9:"nouazeci"]
	// Adolescentele 10-19: o singura palabra neregulata, formata cu "spre".
	// 14 (paisprezece) si 16 (saisprezece) nu urmeaza exact regula.
	private static final Map zeciSpeciale = [0:"zece",1:"unsprezece",2:"doisprezece",3:"treisprezece",4:"paisprezece",5:"cincisprezece",6:"saisprezece",7:"saptesprezece",8:"optsprezece",9:"nouasprezece"]
	// Cuvantul pentru suta: "o suta" (exact 100) / "suta" (folosit la 100 cu rest).
	// Sutele 200-900 se formeaza cu forma feminina a cifrei plus "sute".
	private static final SUTA_SINGULAR = "o suta"
	private static final SUTE_PLURAL = "sute"
	// Scala romaneasca: spre deosebire de Spaniola/Portugheza (care grupeaza pe blocuri
	// de 6 cifre), Romana are un cuvant de scala la fiecare 3 cifre dincolo de mie.
	// Fiecare interval acopera 3 pozitii, cu magnitudinile reale ale limbii:
	//   10^3  mie/mii        10^6  milion       10^9  miliard
	//   10^12 trilion        10^15 cvadrilion   10^18 cvintilion   10^21 sextilion
	// Fiecare sufix are forma de singular si de plural, separate prin SEPARATOR_REGULI.
	private static final List sufixe = [
		[interval:4..6,sufix:" mie$SEPARATOR_REGULI mii"],
		[interval:7..9,sufix:" milion$SEPARATOR_REGULI milioane"],
		[interval:10..12,sufix:" miliard$SEPARATOR_REGULI miliarde"],
		[interval:13..15,sufix:" trilion$SEPARATOR_REGULI trilioane"],
		[interval:16..18,sufix:" cvadrilion$SEPARATOR_REGULI cvadrilioane"],
		[interval:19..21,sufix:" cvintilion$SEPARATOR_REGULI cvintilioane"],
		[interval:22..24,sufix:" sextilion$SEPARATOR_REGULI sextilioane"]
	]
	static final Map rules = [:]

	// Stabileste regulile pentru fiecare pozitie.
	static {
		(POZITIE_MIN..POZITIE_MAX).each{ pozitie ->
			switch( pozitie ) {
				case 1: rules[pozitie] = getReguliUnitati()
					break
				case 2: rules[pozitie] = getReguliZeci()
					break
				case 3: rules[pozitie] = getReguliSute()
					break
				default: rules[pozitie] = getReguliComune()
			}
		}
	}

	/**
	 * Consulta sufixele pentru fiecare interval de pozitii.
	 * @param pozitie Pozitia.
	 * @return Mapa cu intervalul fiecarui sufix si sufixul propriu-zis.
	 */
	private static Map getSufix(int pozitie){
		Map sufixeTmp = sufixe.find{it.interval.isCase(pozitie)}
		if(!sufixeTmp) {
			throw new MissingResourceException("Cannot found class suffix for position:$pozitie", CardinalRules.class.name, "getSufix")
		}
		return sufixeTmp
	}

	/**
	 * Obtine regulile pentru unitati (caz special).
	 * @return Mapa cu regulile pentru determinarea numerelor.
	 */
	private static Map getReguliUnitati(){
		Map reguliUnitati = [:]
		(CIFRA_MIN..CIFRA_MAX).each { cifra ->
			reguliUnitati[cifra] = { number ->
				reguliUnitati[cifra] = cifra == 0 && number.size()!=1?"":unitati[cifra]
			}
		}
		return reguliUnitati
	}

	/**
	 * Obtine regulile pentru zeci (caz special).
	 * Adolescentele (10-19) sunt o singura palabra; 20-99 leaga unitatea cu "si".
	 * @return Mapa cu regulile pentru determinarea numerelor.
	 */
	private static Map getReguliZeci(){
		Map reguliZeci = [:]
		(CIFRA_MIN..CIFRA_MAX).each { cifra ->
			switch ( cifra ) {
				// Zeci de la 10 la 19: o singura palabra neregulata, fara conector.
				case 1: reguliZeci[cifra] = { number, getCardinal ->
					zeciSpeciale[number[-1]]
				}
					break
				// 20 la 90: palabra zecii, optional conectorul "si" plus unitatea.
				default: reguliZeci[cifra] = { number, getCardinal ->
					StringBuilder cardinal = new StringBuilder()
					cardinal << zeci[cifra]
					if( number[-1] != 0 ) {
						cardinal << SEPARATOR_ZECI
						cardinal << getCardinal(number[-1..-1])
					}
					return cardinal.toString()
				}
			}
		}
		return reguliZeci
	}

	/**
	 * Obtine regulile pentru sute (caz special).
	 * "o suta" pentru exact 100; "suta" cand este urmata de rest; 200-900 folosesc
	 * forma feminina a cifrei plus "sute" (doua sute, trei sute, ...).
	 * @return Mapa cu regulile pentru calcularea sutelor.
	 */
	private static Map getReguliSute() {
		Map reguliSute = [:]
		(CIFRA_MIN..CIFRA_MAX).each { cifra ->
			switch ( cifra ) {
				case 1:
					reguliSute[cifra] = { number, getCardinal ->
						def numRest = number[-2..-1]
						numRest == 0?SUTA_SINGULAR:"$SUTA_SINGULAR ${getCardinal(numRest)}"
					}
					break
				default:
					def cifraFeminina = unitatiFeminine[cifra]?:unitati[cifra]
					def suta = "$cifraFeminina $SUTE_PLURAL"
					reguliSute[cifra] = { number, getCardinal ->
						def numRest = number[-2..-1]
						numRest == 0?suta:"$suta ${getCardinal(numRest)}"
					}
			}
		}
		return reguliSute
	}

	/**
	 * Obtine regulile pentru cazurile comune (de la mii in sus).
	 * @param number Numarul din care se doreste obtinerea cardinalului.
	 * @param getCardinal Closure injectat pentru a apela recursiv calculul cardinalelor.
	 * @return Sirul cu cardinalul.
	 * @see groovy.lang.Closure
	 */
	private static String getCardinalComun ( Number number, Closure getCardinal ) {
		StringBuilder cardinal = new StringBuilder()

		def pozitie = number.size()
		def sufix = getSufix(pozitie)
		def arrSufixe = sufix.sufix.split(SEPARATOR_REGULI)

		def numGrup = number[-(pozitie)..-(sufix.interval.from)]
		def numRest = number[-(sufix.interval.from-1)..-1]

		boolean grupUnu = numGrup == 1
		boolean sufixMie = arrSufixe[0].trim() == SUFIX_MIE

		def grupCardinal = getCardinal(numGrup)

		// Forma feminina "doua" inaintea oricarui cuvant de scala (mie este feminin;
		// milion, miliard, ... sunt neutre, deci feminine la plural): doua mii, doua milioane.
		grupCardinal = grupCardinal.replaceAll(/\bdoi\b/, unitatiFeminine[2])

		if( grupUnu ) {
			// Grupul exact 1: "o mie" (mie este feminin) sau "un milion" (scala neutra).
			cardinal << (sufixMie ? "o" : "un")
		} else {
			// "...unu" final devine "una" inaintea femininului "mie" (douazeci si una de
			// mii), dar ramane "unu" inaintea scalelor neutre (douazeci si unu de milioane).
			if( sufixMie ) {
				grupCardinal = grupCardinal.replaceAll(/\bunu\b/, "una")
			}
			cardinal << grupCardinal
		}

		if(arrSufixe.size() > 1 && !grupUnu) {
			cardinal <<  arrSufixe[1]
		} else {
			cardinal <<  arrSufixe[0]
		}

		if( numRest != 0 ){
			cardinal <<  " "
			cardinal << getCardinal(numRest)
		}

		return cardinal.toString().trim()
	}

	/**
	 * Obtine regulile pentru majoritatea numerelor.
	 * Aplica regula "de" (douazeci de mii, o suta de milioane) inainte de cuvantul de
	 * scala, cand ultimele doua cifre ale grupului sunt 00 sau intre 20 si 99.
	 * @return Mapa cu regulile pentru determinarea numerelor.
	 */
	private static Map getReguliComune() {
		Map reguliTmp = [:]
		(CIFRA_MIN..CIFRA_MAX).each { cifra ->
			reguliTmp[cifra] = { number, getCardinal ->
				aplicaRegulaDe(number, getCardinal)
			}
		}
		return reguliTmp
	}

	/**
	 * Insereaza prepozitia "de" intre grup si cuvantul de scala cand regula o cere,
	 * apoi delega calculul catre getCardinalComun.
	 * Ultimele doua cifre ale grupului determina regula: 00 sau 20-99 cer "de";
	 * 1-19 nu (doua mii, cinci milioane vs douazeci de mii, o suta de milioane).
	 * @param number Numarul din care se doreste obtinerea cardinalului.
	 * @param getCardinal Closure injectat pentru a apela recursiv calculul cardinalelor.
	 * @return Sirul cu cardinalul.
	 * @see groovy.lang.Closure
	 */
	private static String aplicaRegulaDe ( Number number, Closure getCardinal ) {
		def pozitie = number.size()
		def sufix = getSufix(pozitie)
		def numGrup = number[-(pozitie)..-(sufix.interval.from)]

		def cardinalBrut = getCardinalComun(number, getCardinal)

		if( !necesitaDe(numGrup) ) {
			return cardinalBrut
		}

		// Insereaza "de" imediat inainte de cuvantul de scala (primul termen dupa grup).
		// arrSufixe contine sufixul cu spatiu initial (ex. " mii"): il localizam pentru a
		// pune "de" inaintea lui fara a afecta restul cardinalului.
		def arrSufixe = sufix.sufix.split(SEPARATOR_REGULI)
		def cuvantScalaSingular = arrSufixe[0]
		def cuvantScalaPlural = arrSufixe.size() > 1 ? arrSufixe[1] : arrSufixe[0]
		def cuvantScala = numGrup == 1 ? cuvantScalaSingular : cuvantScalaPlural

		int indice = cardinalBrut.indexOf(cuvantScala.trim())
		if( indice <= 0 ) {
			return cardinalBrut
		}
		StringBuilder cardinal = new StringBuilder()
		cardinal << cardinalBrut.substring(0, indice).trim()
		cardinal << CONECTOR_DE
		cardinal << cardinalBrut.substring(indice)
		return cardinal.toString()
	}

	/**
	 * Determina daca grupul cere prepozitia "de" inaintea cuvantului de scala.
	 * Adevarat cand ultimele doua cifre ale grupului sunt 00 sau in intervalul 20-99.
	 * @param numGrup Grupul (numarul de mii, de milioane, ...).
	 * @return true daca grupul cere "de", false altfel.
	 */
	private static boolean necesitaDe(Number numGrup) {
		def text = numGrup.toString()
		def ultimeleDoua = text.length() >= 2 ? text.substring(text.length() - 2) : text
		int valoare = ultimeleDoua.toInteger()
		return valoare == 0 || valoare >= 20
	}
}
