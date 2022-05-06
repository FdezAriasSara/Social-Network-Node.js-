package com.uniovi.sdipractica234.pageobjects;

import com.uniovi.sdipractica234.util.SeleniumUtils;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

public class PO_ErrorView extends PO_View{

    public static List<WebElement> getErrorText(WebDriver driver, int language){
        return SeleniumUtils.waitLoadElementsBy(driver,
                "text",
                p.getString("ohoh.message", language),
                getTimeout());

    }
}
