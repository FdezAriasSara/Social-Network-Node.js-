package com.uniovi.sdipractica234.pageobjects;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class PO_APIClientView extends PO_NavView {
    public static void goToApiView(WebDriver driver){
        driver.get("localhost:8090/apiclient/client.html");
        PO_View.checkElementBy(driver, "id", "email");
    }

    static public void fillForm(WebDriver driver, String usernamep,  String passwordp) {
        WebElement dni = driver.findElement(By.name("email"));
        dni.click();
        dni.clear();
        dni.sendKeys(usernamep);
        WebElement password = driver.findElement(By.name("password"));
        password.click();
        password.clear();
        password.sendKeys(passwordp);

        //Pulsar el boton de Alta.
        By boton = By.id("boton-login");
        driver.findElement(boton).click();
    }
}
