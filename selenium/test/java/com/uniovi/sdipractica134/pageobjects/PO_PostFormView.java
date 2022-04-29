package com.uniovi.sdipractica134.pageobjects;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class PO_PostFormView {
    static public void fillForm(WebDriver driver, String title, String description) {
        goToPostFormView(driver);
        //Write in the title
        WebElement titleElement = driver.findElement(By.id("title"));
        titleElement.click();
        titleElement.clear();
        titleElement.sendKeys(title);

        //Write in the description
        WebElement descriptionElement = driver.findElement(By.id("description"));
        descriptionElement .click();
        descriptionElement .clear();
        descriptionElement .sendKeys(description);

        //Send the form to publish the post.
        By boton = By.className("btn");
        driver.findElement(boton).click();
    }

    public static void goToPostFormView(WebDriver driver) {
        driver.get("localhost:8090/post/add");

    }
}
