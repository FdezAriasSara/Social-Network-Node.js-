package com.uniovi.sdipractica234.pageobjects;

import com.uniovi.sdipractica234.util.SeleniumUtils;
import org.junit.jupiter.api.Assertions;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

public class PO_ListPostsView  extends PO_View{
    /**
     * Método que comprueba que el número de post listado sea el esperado.
     * @param driver
     * @param expected
     */
    static public void checkPosts(WebDriver driver,int expected) {
        List<WebElement> postList = SeleniumUtils.waitLoadElementsBy(driver, "free", "//div//div[1]//div[1]//div//", PO_View.getTimeout());
        Assertions.assertEquals(6, postList.size());
    }
    public static void goToPostsListView(WebDriver driver) {
        driver.get("localhost:8090/post/list");

    }
}
