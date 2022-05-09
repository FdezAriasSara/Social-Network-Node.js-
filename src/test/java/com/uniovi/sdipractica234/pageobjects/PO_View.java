package com.uniovi.sdipractica234.pageobjects;

import com.uniovi.sdipractica234.util.SeleniumUtils;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

public class PO_View {

	protected static int timeout = 20;

	public static int getTimeout() {
		return timeout;
	}

	public static void setTimeout(int timeout) {
		PO_View.timeout = timeout;
	}


	/**
	 *  Espera por la visibilidad de un elemento/s en la vista actualmente cargandose en driver..
	 * 
	 * @param driver: apuntando al navegador abierto actualmente.
	 * @param type: 
	 * @param text:
	 * @return Se retornará la lista de elementos resultantes de la búsqueda.
	 */
	static public List<WebElement> checkElementBy(WebDriver driver, String type, String text) {
		return  SeleniumUtils.waitLoadElementsBy(driver, type, text, getTimeout());
	}

}
