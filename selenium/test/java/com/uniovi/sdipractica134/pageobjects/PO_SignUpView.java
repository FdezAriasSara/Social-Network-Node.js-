3package com.uniovi.sdipractica134.pageobjects;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class PO_SignUpView extends PO_NavView {

    static public void fillForm(WebDriver driver, String usernamep, String namep,
                                String surnamep, String passwordp, String passwordconfp) {
        WebElement email = driver.findElement(By.name("email"));
        email.click();
        email.clear();
        email.sendKeys(usernamep);
        WebElement name = driver.findElement(By.name("nombre"));
        name.click();
        name.clear();
        name.sendKeys(namep);
        WebElement lastname = driver.findElement(By.name("apellidos"));
        lastname.click();
        lastname.clear();
        lastname.sendKeys(surnamep);
        WebElement password = driver.findElement(By.name("contraseña"));
        password.click();
        password.clear();
        password.sendKeys(passwordp);
        WebElement passwordConfirm = driver.findElement(By.name("repContra"));
        passwordConfirm.click();
        passwordConfirm.clear();
        passwordConfirm.sendKeys(passwordconfp);
        //Pulsar el boton de Alta.
        By boton = By.className("btn");
        driver.findElement(boton).click();
    }


    public static void goToSignUpPage(WebDriver driver){
        driver.get("localhost:8090/signup");
    }


}
