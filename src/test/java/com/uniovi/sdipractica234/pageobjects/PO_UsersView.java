package com.uniovi.sdipractica234.pageobjects;

import org.openqa.selenium.WebDriver;

public class PO_UsersView extends PO_NavView{
    public static void goToUsersList(WebDriver driver){
        driver.get("localhost:8090/users/list");

    }
}
