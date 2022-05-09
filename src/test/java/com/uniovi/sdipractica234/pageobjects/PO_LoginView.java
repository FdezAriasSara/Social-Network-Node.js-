package com.uniovi.sdipractica234.pageobjects;

import com.uniovi.sdipractica234.util.SeleniumUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

public class PO_LoginView extends PO_NavView{


     public static void goToLoginPage(WebDriver driver){
        driver.get("localhost:8090/users/login");
    }

    static public void fillForm(WebDriver driver, String usernamep,  String passwordp) {
        WebElement dni = driver.findElement(By.name("email"));
        dni.click();
        dni.clear();
        dni.sendKeys(usernamep);
        WebElement password = driver.findElement(By.name("contraseña"));
        password.click();
        password.clear();
        password.sendKeys(passwordp);

        //Pulsar el boton de Alta.
        By boton = By.className("btn");
        driver.findElement(boton).click();
    }


}
