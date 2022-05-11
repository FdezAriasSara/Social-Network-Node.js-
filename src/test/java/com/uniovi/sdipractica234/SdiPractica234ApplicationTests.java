package com.uniovi.sdipractica234;


import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.uniovi.sdipractica234.pageobjects.*;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;


import java.util.List;

import static com.mongodb.client.model.Filters.eq;


class SdiPractica234ApplicationTests {


    //static String PathFirefox = "C:\\Program Files\\Mozilla Firefox\\firefox.exe";
    //static String Geckodriver = "C:\\Path\\geckodriver-v0.30.0-win64.exe";
    //  static String Geckodriver = "C:\\Users\\usuario\\Desktop\\Eii\\AÑO 3 GRADO INGENIERIA INFORMATICA\\Sistemas Distribuidos e Internet\\Lab\\sesion05\\PL-SDI-Sesión5-material\\geckodriver-v0.30.0-win64.exe";
    //sebas
    //static String Geckodriver ="C:\\Users\\sebas\\Downloads\\TERCERO\\SEGUNDO CUATRIMESTRE\\SDI\\PL-SDI-Sesión5-material\\PL-SDI-Sesión5-material\\geckodriver-v0.30.0-win64.exe";

    //ce
    //static String PathFirefox = "C:\\Program Files\\Mozilla Firefox\\firefox.exe";
    // static String Geckodriver = "E:\\UNIOVI\\TERCERO\\Segundo cuatri\\SDI\\PL-SDI-Sesión5-material\\geckodriver-v0.30.0-win64.exe"; //CASA
    //static String Geckodriver = "C:\\Users\\Sara\\Desktop\\Universidad\\3-tercer curso\\segundo cuatri\\(SDI)-Sistemas Distribuidos e Internet\\Sesión5-material\\geckodriver-v0.30.0-win64.exe";

    /* SARA */
    static String PathFirefox = "C:\\Program Files\\Mozilla Firefox\\firefox.exe";
    //static String Geckodriver = "C:\\Path\\geckodriver-v0.30.0-win64.exe";
    static String Geckodriver = "C:\\Users\\Sara\\Desktop\\Universidad\\3-tercer curso\\segundo cuatri\\(SDI)-Sistemas Distribuidos e Internet\\Sesión5-material\\geckodriver-v0.30.0-win64.exe";

    //static String PathFirefox = "/Applications/Firefox.app/Contents/MacOS/firefox-bin";
    // static String Geckodriver = "/Users/USUARIO/selenium/geckodriver-v0.30.0-macos";


    static WebDriver driver = getDriver(PathFirefox, Geckodriver);
    static String URL = "http://localhost:8090";
    private String signUpURL = "http://localhost:8090/users/signup";
    static MongoClient mongoClient = MongoClients.create("mongodb+srv://admin:admin@cluster0.6uext.mongodb.net/myFirstDatabase?retryWrites=true&w=majority");

    static MongoDatabase db;
    static MongoCollection<Document> usersCollection;
    static MongoCollection<Document> publiCollection;
    static MongoCollection<Document> msgsCollection;


    //Común a Windows y a MACOSX
    public static WebDriver getDriver(String PathFirefox, String Geckodriver) {
        System.setProperty("webdriver.firefox.bin", PathFirefox);
        System.setProperty("webdriver.gecko.driver", Geckodriver);
        driver = new FirefoxDriver();
        return driver;
    }

    @BeforeEach
    public void setUp() {

         driver.navigate().to(URL);
    } //Después de cada prueba se borran las cookies del navegador

    @AfterEach
    public void tearDown() {
        driver.manage().deleteAllCookies();

    }

    //Antes de la primera prueba
    @BeforeAll
    static public void begin() {
        try {
            db = mongoClient.getDatabase("redsocial");
            usersCollection = db.getCollection("users");
            publiCollection = db.getCollection("publications");
            msgsCollection = db.getCollection("messages");
        } catch (MongoException e) {
            throw e;
        }
    }

    //Al finalizar la última prueba
    @AfterAll
    static public void end() {
        Bson query = eq("name", "test");
        var deleteUsers = usersCollection.deleteMany(query);
        var deletePublications = usersCollection.deleteMany(query);
        var deleteMessages = usersCollection.deleteMany(query);
        mongoClient.close();
        driver.quit();
    }


    //[Prueba1-1] Registro de Usuario con datos válidos.
    @Test
    @Order(1)
    void PR01() {
        PO_SignUpView.goToSignUpPage(driver);
        long usersBefore = usersCollection.countDocuments();//Number of users prior to sign up process.

        PO_SignUpView.goToSignUpPage(driver);
        PO_SignUpView.fillForm(driver, "sara@email.com", "Sara", "Fernández Arias",
                "password", "password");
        Document user = usersCollection.find(eq("email", "sara@email.com")).first();
        Assertions.assertTrue(user != null);//check that the user can be found by  his/her email
        Assertions.assertTrue(usersBefore < usersCollection.countDocuments());//Check that the number of documents increases after registering.
        //now we remove the document from the database.
        usersCollection.deleteOne(eq("email", "sara@email.com"));

    }


    //[Prueba1-2] Registro de Usuario con datos inválidos (username vacío, nombre vacío, apellidos vacíos).
    @Test
    @Order(2)
    void PR02() {
        PO_SignUpView.goToSignUpPage(driver);
        long usersBefore = usersCollection.countDocuments(); //Number of users prior to sign up process.

        PO_SignUpView.goToSignUpPage(driver);
        PO_SignUpView.fillForm(driver, "", "", "",
                "password", "password");

        //The register will not take place since all fields are required.
        Assertions.assertTrue(usersBefore == usersCollection.countDocuments());//Check that the number of documents REMAINS EQUAL
        //Se that we are still in sign up view.

        String welcomeExpected = "¡Regístrate como usuario!";
        String welcomeFound = driver.findElement(By.tagName("h2")).getText();
        Assertions.assertTrue(welcomeFound != null);
        Assertions.assertEquals(welcomeExpected, welcomeFound);

    }


    //[Prueba1-3] Registro de Usuario con datos inválidos (repetición de contraseña inválida).
    @Test
    @Order(3)
    public void PR03() {
        PO_SignUpView.goToSignUpPage(driver);
        long usersBefore = usersCollection.countDocuments(); //Number of users prior to sign up process.
        PO_SignUpView.goToSignUpPage(driver);
        PO_SignUpView.fillForm(driver, "sara@email.com", "Sara", "Fernández",
                "pass", "pass123");
        String alert = "Las contraseñas no coinciden.";
        String found = driver.findElement(By.className("alert")).getText();
        Assertions.assertTrue(found != null);
        Assertions.assertEquals(alert, found);//to make the test fail in case the verbose alert is not displayed
        Assertions.assertTrue(usersCollection.countDocuments() == usersBefore);//Check that the number of documents remains equals


    }

    //[Prueba1-4] Registro de Usuario con datos inválidos (email existente).
    @Test
    @Order(4)
    public void PR04() {

        PO_SignUpView.goToSignUpPage(driver);
        long usersBefore = usersCollection.countDocuments(); //Number of users prior to sign up process.
        PO_SignUpView.goToSignUpPage(driver);
        PO_SignUpView.fillForm(driver, "user01@email.com", "Sara", "Fernández",
                "pass", "pass");
        //check that the alert message is displayed.
        String alert = "Ya existe un usuario con ese correo electrónico.";
        String found = driver.findElement(By.className("alert")).getText();
        Assertions.assertTrue(found != null);
        Assertions.assertEquals(alert, found);//to make the test fail in case the verbose alert is not displayed
        Assertions.assertTrue(usersCollection.countDocuments() == usersBefore);
    }

    //[Prueba EXTRA] Registro de Usuario con datos inválidos (el email tiene formato inválido)
    @Test
    @Order(5)
    public void PR01_1() {

        PO_SignUpView.goToSignUpPage(driver);
        long usersBefore = usersCollection.countDocuments(); //Number of users prior to sign up process.
        PO_SignUpView.goToSignUpPage(driver);
        PO_SignUpView.fillForm(driver, "emailInvalido@email", "Sara", "Fernández",
                "pass", "pass");
        //check that the alert message is displayed.
        String alert = "El formato del email es incorrecto. Debe ser parecido al siguiente: nombre@dominio.dominio";
        String found = driver.findElement(By.className("alert")).getText();
        Assertions.assertTrue(found != null);
        Assertions.assertEquals(alert, found);//to make the test fail in case the verbose alert is not displayed
        Assertions.assertTrue(usersCollection.countDocuments() == usersBefore);
    }


    //[Prueba2-1] Inicio de sesión con datos válidos (administrador).
    @Test
    @Order(5)
    public void PR05() {

        PO_LoginView.goToLoginPage(driver);
        PO_LoginView.fillForm(driver, "admin@email.com", "admin");

        //check that the logout option is displayed.
        WebElement logoutButton = driver.findElement(By.id("logout"));
        Assertions.assertTrue(logoutButton != null);
        //check that publication options are not displayed.(looking for them should throw an exception)
        Exception thrown = Assertions.assertThrows(NoSuchElementException.class, () -> driver.findElement(By.id("listOwnPosts")));
        Assertions.assertEquals("Unable to locate element: #listOwnPosts", thrown.getMessage().split("\n")[0]);
        thrown = Assertions.assertThrows(NoSuchElementException.class, () -> driver.findElement(By.id("addPost")));
        Assertions.assertEquals("Unable to locate element: #addPost", thrown.getMessage().split("\n")[0]);
        //check that the welcome message of the listing page is displayed.
        String welcomeExpected = "Bienvenido, admin@email.com";
        String welcomeFound = driver.findElement(By.tagName("h1")).getText();
        Assertions.assertTrue(welcomeFound != null);
        Assertions.assertEquals(welcomeExpected, welcomeFound);

    }


    //[Prueba2-2] Inicio de sesión con datos válidos (usuario estándar).
    @Test
    @Order(6)
    public void PR06() {


        PO_LoginView.goToLoginPage(driver);
        PO_LoginView.fillForm(driver, "user01@email.com", "user01");


        //When login is successfull , standard users can list post, add post , list friends or logout.
        WebElement logoutButton = driver.findElement(By.id("logout"));
        Assertions.assertTrue(logoutButton != null);
        WebElement listPostsBtn = driver.findElement(By.id("listOwnPosts"));
        Assertions.assertTrue(listPostsBtn != null);
        WebElement addPostBtn = driver.findElement(By.id("addPost"));
        Assertions.assertTrue(addPostBtn != null);
        WebElement listUsersBtn = driver.findElement(By.id("listUsers"));
        Assertions.assertTrue(listUsersBtn != null);

        //The user is redirected to the list view, so he /She should witness the welcome message:

        String welcomeExpected = "Bienvenido, user01@email.com";
        String welcomeFound = driver.findElement(By.tagName("h1")).getText();
        Assertions.assertTrue(welcomeFound != null);
        Assertions.assertEquals(welcomeExpected, welcomeFound);


    }

    //[Prueba2-3] Inicio de sesión con datos inválidos (usuario estándar, campo email y contraseña vacíos).
    //Al ser campos marcados como required, no se puede avanzar
    @Test
    @Order(7)
    public void PR07() {

        PO_LoginView.goToLoginPage(driver);
        PO_LoginView.fillForm(driver, "", "");
        //logout buton not available.
        Exception thrown = Assertions.assertThrows(NoSuchElementException.class, () -> driver.findElement(By.id("logout")));
        Assertions.assertEquals("Unable to locate element: #logout", thrown.getMessage().split("\n")[0]);
        //sign up & login are available still
        WebElement signupButton = driver.findElement(By.id("signup"));
        Assertions.assertTrue(signupButton != null);
        WebElement loginButton = driver.findElement(By.id("login"));
        Assertions.assertTrue(loginButton != null);
        //The register message is displayed still because user hasnt moved from that page.
        String welcomeExpected = "Inicia sesión en Facecook";
        String welcomeFound = driver.findElement(By.tagName("h2")).getText();
        Assertions.assertTrue(welcomeFound != null);
        Assertions.assertEquals(welcomeExpected, welcomeFound);
    }


    //[Prueba2-4] Inicio de sesión con datos válidos (usuario estándar, email existente, pero contraseña incorrecta).
    @Test
    @Order(8)
    public void PR08() {

        PO_LoginView.goToLoginPage(driver);
        PO_LoginView.fillForm(driver, "user01@email.com", "nopassword");
        String alertExp = "Email o password incorrecto";
        String alertFound = driver.findElement(By.className("alert")).getText();
        Assertions.assertTrue(alertFound != null);
        Assertions.assertEquals(alertExp, alertFound);
        //The register message is displayed still because user hasnt moved from that page.
        String welcomeExpected = "Inicia sesión en Facecook";
        String welcomeFound = driver.findElement(By.tagName("h2")).getText();
        Assertions.assertTrue(welcomeFound != null);
        Assertions.assertEquals(welcomeExpected, welcomeFound);
        //sign up & login are available still
        WebElement signupButton = driver.findElement(By.id("signup"));
        Assertions.assertTrue(signupButton != null);
        WebElement loginButton = driver.findElement(By.id("login"));
        Assertions.assertTrue(loginButton != null);
        //check that publication options are not displayed.(looking for them should throw an exception)
        Exception thrown = Assertions.assertThrows(NoSuchElementException.class, () -> driver.findElement(By.id("listOwnPosts")));
        Assertions.assertEquals("Unable to locate element: #listOwnPosts", thrown.getMessage().split("\n")[0]);
        thrown = Assertions.assertThrows(NoSuchElementException.class, () -> driver.findElement(By.id("addPost")));
        Assertions.assertEquals("Unable to locate element: #addPost", thrown.getMessage().split("\n")[0]);
        thrown = Assertions.assertThrows(NoSuchElementException.class, () -> driver.findElement(By.id("listUsers")));
        Assertions.assertEquals("Unable to locate element: #listUsers", thrown.getMessage().split("\n")[0]);

    }


    //[Prueba3-1] Hacer clic en la opción de salir de sesión
    // y comprobar que se redirige a la página de inicio de sesión (Login).
    @Test
    @Order(9)
    public void PR09() {

        PO_LoginView.goToLoginPage(driver);
        PO_LoginView.fillForm(driver, "user01@email.com", "user01");
        PO_NavView.clickLogout(driver);

        //The login message is displayed due to redirection
        String welcomeExpected = "Inicia sesión en Facecook";
        String welcomeFound = driver.findElement(By.tagName("h2")).getText();
        Assertions.assertTrue(welcomeFound != null);
        Assertions.assertEquals(welcomeExpected, welcomeFound);
        //check that publication options are not displayed.(looking for them should throw an exception)
        Exception thrown = Assertions.assertThrows(NoSuchElementException.class, () -> driver.findElement(By.id("listOwnPosts")));
        Assertions.assertEquals("Unable to locate element: #listOwnPosts", thrown.getMessage().split("\n")[0]);
        thrown = Assertions.assertThrows(NoSuchElementException.class, () -> driver.findElement(By.id("addPost")));
        Assertions.assertEquals("Unable to locate element: #addPost", thrown.getMessage().split("\n")[0]);
        thrown = Assertions.assertThrows(NoSuchElementException.class, () -> driver.findElement(By.id("listUsers")));
        Assertions.assertEquals("Unable to locate element: #listUsers", thrown.getMessage().split("\n")[0]);
        //logout option should not be displayed either
        thrown = Assertions.assertThrows(NoSuchElementException.class, () -> driver.findElement(By.id("logout")));
        Assertions.assertEquals("Unable to locate element: #logout", thrown.getMessage().split("\n")[0]);
        //sign up & login are available
        WebElement signupButton = driver.findElement(By.id("signup"));
        Assertions.assertTrue(signupButton != null);
        WebElement loginButton = driver.findElement(By.id("login"));
        Assertions.assertTrue(loginButton != null);
    }


    //[Prueba3-2] Comprobar que el botón cerrar sesión no está visible si el usuario no está autenticado
    @Test
    @Order(10)
    public void PR010() {

        Exception thrown = Assertions.assertThrows(NoSuchElementException.class, () -> driver.findElement(By.id("logout")));
        Assertions.assertEquals("Unable to locate element: #logout", thrown.getMessage().split("\n")[0]);

        //sign up & login are available
        WebElement signupButton = driver.findElement(By.id("signup"));
        Assertions.assertTrue(signupButton != null);
        WebElement loginButton = driver.findElement(By.id("login"));
        Assertions.assertTrue(loginButton != null);

    }


    //[Prueba24] Ir al formulario de crear publicaciones , rellenarlo con datos VÁLIDOS y pulsar el botón de enviar.
    @Test
    @Order(17)
    public void PR24() {

        //The user must be registered in order to post
        PO_LoginView.goToLoginPage(driver);
        PO_LoginView.fillForm(driver, "user01@email.com", "user01");
        //fill the form
        PO_PostFormView.goToPostFormView(driver);
        PO_PostFormView.fillForm(driver, "Días de vacaciones", "Me lo he pasado genial en málaga! :)");

        //go to the last page.
        List<WebElement> elements = PO_View.checkElementBy(driver, "free", "//a[contains(@class, 'page-link')]");

        elements.get(2).click();
        //search for the publication we just did.
        elements = PO_View.checkElementBy(driver, "text", "Días de vacaciones");

        Assertions.assertEquals("Días de vacaciones", elements.get(0).getText());

        publiCollection.deleteOne(eq("title", "Días de vacaciones"));
    }

    //[Prueba25] Ir al formulario de crear publicaciones , rellenarlo con datos INVÁLIDOS (título vacío) y pulsar el botón de enviar.
    @Test
    @Order(18)
    public void PR25() {

        PO_LoginView.goToLoginPage(driver);
        //The user must be registered in order to post
        PO_LoginView.fillForm(driver, "user01@email.com", "user01");
        //fill the form
        PO_PostFormView.goToPostFormView(driver);
        PO_PostFormView.fillForm(driver, "", "Me lo he pasado genial en málaga! :)");


        String welcomeExpected = "Añade una publicación";
        String welcomeFound = driver.findElement(By.tagName("h2")).getText();
        Assertions.assertTrue(welcomeFound != null);
        Assertions.assertEquals(welcomeExpected, welcomeFound);
        //Check that we remain on the view we were at.
    }


    //[Prueba26]Mostrar el listado de publicaciones de un usuario y comprobar que se muestran todas las que existen para dicho usuario.
    @Test
    @Order(22)
    public void PR26() {
        PO_LoginView.goToLoginPage(driver);
        //The user must be in session in order to make a post
        PO_LoginView.fillForm(driver, "user11@email.com", "user11");
        //Once logged in, he can access his own posts

        PO_NavView.clickListPosts(driver);
        //We check that two pages are displayed,each with 5 posts
        PO_ListPostsView.checkPosts(driver, 5);//primera página.
        List<WebElement> elements = PO_View.checkElementBy(driver, "free", "//a[contains(@class, 'page-link')]");
        //Nos vamos a la última página
        elements.get(1).click();
        PO_ListPostsView.checkPosts(driver, 5);//segunda página.
    }


    //[PRUEBA 27]Mostrar el listado de publicaciones de un usuario amigo y comprobar que se muestran todas las que existen para dicho usuario.
    @Test
    @Order(24)
    public void PR27() {
        PO_LoginView.goToLoginPage(driver);
        //El usuario debe estar registrado para hacer un post , por tanto
        PO_LoginView.fillForm(driver, "user02@email.com", "user02");
        //El usuario 01 es amigo del usuario 3, que tiene 10 publicaciones.
        PO_FriendsView.goToListFriends(driver);
        By enlace = By.id("publicationsButtonuser03@email.com");
        driver.findElement(enlace).click();
        String welcomeExpected = "Publicaciones de user03@email.com";
        String welcomeFound = driver.findElement(By.tagName("h1")).getText();
        Assertions.assertTrue(welcomeFound != null);
        Assertions.assertEquals(welcomeExpected, welcomeFound);

        //We check that two pages are displayed,each with 5 posts
        PO_ListPostsView.checkPosts(driver, 5);//primera página.
        List<WebElement> elements = PO_View.checkElementBy(driver, "free", "//a[contains(@class, 'page-link')]");
        //Nos vamos a la última página
        elements.get(1).click();
        PO_ListPostsView.checkPosts(driver, 5);//segunda página.

    }

    //[PRUEBA 28]Utilizando un acceso vía URL u otra alternativa, tratar de listar las publicaciones de un usuario que no sea amigo del usuario identificado en sesión. Comprobar que el sistema da un error de autorización.
    @Test
    @Order(25)
    public void PR28() {
        PO_LoginView.goToLoginPage(driver);
        //El usuario debe estar registrado para hacer un post , por tanto
        PO_LoginView.fillForm(driver, "user02@email.com", "user02");
        //El usuario 02 NO es amigo del usuario 12, que tiene 10 publicaciones.
        driver.get("http://localhost:8090/posts/listFor/user12@email.com");
//        List<WebElement> forbiddenMessage= PO_View.checkElementBy(driver, "text", PO_View.getP().getString("error.message",PO_Properties.getSPANISH()));
        //Assertions.assertEquals("Parece que este sitio no existe o no tienes acceso a él :(",forbiddenMessage.get(0).getText());
    }


    //[Prueba29] Intentar acceder sin estar autenticado a la opción de
    //listado de usuarios. Se deberá volver al formulario de login.
    @Test
    @Order(27)
    void PR029() {

        PO_UsersView.goToUsersList(driver);
        //check we are redirected to the login page.
        String welcomeExpected = "Inicia sesión en Facecook";
        String welcomeFound = driver.findElement(By.tagName("h2")).getText();
        Assertions.assertTrue(welcomeFound != null);
        Assertions.assertEquals(welcomeExpected, welcomeFound);


    }

    //[Prueba30] Intentar acceder sin estar autenticado a la opción de listado de invitaciones de amistad
    // recibida de un usuario estándar. Se deberá volver al formulario de login
    @Test
    @Order(28)
    void PR030() {

        PO_FriendsView.goToListFriendsInvitations(driver);
        //check we are redirected to the login page.
        String welcomeExpected = "Inicia sesión en Facecook";
        String welcomeFound = driver.findElement(By.tagName("h2")).getText();
        Assertions.assertTrue(welcomeFound != null);
        Assertions.assertEquals(welcomeExpected, welcomeFound);
    }



}

