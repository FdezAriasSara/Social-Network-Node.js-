package com.uniovi.sdipractica234.pageobjects;

import com.uniovi.sdipractica234.util.SeleniumUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

public class PO_LoginView extends PO_NavView{


    static public List<WebElement> getLoginText(WebDriver driver, int language) {
        //Esperamos a que se cargue el saludo de bienvenida en Español
        return SeleniumUtils.waitLoadElementsBy(driver,
                "text",
                p.getString("login.title", language),
                getTimeout());
    }


    public static void goToLoginPage(WebDriver driver){
        driver.get("localhost:8090/login");
    }

    static public void fillForm(WebDriver driver, String usernamep,  String passwordp) {
        WebElement dni = driver.findElement(By.name("username"));
        dni.click();
        dni.clear();
        dni.sendKeys(usernamep);
        WebElement password = driver.findElement(By.name("password"));
        password.click();
        password.clear();
        password.sendKeys(passwordp);

        //Pulsar el boton de Alta.
        By boton = By.className("btn");
        driver.findElement(boton).click();
    }


}
