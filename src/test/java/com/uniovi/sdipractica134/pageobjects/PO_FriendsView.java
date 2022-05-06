package com.uniovi.sdipractica134.pageobjects;

import org.openqa.selenium.WebDriver;

public class PO_FriendsView extends PO_View{


    public static void goToListFriendsInvitations(WebDriver driver){
        driver.get("localhost:8090/friends/invites");
    }
    public static void goToListFriends(WebDriver driver){

        driver.get("localhost:8090/friends/list");
    }


}
