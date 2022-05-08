package com.uniovi.sdipractica234;


import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.uniovi.sdipractica234.pageobjects.PO_SignUpView;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;


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
    private String signUpURL="http://localhost:8090/users/signup";
    static MongoClient mongoClient= MongoClients.create("mongodb+srv://admin:admin@cluster0.6uext.mongodb.net/myFirstDatabase?retryWrites=true&w=majority");

    static MongoDatabase db;
    static MongoCollection<Document> usersCollection;
    static MongoCollection<Document> publiCollection;
    static  MongoCollection<Document>  msgsCollection;


    //Común a Windows y a MACOSX
    public static WebDriver getDriver(String PathFirefox, String Geckodriver) {
        System.setProperty("webdriver.firefox.bin", PathFirefox);
        System.setProperty("webdriver.gecko.driver", Geckodriver);
        driver = new FirefoxDriver();
        return driver;
    }

    @BeforeEach
    public void setUp() {
      //  driver.navigate().to(URL);
    } //Después de cada prueba se borran las cookies del navegador

    @AfterEach
    public void tearDown() {
        driver.manage().deleteAllCookies();

    }

    //Antes de la primera prueba
    @BeforeAll
    static public void begin() {
    try{
            db=mongoClient.getDatabase("redsocial");
            usersCollection=  db.getCollection("users");
            publiCollection=  db.getCollection("publications");
            msgsCollection=  db.getCollection("messages");
    }catch (MongoException e){
      throw e;
    }}

    //Al finalizar la última prueba
    @AfterAll
    static public void end() {
      Bson query=eq("name","test");
        var deleteUsers=usersCollection.deleteMany(query);
        var deletePublications=usersCollection.deleteMany(query);
        var deleteMessages=usersCollection.deleteMany(query);
        mongoClient.close();
        driver.quit();
    }



    //[Prueba1-1] Registro de Usuario con datos válidos.
    @Test
    @Order(1)
    void PR01_1() {
       PO_SignUpView.goToSignUpPage(driver);
       long usersBefore=usersCollection.countDocuments();//Number of users prior to sign up process.

        PO_SignUpView.goToSignUpPage(driver);
        PO_SignUpView.fillForm(driver,"sara@email.com","Sara","Fernández Arias",
               "password","password");
       Document user=usersCollection.find(eq("email","sara@email.com")).first();
        Assertions.assertTrue(user!=null);//check that the user can be found by  his/her email
       Assertions.assertTrue(usersBefore < usersCollection.countDocuments());//Check that the number of documents increases after registering.
        //now we remove the document from the database.
        usersCollection.deleteOne(eq("email","sara@email.com"));

    }


    //[Prueba1-2] Registro de Usuario con datos inválidos (username vacío, nombre vacío, apellidos vacíos).
    @Test
    @Order(2)
    void PR01_2() {
        PO_SignUpView.goToSignUpPage(driver);
        long usersBefore=usersCollection.countDocuments(); //Number of users prior to sign up process.
        String failureMessage="Debes rellenar todos los campos para registrarte como usuario.";
        PO_SignUpView.goToSignUpPage(driver);
        PO_SignUpView.fillForm(driver,"","","",
                "password","password");
        String found=driver.findElement(By.className("alert")).getText();
        Assertions.assertTrue(found!=null);
        Assertions.assertEquals(failureMessage,found);//to make the test fail in case the verbose alert is not displayed
        //The register will not take place since all fields are required.
       Assertions.assertTrue(usersBefore == usersCollection.countDocuments());//Check that the number of documents REMAINS EQUAL

    }


    //[Prueba1-3] Registro de Usuario con datos inválidos (repetición de contraseña inválida).
    @Test
    @Order(3)
    public void PR01_3() {
        PO_SignUpView.goToSignUpPage(driver);
        long usersBefore=usersCollection.countDocuments(); //Number of users prior to sign up process.
        PO_SignUpView.goToSignUpPage(driver);
        PO_SignUpView.fillForm(driver,"sara@email.com","Sara","Fernández",
                "pass","pass123");
        String alert="Las contraseñas no coinciden.";
        String found=driver.findElement(By.className("alert")).getText();
        Assertions.assertTrue(found!=null);
       Assertions.assertEquals(alert,found);//to make the test fail in case the verbose alert is not displayed
        Assertions.assertTrue(usersCollection.countDocuments()==usersBefore);//Check that the number of documents remains equals



    }

    //[Prueba1-4] Registro de Usuario con datos inválidos (email existente).
    @Test
    @Order(4)
    public void PR01_4() {

        PO_SignUpView.goToSignUpPage(driver);
        long usersBefore=usersCollection.countDocuments(); //Number of users prior to sign up process.
        PO_SignUpView.goToSignUpPage(driver);
        PO_SignUpView.fillForm(driver,"user01@email.com","Sara","Fernández",
                "pass","pass");
        //check that the alert message is displayed.
        String alert="Ya existe un usuario con ese correo electrónico.";
        String found=driver.findElement(By.className("alert")).getText();
        Assertions.assertTrue(found!=null);
        Assertions.assertEquals(alert,found);//to make the test fail in case the verbose alert is not displayed
        Assertions.assertTrue(usersCollection.countDocuments() ==usersBefore);
    }
    //[Prueba EXTRA] Registro de Usuario con datos inválidos (el email tiene formato invalido)
    @Test
    @Order(5)
    public void PR01_Extra1() {

        PO_SignUpView.goToSignUpPage(driver);
        long usersBefore=usersCollection.countDocuments(); //Number of users prior to sign up process.
        PO_SignUpView.goToSignUpPage(driver);
        PO_SignUpView.fillForm(driver,"emailInvalido@email","Sara","Fernández",
                "pass","pass");
        //check that the alert message is displayed.
        String alert="El formato del email es incorrecto. Debe ser parecido al siguiente: nombre@dominio.dominio";
        String found=driver.findElement(By.className("alert")).getText();
        Assertions.assertTrue(found!=null);
        Assertions.assertEquals(alert,found);//to make the test fail in case the verbose alert is not displayed
        Assertions.assertTrue(usersCollection.countDocuments() ==usersBefore);
    }

/*
    //[Prueba2-1] Inicio de sesión con datos válidos (administrador).
    @Test
    @Order(5)
    public void PR02_1() {

        PO_LoginView.goToLoginPage(driver);
        PO_LoginView.fillForm(driver,"admin@email.com","admin");


        //Si se ha logeado bien, podrá encontrar el boton de logout
        WebElement myDynamicElement = (new WebDriverWait(driver,
                10))
                .until(ExpectedConditions.presenceOfElementLocated(By.id("logout")));


        WebElement logoutButton = driver.findElement(By.id("logout"));
        Assertions.assertTrue(logoutButton != null);

        //Para comprobar, checkeamos que el menú de listar logs sea visible (solo admins)
        WebElement logsButton = driver.findElement(By.id("listLogs"));
        Assertions.assertTrue(logsButton != null);
    }


    //[Prueba2-2] Inicio de sesión con datos válidos (usuario estándar).
    @Test
    @Order(6)
    public void PR02_2() {


        PO_LoginView.goToLoginPage(driver);
        PO_LoginView.fillForm(driver,"user01@email.com","user01");


        //Si se ha logeado bien, podrá encontrar el boton de logout
        WebElement myDynamicElement = (new WebDriverWait(driver,
                10))
                .until(ExpectedConditions.presenceOfElementLocated(By.id("logout")));
        WebElement logoutButton = driver.findElement(By.id("logout"));
        Assertions.assertTrue(logoutButton != null);

        //Si se ha logeado bien, podra ver el menu de usuario "Mi Cuenta"
        WebElement myAccountDropdown = driver.findElement(By.id("accountDropdown"));
        Assertions.assertTrue(logoutButton != null);



    }

    //[Prueba2-3] Inicio de sesión con datos inválidos (usuario estándar, campo email y contraseña vacíos).
    @Test
    @Order(7)
    public void PR02_3() {


        //Te logueas con credenciales vacias
        PO_LoginView.goToLoginPage(driver);
        PO_LoginView.fillForm(driver,"","");

        //Sigues en la página de login
        List<WebElement> welcomeMessageElement = PO_LoginView.getLoginText(driver,PO_Properties.getSPANISH());

        Assertions.assertEquals(welcomeMessageElement.get(0).getText(),
                PO_View.getP().getString("login.title",
                        PO_Properties.getSPANISH()));



    }

    //[Prueba2-4] Inicio de sesión con datos válidos (usuario estándar, email existente, pero contraseña incorrecta).
    @Test
    @Order(8)
    public void PR02_4() {


        //Te logueas con email existente pero contraseña invalida
        PO_LoginView.goToLoginPage(driver);
        PO_LoginView.fillForm(driver,"user01@email.com","nopassword");

        //Sigues en la página de login
        List<WebElement> welcomeMessageElement = PO_LoginView.getLoginText(driver,PO_Properties.getSPANISH());

        Assertions.assertEquals(welcomeMessageElement.get(0).getText(),
                PO_View.getP().getString("login.title",
                        PO_Properties.getSPANISH()));



    }


    //[Prueba3-1] Hacer clic en la opción de salir de sesión
    // y comprobar que se redirige a la página de inicio de sesión (Login).
    @Test
    @Order(9)
    public void PR03_1() {


        //Te logueas con email existente
        PO_LoginView.goToLoginPage(driver);
        PO_LoginView.fillForm(driver,"user01@email.com","user01");

        //Puedes hacer logout
        PO_NavView.clickLogout(driver);

        //Te vas a la página de login
        List<WebElement> welcomeMessageElement = PO_LoginView.getLoginText(driver,PO_Properties.getSPANISH());

        Assertions.assertEquals(welcomeMessageElement.get(0).getText(),
                PO_View.getP().getString("login.title",
                        PO_Properties.getSPANISH()));

    }


    //[Prueba3-2] Comprobar que el botón cerrar sesión no está visible si el usuario no está autenticado
    @Test
    @Order(10)
    public void PR03_2() {


        //Si el usuario no esta autenticado y tratamos de buscar el boton,
        //se lanzará una NoSuchElementException
        try{

            driver.findElement(By.id("logout"));
            //El test fallará si encuentra el botón sin logearse
            Assertions.assertTrue(false);
        }catch(NoSuchElementException e){
            //El test pasa si no encuentra el botón sin logearse
            Assertions.assertTrue(true);
        }



    }


    //Prueba[4-1] Mostrar el listado de usuarios y comprobar que se muestran todos los que existen en el sistema.
    @Test
    @Order(32)
    public void PR04_1(){
        loginAs("admin@email.com", "admin");
        PO_UsersView.goToUsersList(driver);

        List<User> totalUsers = usersRepository.getUsersAdminView(Pageable.unpaged()).getContent();

        List<WebElement> usersInListView = driver.findElements(By.className("username"));

        Assertions.assertTrue( totalUsers.size() == usersInListView.size(),
                "Sizes differ: size of users with ROLE_USER in DB differ with size of users displayed");
        List<String> userNames = new LinkedList<>();
        for (WebElement element:
                usersInListView) {
            userNames.add(element.getText());
        }
        for (User user:
                totalUsers) {
            Assertions.assertTrue(userNames.contains(user.getUsername()), "Username:"+user.getUsername()+" not present in the view ");
        }


    }
    //Prueba[5-1] Ir a la lista de usuarios, borrar el primer usuario de la lista, comprobar que la lista se actualiza
    //y dicho usuario desaparece.
    @Test
    @Order(33)
    public void PR05_1(){
        loginAs("admin@email.com", "admin");
        PO_UsersView.goToUsersList(driver);
        deleteUserInPath("//*[@id=\"tableUsers\"]/tbody/tr[1]/td[4]/input");


    }
    //Prueba[5-2] Ir a la lista de usuarios, borrar el último usuario de la lista, comprobar que la lista se actualiza
    //y dicho usuario desaparece.
    @Test
    @Order(34)
    public void PR05_2(){
        loginAs("admin@email.com", "admin");
        PO_UsersView.goToUsersList(driver);
        deleteUserInPath("//*[@id=\"tableUsers\"]/tbody/tr[last()]/td[4]/input");

    }
    //Prueba[5-3] Ir a la lista de usuarios, borrar 3 usuarios, comprobar que la lista se actualiza y dichos usuarios
    //desaparecen.
    @Test
    @Order(35)
    public void PR05_3(){
        loginAs("admin@email.com", "admin");
        PO_UsersView.goToUsersList(driver);
        List<User> totalUsers = usersRepository.getUsersAdminView(Pageable.unpaged()).getContent(); //usersBefore
        WebElement firstUserEl = driver.findElement(By.xpath("//*[@id=\"tableUsers\"]/tbody/tr[1]/td[4]/input"));
        WebElement secondUserEl =driver.findElement(By.xpath("//*[@id=\"tableUsers\"]/tbody/tr[2]/td[4]/input"));
        WebElement thirdUserEl = driver.findElement(By.xpath("//*[@id=\"tableUsers\"]/tbody/tr[last()-1]/td[4]/input"));
        String idFirstUser = firstUserEl.getAttribute("id");
        String idSecondUser = secondUserEl.getAttribute("id");
        String idThirdUser = thirdUserEl.getAttribute("id");
        User firstDeleted = getUser(Long.valueOf(idFirstUser));
        User secondDeleted = getUser(Long.valueOf(idSecondUser));
        User thirdDeleted = getUser(Long.valueOf(idThirdUser));
        //Select the checkboxes of three users
        firstUserEl.click();
        secondUserEl.click();
        thirdUserEl.click();
        driver.findElement(By.id("deleteButton")).click(); //Press delete button and delete users.
        SeleniumUtils.waitSeconds(driver, 1); //wait a second in order for the database to update
        List<User> usersAfterDeleting = usersRepository.getUsersAdminView(Pageable.unpaged()).getContent();
        Assertions.assertTrue(totalUsers.size() == usersAfterDeleting.size()+3);
        Assertions.assertTrue(!usersAfterDeleting.contains(firstDeleted), "User with id: "+ idFirstUser+" was not deleted");
        Assertions.assertTrue(!usersAfterDeleting.contains(secondDeleted), "User with id: "+ idFirstUser+" was not deleted");
        Assertions.assertTrue(!usersAfterDeleting.contains(thirdDeleted), "User with id: "+ idFirstUser+" was not deleted");
        //volvemos a añadir los usuarios eliminados

        usersRepository.save(firstDeleted);
        usersRepository.save(secondDeleted);
        usersRepository.save(thirdDeleted);

    }
    private User getUser(Long id){
        Optional<User> user = usersRepository.findById(Long.valueOf(id));
        if(user.isPresent()){
            return user.get();
        }else {
            Assertions.fail("Failed when trying to retrieve user : " +id);
            return null;

        }
    }
    private void loginAs(String username, String password){
        PO_LoginView.goToLoginPage(driver);
        PO_LoginView.fillForm(driver,username,password);
    }
    private void deleteUserInPath(String xPath){


        List<User> totalUsers = usersRepository.getUsersAdminView(Pageable.unpaged()).getContent();

        WebElement element = driver.findElement(By.xpath(xPath)); //Eliminaremos el usuario del path
        String userId = element.getAttribute("id");
        User userDeleted = getUser(Long.valueOf(userId));
        Assertions.assertTrue(totalUsers.contains(userDeleted)); //Chequeo user is present
        element.click();
        driver.findElement(By.id("deleteButton")).click(); //we delete the user

        List<User> remainingUsers =  usersRepository.getUsersAdminView(Pageable.unpaged()).getContent();
        Assertions.assertTrue(!remainingUsers.contains(userDeleted), "User was not deleted");
        Assertions.assertTrue( totalUsers.size() == remainingUsers.size()+1,
                "Sizes differ: seems like user was not deleted");


        usersRepository.save(userDeleted); //Volvemos a añadir el usuario eliminado
    }

    //Prueba[5-2] Mostrar el listado de usuarios y comprobar que se muestran todos los que existen en el sistema,
    //excepto el propio usuario y aquellos que sean Administradores
    @Test
    @Order(36)
    public void PR06_1(){
        //Login como user01 y nos vamos a la vista de listar usuarios
        loginAs("user01@email.com", "user01");
        PO_UsersView.goToUsersList(driver);


        compareUserListViewWithUserListInSystem();

    }

    private void compareUserListViewWithUserListInSystem() {
        //Preparar variables para testear: usuario que pedirá la peticion, listado de usernames que se mostrará en la vista
        // además de listado de posibles usernames de administradores.
        User userThatAskedForList = usersRepository.findByUsername("user01@email.com");
        List<String> adminUsernames = new LinkedList<>();
        for (User admin:
                usersRepository.finAdminUsers()) {
            adminUsernames.add(admin.getUsername());
        }
        List<String> usernamesThatShouldBeInView = new LinkedList<>();
        for (User u:
                usersRepository.getUsersNormalUserView(Pageable.unpaged(), 1L).getContent()) {
            usernamesThatShouldBeInView.add(u.getUsername());
        }


        //Obtenemos todas las páginas que hay
        List<WebElement> nextPage= driver.findElements(By.id("pagsiguiente"));
        List<WebElement> usernamesDisplayed;

        while( !nextPage.isEmpty()){

            nextPage = driver.findElements(By.id("pagsiguiente"));
            usernamesDisplayed = driver.findElements(By.className("username")); //cogemos los usernames que aparecen en la vista
            //Por cada username en vista, chequeamos los asertos:
            for (WebElement usernameDisplayed:
                    usernamesDisplayed) {
                Assertions.assertTrue( !adminUsernames.contains(usernameDisplayed.getText()));
                Assertions.assertTrue( !userThatAskedForList.getUsername().equals(usernameDisplayed.getText()), "The user that asks for the list is displayed");
                Assertions.assertTrue(usernamesThatShouldBeInView.contains(usernameDisplayed.getText()), "Username: "+ usernameDisplayed + " should not be displayed.");
            }
            if(!nextPage.isEmpty()){//Cuando llega al último número d página, no hay más elementos con id 'pagsiguiente'.
                nextPage.get(0).click();
            }

        }
    }

    //Prueba[7_1]Hacer una búsqueda con el campo vacío y comprobar que se muestra la página que
    //corresponde con el listado usuarios existentes en el sistema.
    @Test
    @Order(37)
    public void PR07_1(){
        //Login como user01 y nos vamos a la vista de listar usuarios
        loginAs("user01@email.com", "user01");
        PO_UsersView.goToUsersList(driver);
        driver.findElement(By.name("searchText")).sendKeys("");
        driver.findElement(By.id("searchButton")).click();
        compareUserListViewWithUserListInSystem();
    }
    //Prueba[7_2]Hacer una búsqueda escribiendo en el campo un texto que no exista y comprobar que se
    //muestra la página que corresponde, con la lista de usuarios vacía.
    @Test
    @Order(38)
    public void PR07_2(){
        //Login como user01 y nos vamos a la vista de listar usuarios
        loginAs("user01@email.com", "user01");
        PO_UsersView.goToUsersList(driver);
        driver.findElement(By.name("searchText")).sendKeys("¡¡NoExistente!!");
        driver.findElement(By.id("searchButton")).click();
        Assertions.assertTrue(driver.findElements(By.className("username")).isEmpty());
    }

    //Prueba[7_3]Hacer una búsqueda con un texto específico y comprobar que se muestra la página que
    //corresponde, con la lista de usuarios en los que el texto especificado sea parte de su nombre, apellidos o
    //de su email.
    @Test
    @Order(39)
    public void PR07_3(){
        //Login como user01 y nos vamos a la vista de listar usuarios
        loginAs("user01@email.com", "user01");
        PO_UsersView.goToUsersList(driver);
        driver.findElement(By.name("searchText")).sendKeys("2");
        driver.findElement(By.id("searchButton")).click();
        List<WebElement> usernamesDisplayed = driver.findElements(By.className("username")); //cogemos los usernames que aparecen en la vista
        List<String> userNamesObtainedWithSearchBy_2_ = new LinkedList<>();
        userNamesObtainedWithSearchBy_2_.add("user02@email.com");
        userNamesObtainedWithSearchBy_2_.add("user12@email.com");
        userNamesObtainedWithSearchBy_2_.add("martin2@email.com");
        //Por cada username en vista, chequeamos los asertos:
        for (WebElement usernameDisplayed:
                usernamesDisplayed) {
            Assertions.assertTrue(userNamesObtainedWithSearchBy_2_.contains(usernameDisplayed.getText()), "Username: "+ usernameDisplayed.getText() + " should not be displayed!");
        }
    }

    //[Prueba 8-1] Iniciamos sesión, mandamos una invitación de amistad a otro usuario, cerramos sesión y entramos como
    // el otro usuario para comprobar que la nueva invitación aparece en la lista.
    @Test
    @Order(11)
    public void PR08_1(){
        PO_LoginView.goToLoginPage(driver);
        PO_LoginView.fillForm(driver,"user10@email.com","user10");

        //send invite
        driver.findElement(By.xpath("//*[@id=\"sendButton1\"]")).click();

        //logout
        PO_NavView.clickLogout(driver);

        //login as user01
        PO_LoginView.fillForm(driver,"user01@email.com","user01");

        //go to invite list
        PO_FriendsView.goToListFriendsInvitations(driver);

        //check that user 10 is there
        String checkText = "User10Nombre";
        List<WebElement> result = PO_View.checkElementBy(driver, "text", checkText);
        Assertions.assertEquals(checkText, result.get(0).getText());
    }

    //[Prueba 8-2] Iniciamos sesión, mandamos una invitación de amistad a otro usuario, y tratamos de volver a mandar una
    //invitación. El mensaje "enviado" aparecerá en el botón y no nos dejará volver a enviar invitación
    @Test
    @Order(12)
    public void PR08_2(){
        PO_LoginView.goToLoginPage(driver);
        PO_LoginView.fillForm(driver,"user04@email.com","user04");

        //send invite
        By path = By.xpath("//*[@id=\"sendButton1\"]");
        driver.findElement(path).click();

        //check that it was sent
        Assertions.assertEquals(driver.findElement(path).getText(), "Enviado");
    }

    //[Prueba 9-1] Iniciamos sesión y mostramos el listado de invitaciones de amistad recibidas
    @Test
    @Order(13)
    public void PR09_1(){
        PO_LoginView.goToLoginPage(driver);
        PO_LoginView.fillForm(driver,"user05@email.com","user05");

        //go to invite list
        PO_FriendsView.goToListFriendsInvitations(driver);

        //check that all invites inputted are there
        List<WebElement> inviteList = SeleniumUtils.waitLoadElementsBy(driver, "free", "//tbody/tr", PO_View.getTimeout());
        Assertions.assertEquals(3, inviteList.size());
    }

    //[Prueba 10-1] Sobre el listado de invitaciones recibidas. Hacer clic en el botón/enlace de una de ellas y comprobar
    // que dicha solicitud desaparece del listado de invitaciones
    @Test
    @Order(14)
    public void PR010_1(){
        PO_LoginView.goToLoginPage(driver);
        PO_LoginView.fillForm(driver,"user05@email.com","user05");

        //go to invite list
        PO_FriendsView.goToListFriendsInvitations(driver);

        //accept invite
        By path = By.xpath("//*[@id=\"tableInvites\"]/tbody/tr/td/a");
        driver.findElement(path).click();

        //check that it disappears from the invite list
        List<WebElement> inviteList = SeleniumUtils.waitLoadElementsBy(driver, "free", "//tbody/tr", PO_View.getTimeout());
        Assertions.assertEquals(2, inviteList.size());
    }

    //[Prueba 10-2] Prueba adicional para comprobar que tras aceptar la invitación, aparece el usuario en el listado de
    // amigos
    @Test
    @Order(15)
    public void PR010_2(){
        PO_LoginView.goToLoginPage(driver);
        PO_LoginView.fillForm(driver,"user05@email.com","user05");


        //go to friends list
        PO_FriendsView.goToListFriends(driver);

        //check that user is NOT there (user05 has one friend that he accepted in the previous text)
        List<WebElement> inviteList = SeleniumUtils.waitLoadElementsBy(driver, "free", "//tbody/tr", PO_View.getTimeout());
        Assertions.assertEquals(1, inviteList.size());

        //go to invite list
        PO_FriendsView.goToListFriendsInvitations(driver);

        //accept both invites
        By path = By.xpath("//*[@id=\"tableInvites\"]/tbody/tr/td/a");
        driver.findElement(path).click();
        path = By.xpath("//*[@id=\"tableInvites\"]/tbody/tr/td/a");
        driver.findElement(path).click();

        //go to friends list
        PO_FriendsView.goToListFriends(driver);

        //check that both users are now there
        List<WebElement> friendList = SeleniumUtils.waitLoadElementsBy(driver, "free", "//tbody/tr", PO_View.getTimeout());
        Assertions.assertEquals(3, friendList.size());
    }

    //[Prueba 11-1] Mostrar el listado de amigos de un usuario. Comprobar que el listado contiene los amigos que deben ser
    @Test
    @Order(16)
    public void PR011_1(){
        PO_LoginView.goToLoginPage(driver);
        PO_LoginView.fillForm(driver,"user01@email.com","user01");

        //go to friends list
        PO_FriendsView.goToListFriends(driver);

        //check correct number of friends (take into account user 5 has accepted the request in previous tests)
        List<WebElement> friendList = SeleniumUtils.waitLoadElementsBy(driver, "free", "//tbody/tr", PO_View.getTimeout());
        Assertions.assertEquals(5, friendList.size());

        //Check specifically every friend
        String checkText = "User02Nombre";
        PO_View.checkElementBy(driver, "text", checkText);

        checkText = "User03Nombre";
        PO_View.checkElementBy(driver, "text", checkText);

        checkText = "User07Nombre";
        PO_View.checkElementBy(driver, "text", checkText);
    }


    //[Prueba24] Ir al formulario de crear publicaciones , rellenarlo con datos VÁLIDOS y pulsar el botón de enviar.
    @Test
    @Order(17)
    public void PR012_1() {
        //El usuario debe estar registrado para hacer un post , por tanto
        PO_LoginView.fillForm(driver,"user17@email.com","user17");
        //Una vez autenticado el usuario,rellena el formulario
        PO_PostFormView.fillForm(driver,"Días de vacaciones", "Me lo he pasado genial en málaga! :)");

        //Vamos a la última página
        List<WebElement> elements= PO_View.checkElementBy(driver, "free", "//a[contains(@class, 'page-link')]");
        //Nos vamos a la última página
        elements.get(1).click();
        elements=PO_View.checkElementBy(driver, "text", "Días de vacaciones");
        //Comprobamos que aparece la nueva publicación.
        Assertions.assertEquals("Días de vacaciones",elements.get(0).getText());
    }

    //[Prueba25] Ir al formulario de crear publicaciones , rellenarlo con datos INVÁLIDOS (título vacío) y pulsar el botón de enviar.
    @Test
    @Order(18)
    public void PR012_2() {
        //El usuario debe estar registrado para hacer un post , por tanto
        PO_LoginView.fillForm(driver,"user01@email.com","user01");
        //Una vez autenticado el usuario,rellena el formulario
        PO_PostFormView.fillForm(driver,"", "Me lo he pasado genial en málaga! :)");

        List<WebElement> emptyMessage= PO_View.checkElementBy(driver, "text", PO_View.getP().getString("Error.posts.add.empty.title",PO_Properties.getSPANISH()));

        Assertions.assertTrue(emptyMessage.get(0).getText().contains("El título de la publicación no puede estar vacío."));
        //Como aparece también el mensaje de longitud, uso assert equals y contains para centrarme en el mensaje de vacío.
    }
    //[PRUEBA EXTRA APARTADO 12]Comprobar que no se puede realizar una publicación sin cuerpo.
    @Test
    @Order(19)
    public void PR012_3() {
        //El usuario debe estar registrado para hacer un post , por tanto
        PO_LoginView.fillForm(driver,"user01@email.com","user01");
        //Una vez autenticado el usuario,rellena el formulario
        PO_PostFormView.fillForm(driver,"Vacaciones!", "");
        List<WebElement> emptyMessage= PO_View.checkElementBy(driver, "text", PO_View.getP().getString("Error.posts.add.empty.description",PO_Properties.getSPANISH()));
        Assertions.assertTrue(emptyMessage.get(0).getText().contains("La descripción de la publicación no puede estar vacía."));//Como aparece también el mensaje de longitud, uso assert equals y contains para centrarme en el mensaje de vacío.

    }
    //[PRUEBA EXTRA APARTADO 12]Comprobar que no se puede realizar una publicación con un título demasiado corto (menor a 10 caracteres)
    @Test
    @Order(20)
    public void PR012_4() {
        //El usuario debe estar registrado para hacer un post , por tanto
        PO_LoginView.fillForm(driver,"user01@email.com","user01");
        //Una vez autenticado el usuario,rellena el formulario
        PO_PostFormView.fillForm(driver,"corto", "Descripción de más de 15 caracteres");
        List<WebElement> emptyMessage= PO_View.checkElementBy(driver, "text", PO_View.getP().getString("Error.posts.add.title.tooShort",PO_Properties.getSPANISH()));
        Assertions.assertEquals("El título debe tener al menos 10 caracteres.",emptyMessage.get(0).getText());

    }
    //[PRUEBA EXTRA APARTADO 12]Comprobar que no se puede realizar una publicación con una descripción demasiado corta (menor a 15 caracteres)
    @Test
    @Order(21)
    public void PR012_5() {
        //El usuario debe estar registrado para hacer un post , por tanto
        PO_LoginView.fillForm(driver,"user01@email.com","user01");
        //Una vez autenticado el usuario,rellena el formulario
        PO_PostFormView.fillForm(driver,"Vacaciones!", "hola");
        List<WebElement> emptyMessage= PO_View.checkElementBy(driver, "text", PO_View.getP().getString("Error.posts.add.description.tooShort",PO_Properties.getSPANISH()));
        Assertions.assertEquals("La descripción debe tener al menos 15 caracteres.",emptyMessage.get(0).getText());

    }

    //[Prueba26]Mostrar el listado de publicaciones de un usuario y comprobar que se muestran todas las que existen para dicho usuario.
    @Test
    @Order(22)
    public void PR013_1() {
        //El usuario debe estar registrado para hacer un post , por tanto
        PO_LoginView.fillForm(driver,"user17@email.com","user17");
        //Una vez inciada la sesión , el usuario podrá ver sus publicaciones.
        PO_NavView.clickListPosts(driver);
        //Una vez el usuario seleccione la opción de ver sus publicaciones, comprobamos que realmente se muestran.
        PO_ListPostsView.checkPosts(driver,5);//primera página.
        List<WebElement> elements= PO_View.checkElementBy(driver, "free", "//a[contains(@class, 'page-link')]");
        //Nos vamos a la última página
        elements.get(1).click();
        PO_ListPostsView.checkPosts(driver,5);//segunda página.
    }

    //[Prueba26-EXTRA]Mostrar el listado de publicaciones de un usuario que no tiene ninguna-> mensaje "No hay publicaciones"
    @Test
    @Order(23)
    public void PR013_2() {
        //El usuario debe estar registrado para hacer un post , por tanto
        PO_LoginView.fillForm(driver,"userExtra@email.com","userExtra");
        //Una vez inciada la sesión , el usuario podrá ver sus publicaciones.(No tiene)
        PO_NavView.clickListPosts(driver);
        //Una vez el usuario seleccione la opción de ver sus publicaciones, comprobamos que realmente se muestran.
        List<WebElement>  noPostsMsg=PO_View.checkElementBy(driver, "text", PO_View.getP().getString("posts.list.noPosts",PO_Properties.getSPANISH()));
        Assertions.assertEquals("No hay publicaciones disponibles.",noPostsMsg.get(0).getText());
    }

    //[PRUEBA 27]Mostrar el listado de publicaciones de un usuario amigo y comprobar que se muestran todas las que existen para dicho usuario.
    @Test
    @Order(24)
    public void PR014_1() {
        //El usuario debe estar registrado para hacer un post , por tanto
        PO_LoginView.fillForm(driver,"user01@email.com","user01");
        //El usuario 01 es amigo del usuario 17, que tiene 10 publicaciones.
        PO_FriendsView.goToListFriends(driver);
        By enlace = By.id("User17Nombre");
        driver.findElement(enlace).click();
        List<WebElement> postMessage= PO_View.checkElementBy(driver, "text", PO_View.getP().getString("posts.list.message",PO_Properties.getSPANISH()));
        Assertions.assertEquals("Estas son las publicaciones del usuario:",postMessage.get(0).getText());
        //Una vez el usuario seleccione la opción de ver sus publicaciones, comprobamos que realmente se muestran.
        PO_ListPostsView.checkPosts(driver,5);//primera página.
        List<WebElement> elements= PO_View.checkElementBy(driver, "free", "//a[contains(@class, 'page-link')]");
        //Nos vamos a la última página
        elements.get(1).click();
        PO_ListPostsView.checkPosts(driver,5);//segunda página.
    }

    //[PRUEBA 24]Utilizando un acceso vía URL u otra alternativa, tratar de listar las publicaciones de un usuario que no sea amigo del usuario identificado en sesión. Comprobar que el sistema da un error de autorización.
    @Test
    @Order(25)
    public void PR014_2() {
        //El usuario debe estar registrado para hacer un post , por tanto
        PO_LoginView.fillForm(driver,"user02@email.com","user02");
        //El usuario 02 NO es amigo del usuario 17, que tiene 10 publicaciones.
        driver.get("http://localhost:8090/posts/listFor/user17@email.com");
        List<WebElement> forbiddenMessage= PO_View.checkElementBy(driver, "text", PO_View.getP().getString("error.message",PO_Properties.getSPANISH()));
        Assertions.assertEquals("Parece que este sitio no existe o no tienes acceso a él :(",forbiddenMessage.get(0).getText());
    }

    //[PRUEBA EXTRA APARTADO 14]Mostrar el listado de publicaciones de un usuario amigo que no tiene publicaciones.
    @Test
    @Order(26)
    public void PR014_3() {
        //El usuario debe estar registrado para hacer un post , por tanto
        PO_LoginView.fillForm(driver,"user02@email.com","user02");
        //El usuario 02 es amigo del usuario extra, que NO tiene publicaciones.
        PO_FriendsView.goToListFriends(driver);
        By enlace = By.id("UserExtraNombre");
        driver.findElement(enlace).click();
        //Una vez el usuario seleccione la opción de ver sus publicaciones, comprobamos que realmente se muestran.
        List<WebElement>  noPostsMsg=PO_View.checkElementBy(driver, "text", PO_View.getP().getString("posts.list.noPosts",PO_Properties.getSPANISH()));
        Assertions.assertEquals("No hay publicaciones disponibles.",noPostsMsg.get(0).getText());
    }

    //[PRUEBA EXTRA APARTADO 14]Visualizar al menos cuatro páginas en español/inglés/español
    // (comprobando que algunas de las etiquetas cambian al idioma correspondiente).
    // Ejemplo, Página principal/Opciones Principales de Usuario/Listado de Usuarios.
    @Test
    @Order(99)
    public void PR015_1() {

        //Nos vamos a la página de inicio de session
        PO_LoginView.goToLoginPage(driver);

        //El texto de bienvenida debe estar en español
        List<WebElement>  loginText=PO_View.checkElementBy(driver, "text", PO_View.getP().getString("login.title",PO_Properties.getSPANISH()));
        Assertions.assertEquals("Identifícate",loginText.get(0).getText());

        //Cambiamos a inglés
        driver.findElement(By.id("btnLanguage")).click();
        driver.findElement(By.id("btnEnglish"));

        //El texto de bienvenida debe estar en ingles
        loginText=PO_View.checkElementBy(driver, "text", PO_View.getP().getString("login.title",PO_Properties.getSPANISH()));
        Assertions.assertEquals("Login to enter",loginText.get(0).getText());

    }


    //[Prueba16-1] Intentar acceder sin estar autenticado a la opción de
    //listado de usuarios. Se deberá volver al formulario de login.
    @Test
    @Order(27)
    void PR016_1() {

        PO_LogsView.goToLogsPage(driver);
        List<WebElement> welcomeMessageElement = PO_LoginView.getLoginText(driver,PO_Properties.getSPANISH());

        Assertions.assertEquals(welcomeMessageElement.get(0).getText(),
                PO_View.getP().getString("login.message",
                        PO_Properties.getSPANISH()));

    }

    //[Prueba16-2] Intentar acceder sin estar autenticado a la opción de listado de invitaciones de amistad
    // recibida de un usuario estándar. Se deberá volver al formulario de login
    @Test
    @Order(28)
    void PR016_2() {

        PO_FriendsView.goToListFriendsInvitations(driver);
        List<WebElement> welcomeMessageElement = PO_LoginView.getLoginText(driver,PO_Properties.getSPANISH());

        Assertions.assertEquals(welcomeMessageElement.get(0).getText(),
                PO_View.getP().getString("login.message",
                        PO_Properties.getSPANISH()));

    }


    //[Prueba6-3] Estando autenticado como usuario estándar intentar acceder a una opción disponible
    // solo para usuarios administradores (Añadir menú de auditoria (visualizar logs)).
    // Se deberá indicar un mensaje de acción prohibida.
    @Test
    @Order(29)
    void PR016_3() {

        PO_SignUpView.goToSignUpPage(driver);
        PO_SignUpView.fillForm(driver,"martin@email.com","Martin","Beltran",
                "password","password");

        PO_LoginView.goToLoginPage(driver);
        PO_LoginView.fillForm(driver,"martin@email.com","password");

        PO_LogsView.goToLogsPage(driver);

        List<WebElement> ohohMessage = PO_ErrorView.getErrorText(driver,PO_Properties.getSPANISH());

        Assertions.assertEquals(ohohMessage.get(0).getText(), "OhOh");

    }


    //[Prueba16-4] Estando autenticado como usuario administrador visualizar
    // todos los logs generados en una serie de interacciones.
    // Esta prueba deberá generar al menos dos interacciones de cada tipo y
    // comprobar que el listado incluye los logs correspondientes.
    @Test
    @Order(30)
    void PR016_4() {

        //Generate several logs of different types
        PO_LogsView.generateBatchLogs(driver);

        String[] types = {"PET","LOGOUT","LOGIN_EX","LOGIN_ERR","ALTA"};

        List<WebElement> logsListed;
        for(String type : types){

            //Primer log de los PET
            logsListed = PO_LogsView.getLogListedInPosition(driver, type, 1);
            Assertions.assertEquals(logsListed.get(0).getText(), type);

            //Primer log de los PET
            logsListed = PO_LogsView.getLogListedInPosition(driver, type, 2);
            Assertions.assertEquals(logsListed.get(0).getText(), type);
        }


    }


    //[Prueba16-5] Estando autenticado como usuario administrador,
    // ir a visualización de logs, pulsar el botón/enlace borrar logs y
    // comprobar que se eliminan los logs de la base de datos
    @Test
    @Order(31)
    void PR016_5() {
        PO_LoginView.goToLoginPage(driver);
        PO_LoginView.fillForm(driver,"admin@email.com","admin");
        PO_LogsView.goToLogsPage(driver);

        List<Log> prevDelete = logRepository.findAll();
        int sizeBeforeDeletion = prevDelete.size();
        Assertions.assertTrue( sizeBeforeDeletion >= 0);


        PO_LogsView.deleteFirstLog(driver);

        List<Log> afterDelete = logRepository.findAll();
        int sizeAfterDeletion = afterDelete.size();



        Assertions.assertTrue(sizeAfterDeletion + 1 == sizeBeforeDeletion);

    }
*/
}