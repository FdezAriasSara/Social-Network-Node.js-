package com.uniovi.sdipractica134.pageobjects;

import com.uniovi.sdipractica134.util.SeleniumUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import java.util.List;

public class PO_LogsView extends PO_NavView {



    public static void goToLogsPage(WebDriver driver){
        driver.get("localhost:8090/logs/list");
    }


    public static List<WebElement> getLogListedInPosition(WebDriver driver, String typeLog, int numberLog){


        //Selecciona en el desplegable el tipo de log para filtrar
        WebElement elements = driver.findElement(By.id("typesCombo"));
        Select optionSelect = new Select(elements);
        optionSelect.selectByValue(typeLog);

        driver.findElement(By.id("updateButton")).click();

        return  SeleniumUtils.waitLoadElementsByXpath(driver,
                "//*[@id=\"tableLogs\"]/table/tbody/tr[" + numberLog + "]/td[2]",500);

    }

    public static void generateBatchLogs(WebDriver driver){

        //Se generan, al menos, dos PET (ir a /login e ir a /login después de logout),
        //un LOGIN_EX y un LOGOUT.


        PO_LoginView.goToLoginPage(driver);
        PO_LoginView.fillForm(driver,"admin@email.com","admin");
        PO_NavView.clickLogout(driver);

        //Se generan dos LOGIN_ERR
        PO_LoginView.fillForm(driver,"wrong@email.com","admin");
        PO_LoginView.fillForm(driver,"wrong@email.com","admin");

        //Se generan otro LOGIN_EX y otro LOGOUT
        PO_LoginView.fillForm(driver,"admin@email.com","admin");
        PO_NavView.clickLogout(driver);

        //Se genera un PET, un ALTA y otro LOGOUT
        PO_SignUpView.goToSignUpPage(driver);
        PO_SignUpView.fillForm(driver,"random@email.com","Martin",
                "beltran","password", "password");
        PO_NavView.clickLogout(driver);

        //Se genera un PET, un ALTA y otro LOGOUT
        PO_SignUpView.goToSignUpPage(driver);
        PO_SignUpView.fillForm(driver,"martin2@email.com","Martin",
                "beltran","password", "password");
        PO_NavView.clickLogout(driver);



        //Se logea el admin para ver los logs
        PO_LoginView.goToLoginPage(driver);
        PO_LoginView.fillForm(driver,"admin@email.com","admin");

        //Log más reciente debe ser de tipo
        PO_LogsView.goToLogsPage(driver);

    }


    public static void deleteFirstLog(WebDriver driver){

        List<WebElement> buttonsEliminar  =  driver.findElements(By.id("delete"));

        try{
            buttonsEliminar.get(0).click();
        }catch (Exception e){
            //Para evitar que, en caso de no haber LOGS, salte una nullPointer
        }

    }
}
