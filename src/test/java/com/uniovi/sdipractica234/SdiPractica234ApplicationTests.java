package com.uniovi.sdipractica234;


import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

import com.mongodb.client.model.Updates;
import com.uniovi.sdipractica234.pageobjects.*;
import com.uniovi.sdipractica234.util.SeleniumUtils;
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


import java.util.LinkedList;
import java.util.List;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Sorts.ascending;

import static com.mongodb.client.model.Filters.eq;


class SdiPractica234ApplicationTests {


    public static final int USERS_PER_PAGE = 5;
    //static String PathFirefox = "C:\\Program Files\\Mozilla Firefox\\firefox.exe";
    //static String Geckodriver = "C:\\Path\\geckodriver-v0.30.0-win64.exe";
  //  static String Geckodriver = "C:\\Users\\usuario\\Desktop\\Eii\\AÑO 3 GRADO INGENIERIA INFORMATICA\\Sistemas Distribuidos e Internet\\Lab\\sesion05\\PL-SDI-Sesión5-material\\geckodriver-v0.30.0-win64.exe";
    //sebas
    static String Geckodriver ="C:\\Users\\sebas\\Downloads\\TERCERO\\SEGUNDO CUATRIMESTRE\\SDI\\PL-SDI-Sesión5-material\\PL-SDI-Sesión5-material\\geckodriver-v0.30.0-win64.exe";

    //ce
    //static String PathFirefox = "C:\\Program Files\\Mozilla Firefox\\firefox.exe";

    //static String Geckodriver = "E:\\UNIOVI\\TERCERO\\Segundo cuatri\\SDI\\PL-SDI-Sesión5-material\\geckodriver-v0.30.0-win64.exe"; //CASA
    //static String Geckodriver = "C:\\Users\\Sara\\Desktop\\Universidad\\3-tercer curso\\segundo cuatri\\(SDI)-Sistemas Distribuidos e Internet\\Sesión5-material\\geckodriver-v0.30.0-win64.exe";

    /* SARA */
    static String PathFirefox = "C:\\Program Files\\Mozilla Firefox\\firefox.exe";
    //static String Geckodriver = "C:\\Path\\geckodriver-v0.30.0-win64.exe";
    //static String Geckodriver = "C:\\Users\\Sara\\Desktop\\Universidad\\3-tercer curso\\segundo cuatri\\(SDI)-Sistemas Distribuidos e Internet\\Sesión5-material\\geckodriver-v0.30.0-win64.exe";

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


    //Prueba[11] Mostrar el listado de usuarios y comprobar que se muestran todos los que existen en el sistema.
    @Test
    @Order(32)
    public void PR11(){
        loginAs("admin@email.com", "admin");
        PO_UsersView.goToUsersList(driver);
        List<Document> totalUsers = getUsersAdminView();

        List<WebElement> usersInListView = driver.findElements(By.className("username"));

        Assertions.assertTrue( totalUsers.size() == usersInListView.size(),
                "Sizes differ: size of users with ROLE_USER in DB differ with size of users displayed");
        List<String> userNames = new LinkedList<>();
        for (WebElement element:
                usersInListView) {
            userNames.add(element.getText());
        }
        for (Document userDoc:
                totalUsers) {
            Assertions.assertTrue(userNames.contains(userDoc.getString("name")), "Username:"+userDoc.getString("name")+" not present in the view ");
        }


    }

    private List<Document> getUsersAdminView() {
        List<Document> totalUsers = new LinkedList<>();
        usersCollection.find(Filters.not(Filters.exists("role"))).into(totalUsers);
        return totalUsers;
    }

    //Prueba[12] Ir a la lista de usuarios, borrar el primer usuario de la lista, comprobar que la lista se actualiza
    //y dicho usuario desaparece.
    @Test
    @Order(33)
    public void PR12(){
        loginAs("admin@email.com", "admin");
        PO_UsersView.goToUsersList(driver);
        deleteUserInPath("//*[@id=\"tableUsers\"]/tbody/tr[1]/td[4]/input");


    }
    //Prueba[13] Ir a la lista de usuarios, borrar el último usuario de la lista, comprobar que la lista se actualiza
    //y dicho usuario desaparece.
    @Test
    @Order(34)
    public void PR13(){
        loginAs("admin@email.com", "admin");
        PO_UsersView.goToUsersList(driver);
        deleteUserInPath("//*[@id=\"tableUsers\"]/tbody/tr[last()]/td[4]/input");

    }

    //Prueba[14] Ir a la lista de usuarios, borrar 3 usuarios, comprobar que la lista se actualiza y dichos usuarios
    //desaparecen.
    @Test
    @Order(35)
    public void PR14(){
        loginAs("admin@email.com", "admin");
        PO_UsersView.goToUsersList(driver);
        List<Document> totalUsers = getUsersAdminView();//usersBefore
        //Cogemos los elementos a borrar. En este test van a ser 3, el primero,segundo y penúltimo de la lista:
        WebElement firstUserEl = driver.findElement(By.xpath("//*[@id=\"tableUsers\"]/tbody/tr[1]/td[4]/input"));
        WebElement secondUserEl =driver.findElement(By.xpath("//*[@id=\"tableUsers\"]/tbody/tr[2]/td[4]/input"));
        WebElement thirdUserEl = driver.findElement(By.xpath("//*[@id=\"tableUsers\"]/tbody/tr[last()-1]/td[4]/input"));
        //Generamos los IDs en formato ObjectId
        ObjectId idFirstUser = new ObjectId(firstUserEl.getAttribute("id"));
        ObjectId idSecondUser = new ObjectId( secondUserEl.getAttribute("id"));
        ObjectId idThirdUser = new ObjectId(thirdUserEl.getAttribute("id"));
        //Obtenemos los documentos que serán borrados para restaurarlos al final del test
        Document firstDeleted = getUser(idFirstUser);
        Document secondDeleted = getUser(idSecondUser);
        Document thirdDeleted = getUser(idThirdUser);
        //Obtenemos datos relacionados con los documentos anteriores, que por el hecho de estar asociados
        // van a ser borrados. Como van a ser eliminados, habrá que restaurarlos al final del test.
        //Primer usuario
        List<Document> messagesInvolvingFirst = getMessagesInvolvingUser(firstDeleted);
        List<Document> friendsInvitesInvolvingFirst = getUsersFriendsInvitesRelated2DeletedUser(idFirstUser);
        List<Document> publicationsInvolvingFirst = getPublicationsOf(idFirstUser);
        //Segundo usuario
        List<Document> messagesInvolvingSecond = getMessagesInvolvingUser(secondDeleted);
        List<Document> friendsInvitesInvolvingSecond = getUsersFriendsInvitesRelated2DeletedUser(idSecondUser);
        List<Document> publicationsInvolvingSecond = getPublicationsOf(idSecondUser);
        //Tercer usuario
        List<Document> messagesInvolvingThird = getMessagesInvolvingUser(thirdDeleted);
        List<Document> friendsInvitesInvolvingThird = getUsersFriendsInvitesRelated2DeletedUser(idThirdUser);
        List<Document> publicationsInvolvingThird = getPublicationsOf(idThirdUser);
        //Select the checkboxes of three users
        firstUserEl.click();
        secondUserEl.click();
        thirdUserEl.click();
        driver.findElement(By.id("deleteButton")).click(); //Press delete button and delete users.
        SeleniumUtils.waitSeconds(driver, 1); //wait a second in order for the database to update
        List<Document> usersAfterDeleting = getUsersAdminView();
        Assertions.assertTrue(totalUsers.size() == usersAfterDeleting.size()+3);
        Assertions.assertTrue(!usersAfterDeleting.contains(firstDeleted), "User with id: "+ idFirstUser+" was not deleted");
        Assertions.assertTrue(!usersAfterDeleting.contains(secondDeleted), "User with id: "+ idFirstUser+" was not deleted");
        Assertions.assertTrue(!usersAfterDeleting.contains(thirdDeleted), "User with id: "+ idFirstUser+" was not deleted");
        //volvemos a añadir los usuarios eliminados así como los datos relacionados con los mismos
        //(amigos, publicaciones, mensajes.....)
        restoreUser(firstDeleted);
        restoreFriendsAndInvites(friendsInvitesInvolvingFirst);
        restorePublications(publicationsInvolvingFirst);
        restoreMessages(messagesInvolvingFirst);

        restoreUser(secondDeleted);
        restoreFriendsAndInvites(friendsInvitesInvolvingSecond);
        restorePublications(publicationsInvolvingSecond);
        restoreMessages(messagesInvolvingSecond);

        restoreUser(thirdDeleted);
        restoreFriendsAndInvites(friendsInvitesInvolvingThird);
        restorePublications(publicationsInvolvingThird);
        restoreMessages(messagesInvolvingThird);
    }

    private void restoreMessages(List<Document> messages) {
        for (Document message:
                messages) {
            msgsCollection.insertOne(message);
        }
    }

    private void restorePublications(List<Document> publications) {
        for (Document publication:
                publications) {
            publiCollection.insertOne(publication);
        }
    }

    private void restoreFriendsAndInvites(List<Document> friendsInvitesInvolvingUser) {
        for (Document user :
                friendsInvitesInvolvingUser) {
            String _userId = user.get("_id").toString();
            usersCollection.replaceOne(eq("_id", new ObjectId(_userId)),
                    user);
        }
    }


    private void restoreUser(Document userDeleted) {
        usersCollection.insertOne(userDeleted);
    }

    private Document getUser(ObjectId id){
       Document user = usersCollection.find(eq("_id", id)).first();
       return user;
    }
    private void loginAs(String username, String password){
        PO_LoginView.goToLoginPage(driver);
        PO_LoginView.fillForm(driver,username,password);
    }


    private void deleteUserInPath(String xPath){


        List<Document> totalUsers = getUsersAdminView();

        WebElement element = driver.findElement(By.xpath(xPath)); //Eliminaremos el usuario del path
        String userId = element.getAttribute("id");
        ObjectId _userDeletedId= new ObjectId(userId);
        Document userDeleted = usersCollection.find(eq("_id", _userDeletedId)).first();
        List<Document> publicationsDeleted = getPublicationsOf(_userDeletedId);
        List<Document> messagesInvolvingDeletedUser = getMessagesInvolvingUser(userDeleted);
        List<Document> usersWithFriendsAndInvitesRelated2DeletedUser = getUsersFriendsInvitesRelated2DeletedUser(_userDeletedId);
        Assertions.assertTrue(totalUsers.contains(userDeleted)); //Chequeo user is present
        element.click();
        driver.findElement(By.id("deleteButton")).click(); //we delete the user

        List<Document> remainingUsers = getUsersAdminView();
        Assertions.assertTrue(!remainingUsers.contains(userDeleted), "User was not deleted");
        Assertions.assertTrue( totalUsers.size() == remainingUsers.size()+1,
                "Sizes differ: seems like user was not deleted");


        //Volvemos a añadir la información eliminada!!!!!!
        restoreUser(userDeleted);
        restoreFriendsAndInvites(usersWithFriendsAndInvitesRelated2DeletedUser);
        restoreMessages(messagesInvolvingDeletedUser);
        restorePublications(publicationsDeleted);


    }

    private List<Document> getUsersFriendsInvitesRelated2DeletedUser(ObjectId _userDeletedId) {
        List<Document> usersWithFriendsAndInvitesRelated2DeletedUser = new LinkedList<>();
        usersCollection.find(Filters.or(
                eq("friendships", _userDeletedId),
                eq("invitesSent", _userDeletedId),
                eq("invitesReceived", _userDeletedId)
        )).into(usersWithFriendsAndInvitesRelated2DeletedUser); //Guardamos esto porque tendremos que restaurarlo después
        return usersWithFriendsAndInvitesRelated2DeletedUser;
    }

    private List<Document> getMessagesInvolvingUser(Document userDeleted) {
        List<Document> messagesInvolvingDeletedUser = new LinkedList<>();
        msgsCollection.find(Filters.or(
                eq("senderEmail", userDeleted.get("email")),
                eq("receiverEmail", userDeleted.get("email"))
                )).into(messagesInvolvingDeletedUser);
        return messagesInvolvingDeletedUser;
    }

    private List<Document> getPublicationsOf(ObjectId _userDeletedId) {
        List<Document> publicationsDeleted = new LinkedList<>();
        publiCollection.find(eq("userID", _userDeletedId)).into(publicationsDeleted);
        return publicationsDeleted;
    }

    //Prueba[15] Mostrar el listado de usuarios y comprobar que se muestran todos los que existen en el sistema,
    //excepto el propio usuario y aquellos que sean Administradores
    @Test
    @Order(36)
    public void PR15(){
        //Login como user01 y nos vamos a la vista de listar usuarios
        loginAs("user01@email.com", "user01");
        PO_UsersView.goToUsersList(driver);


        compareUserListViewWithUserListInSystem("user01@email.com");

    }

    private void compareUserListViewWithUserListInSystem(String userEmailThatAsksForList) {
        //Preparar variables para testear: usuario que pedirá la peticion, listado de usernames que se mostrará en la vista
        // además de listado de posibles usernames de administradores.
        Document userThatAskedForList = usersCollection.find(eq("email", userEmailThatAsksForList)).first();
        List<String> adminUsernames = new LinkedList<>();
        for (Document admin:
                usersCollection.find(Filters.exists("role"))) {
            adminUsernames.add(admin.getString("email"));
        }
        List<String> usernamesThatShouldBeInView = new LinkedList<>();
        List<Document> usersInNormalView = new LinkedList<>();
        usersCollection.find(and(
                Filters.not(Filters.exists("role")),
                Filters.ne("email", userEmailThatAsksForList))
                ).sort(ascending("email"))
                .into(usersInNormalView);
        long totalUsers = usersInNormalView.size();
        long totalPages = totalUsers % USERS_PER_PAGE == 0 ? totalUsers/USERS_PER_PAGE : totalUsers/USERS_PER_PAGE +1;
        for (Document u:
               usersInNormalView) {
            usernamesThatShouldBeInView.add(u.getString("email"));
        }


        //Obtenemos todas las páginas que hay
        List<WebElement> pagesInView= driver.findElements(By.className("page-link"));
        List<WebElement> usernamesDisplayed;
        int currentPage = 0;
        while(!pagesInView.isEmpty() && currentPage<totalPages-1){
            usernamesDisplayed = driver.findElements(By.className("user-email"));
            for (WebElement usernameDisplayed:
                    usernamesDisplayed) {
                Assertions.assertTrue( !adminUsernames.contains(usernameDisplayed.getText()));
                Assertions.assertTrue( !userThatAskedForList.getString("email").equals(usernameDisplayed.getText()),
                        "The user that asks for the list is displayed");
                Assertions.assertTrue(usernamesThatShouldBeInView.contains(usernameDisplayed.getText()),
                        "Username: "+ usernameDisplayed + " should not be displayed.");
            }
            currentPage++;
            pagesInView.get(currentPage).click(); //Indexes in Java always start at 0

            pagesInView = driver.findElements(By.className("page-link"));
        }

    }

    //Prueba[16]Hacer una búsqueda con el campo vacío y comprobar que se muestra la página que
    //corresponde con el listado usuarios existentes en el sistema.
    @Test
    @Order(37)
    public void PR16(){
        //Login como user01 y nos vamos a la vista de listar usuarios
        loginAs("user01@email.com", "user01");
        PO_UsersView.goToUsersList(driver);
        driver.findElement(By.id("search")).sendKeys("");
        driver.findElement(By.id("searchButton")).click();
        compareUserListViewWithUserListInSystem("user01@email.com");
    }
    //Prueba[17]Hacer una búsqueda escribiendo en el campo un texto que no exista y comprobar que se
    //muestra la página que corresponde, con la lista de usuarios vacía.
    @Test
    @Order(38)
    public void PR17(){
        //Login como user01 y nos vamos a la vista de listar usuarios
        loginAs("user01@email.com", "user01");
        PO_UsersView.goToUsersList(driver);
        driver.findElement(By.id("search")).sendKeys("¡¡NoExistente!!");
        driver.findElement(By.id("searchButton")).click();
        Assertions.assertTrue(driver.findElements(By.className("username")).isEmpty());
    }

    //Prueba[18]Hacer una búsqueda con un texto específico y comprobar que se muestra la página que
    //corresponde, con la lista de usuarios en los que el texto especificado sea parte de su nombre, apellidos o
    //de su email.
    @Test
    @Order(39)
    public void PR18(){
        //Login como user01 y nos vamos a la vista de listar usuarios
        loginAs("user01@email.com", "user01");
        PO_UsersView.goToUsersList(driver);
        driver.findElement(By.id("search")).sendKeys("2");
        driver.findElement(By.id("searchButton")).click();
        List<WebElement> usernamesDisplayed = driver.findElements(By.className("username")); //cogemos los usernames que aparecen en la vista
        List<String> userNamesObtainedWithSearchBy_2_ = new LinkedList<>();
        userNamesObtainedWithSearchBy_2_.add("Ellie");
        userNamesObtainedWithSearchBy_2_.add("Gala");
        //Por cada username en vista, chequeamos los asertos:
        for (WebElement usernameDisplayed:
                usernamesDisplayed) {
            Assertions.assertTrue(userNamesObtainedWithSearchBy_2_.contains(usernameDisplayed.getText()), "Username: "+ usernameDisplayed.getText() + " should not be displayed!");
        }
    }

    //[Prueba19] Iniciamos sesión, mandamos una invitación de amistad a otro usuario, cerramos sesión y entramos como
    // el otro usuario para comprobar que la nueva invitación aparece en la lista.
    @Test
    @Order(11)
    public void PR19(){
        PO_SignUpView.goToSignUpPage(driver);
        PO_SignUpView.fillForm(driver,"test@email.com","test","Test Friends",
                "password","password");
        PO_LoginView.goToLoginPage(driver);
        PO_LoginView.fillForm(driver,"user02@email.com","user02");

        //get the user id
        WebElement receiver = driver.findElement(By.xpath("//*[@id=\"tableUsers\"]/tbody/tr[2]/td[4]/form"));
        String url = receiver.getAttribute("action");
        ObjectId id = new ObjectId(url.substring(37));

        //send invite
        driver.findElement(By.xpath("//*[@id=\"sendButtontest@email.com\"]")).click();

        //logout
        PO_NavView.clickLogout(driver);

        //login as user01
        PO_LoginView.fillForm(driver,"test@email.com","password");

        //go to invite list
        PO_FriendsView.goToListFriendsInvitations(driver);

        //check that user 02 is there
        String checkText = "Ellie";
        List<WebElement> result = PO_View.checkElementBy(driver, "text", checkText);
        Assertions.assertEquals(checkText, result.get(0).getText());

        //now we remove the user from the database. TODO check it works <3
        usersCollection.deleteOne(eq("email","test@email.com"));
        usersCollection.findOneAndUpdate(eq("email", "user02@email.com"),
                Updates.pull("invitesSent", id));
    }

    //[Prueba20] Iniciamos sesión, mandamos una invitación de amistad a otro usuario, y tratamos de volver a mandar una
    //invitación. No existirá un botón para mandarlo ni deberá dejarnos mandarla desde la url
    @Test
    @Order(12)
    public void PR20(){
        PO_SignUpView.goToSignUpPage(driver);
        PO_SignUpView.fillForm(driver,"test@email.com","test","Test Friends",
                "password","password");
        PO_LoginView.goToLoginPage(driver);
        PO_LoginView.fillForm(driver,"user04@email.com","user04");

        //get the user id
        WebElement receiver = driver.findElement(By.xpath("//*[@id=\"tableUsers\"]/tbody/tr[2]/td[4]/form"));
        String url = receiver.getAttribute("action");
        ObjectId id = new ObjectId(url.substring(37));

        //send invite
        By path = By.xpath("//*[@id=\"sendButtontest@email.com\"]");
        driver.findElement(path).click();

        //ya no hay botón
        try {
            driver.findElement(path);
        } catch (org.openqa.selenium.NoSuchElementException e) {
            Assertions.assertTrue(true); //we cannot find the element
        }
        //intentamos acceder mediante la url y nos lleva a página de error
        driver.get(url);
        Assertions.assertEquals("ERROR:", PO_View.checkElementBy(driver, "text", "ERROR").get(0).getText());

        //now we remove the user from the database. TODO check it works <3
        usersCollection.deleteOne(eq("email","test@email.com"));
        usersCollection.findOneAndUpdate(eq("email", "user04@email.com"),
                Updates.pull("invitesReceived", id));
    }

    //[Prueba21] Iniciamos sesión y mostramos el listado de invitaciones de amistad recibidas
    @Test
    @Order(13)
    public void PR21(){
        PO_LoginView.goToLoginPage(driver);
        PO_LoginView.fillForm(driver,"user05@email.com","user05");

        //go to invite list
        PO_FriendsView.goToListFriendsInvitations(driver);

        //check that all invites inputted are there
        List<WebElement> inviteList = SeleniumUtils.waitLoadElementsBy(driver, "free", "//tbody/tr", PO_View.getTimeout());
        Assertions.assertEquals(3, inviteList.size());
    }

    private void sendTestInvite(boolean create){
        //Mandamos una invitación con un usuario de prueba
        if (create){
            PO_SignUpView.goToSignUpPage(driver);
            PO_SignUpView.fillForm(driver,"test@email.com","test","Test Friends",
                    "password","password");
        }
        PO_LoginView.goToLoginPage(driver);
        PO_LoginView.fillForm(driver,"test@email.com","password");
        driver.findElements(By.className("page-link")).get(1).click();
        driver.findElement(By.xpath("//*[@id=\"sendButtonuser05@email.com\"]")).click();
        //logout
        PO_NavView.clickLogout(driver);
    }

    //[Prueba22] Sobre el listado de invitaciones recibidas. Hacer clic en el botón/enlace de una de ellas y comprobar
    // que dicha solicitud desaparece del listado de invitaciones
    @Test
    @Order(14)
    public void PR22(){
        PO_SignUpView.goToSignUpPage(driver);
        PO_SignUpView.fillForm(driver,"test@email.com","test","Test Friends",
                "password","password");

        PO_LoginView.goToLoginPage(driver);
        PO_LoginView.fillForm(driver,"user05@email.com","user05");

        //get the user id
        WebElement receiver = driver.findElement(By.xpath("//*[@id=\"tableUsers\"]/tbody/tr[2]/td[4]/form"));
        String url = receiver.getAttribute("action");
        ObjectId id = new ObjectId(url.substring(37));

        //logout
        PO_NavView.clickLogout(driver);

        sendTestInvite(false);

        PO_LoginView.goToLoginPage(driver);
        PO_LoginView.fillForm(driver,"user05@email.com","user05");

        //go to invite list
        PO_FriendsView.goToListFriendsInvitations(driver);

        //Número inicial de invitaciones
        List<WebElement> inviteList = SeleniumUtils.waitLoadElementsBy(driver, "free", "//tbody/tr", PO_View.getTimeout());
        Assertions.assertEquals(4, inviteList.size());

        //Aceptamos la de test
        By path = By.xpath("//*[@id=\"acceptButtontest@email.com\"]");
        driver.findElement(path).click();

        //check that it disappears from the invite list
        inviteList = SeleniumUtils.waitLoadElementsBy(driver, "free", "//tbody/tr", PO_View.getTimeout());
        Assertions.assertEquals(3, inviteList.size());

        //now we remove the user from the database. TODO check it works <3
        usersCollection.deleteOne(eq("email","test@email.com"));


        usersCollection.findOneAndUpdate(eq("email", "user05@email.com"),
                Updates.pull("friendships", id));
    }

    //[Prueba22_1] Prueba adicional para comprobar que tras aceptar la invitación, aparece el usuario en el listado de
    // amigos
    @Test
    @Order(15)
    public void PR22_1(){
        PO_SignUpView.goToSignUpPage(driver);
        PO_SignUpView.fillForm(driver,"test@email.com","test","Test Friends",
                "password","password");

        PO_LoginView.goToLoginPage(driver);
        PO_LoginView.fillForm(driver,"user05@email.com","user05");

        //get the user id
        WebElement receiver = driver.findElement(By.xpath("//*[@id=\"tableUsers\"]/tbody/tr[2]/td[4]/form"));
        String url = receiver.getAttribute("action");
        ObjectId id = new ObjectId(url.substring(37));

        //logout
        PO_NavView.clickLogout(driver);

        sendTestInvite(false);

        PO_LoginView.goToLoginPage(driver);
        PO_LoginView.fillForm(driver,"user05@email.com","user05");

        //go to friends list
        PO_FriendsView.goToListFriends(driver);

        //check that user is NOT there (user05 has no friends)
        Assertions.assertEquals("No hay usuarios en tu lista de amigos",
                PO_View.checkElementBy(driver, "text", "No hay usuarios en tu lista de amigos").get(0).getText());

        //go to invite list
        PO_FriendsView.goToListFriendsInvitations(driver);

        //accept test invite
        By path = By.xpath("//*[@id=\"acceptButtontest@email.com\"]");
        driver.findElement(path).click();

        //go to friends list
        PO_FriendsView.goToListFriends(driver);

        //check that test user is now there
        List<WebElement> friendList = SeleniumUtils.waitLoadElementsBy(driver, "free", "//tbody/tr", PO_View.getTimeout());
        Assertions.assertEquals(1, friendList.size());

        //now we remove the user from the database. TODO check it works <3
        usersCollection.deleteOne(eq("email","test@email.com"));
        usersCollection.findOneAndUpdate(eq("email", "user05@email.com"),
                Updates.pull("friendships", id));
    }

    //[Prueba23] Mostrar el listado de amigos de un usuario. Comprobar que el listado contiene los amigos que deben ser
    @Test
    @Order(16)
    public void PR23(){
        PO_LoginView.goToLoginPage(driver);
        PO_LoginView.fillForm(driver,"user02@email.com","user02");

        //go to friends list
        PO_FriendsView.goToListFriends(driver);

        //check correct number of friends
        List<WebElement> friendList = SeleniumUtils.waitLoadElementsBy(driver, "free", "//tbody/tr", PO_View.getTimeout());
        Assertions.assertEquals(2, friendList.size());

        //Check specifically every friend
        String checkText = "Joel-Joseph";
        PO_View.checkElementBy(driver, "text", checkText);

        checkText = "José";
        PO_View.checkElementBy(driver, "text", checkText);

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





