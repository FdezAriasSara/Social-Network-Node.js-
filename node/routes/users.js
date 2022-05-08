const {ObjectId} = require("mongodb");
module.exports = function (app, usersRepository, publicationsRepository, messagesRepository) {
    const emailRegexp = new RegExp("\\w*\\@\\w*\\.\\w*");
    const nombreYapellidosRegExp = new RegExp("[a-zA-Z]+('-'|' '[a-zA-Z])*");
    const pswdRegExp = new RegExp("^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z]).{6,}$")//passwords must have at least eight characters, with at least one letter and one number.

    /**
     * Para poder listar usuarios hay que estar autenticados en la aplicación, esto se consigue a través
     * del userSessionRouter y por eso no se comprueba aquí.
     * Si el usuario identificado es Administrador, se le listarán todos los usuarios, excepto los administradores
     * ya que no se pueden eliminar a sí mismos.
     * El usuario con rol normal, lista a los demás usuarios no admins y se excluye a sí mismo.
     * En esta app, solo los administradores tienen definido el campo 'rol'.
     */
    app.get('/users/list', function (req, res) {
        let filter ={ role: {$exists:false} }; //Nunca listaremos al admin ya que el admin no se puede eliminar a sí mismo.
        let options = {};
        let admin = true;
        //Buscamos el usuario autenticado para saber si es admin o no y cambiar el filtrado de búsqueda
        //según el resultado.
        usersRepository.findUser({email: {$in: [req.session.user]}}, {}).then( result =>{
            if(result.length != 0){
                if(result[0].role === undefined){ // Si no tiene definido el campo rol significa que no es un administrador.
                    //Los usuarios sin rol administrador tienen la lista con paginación.
                    admin = false;
                    filter = {email: {$nin:[result[0].email]}, role: {$exists:false}} //excluimos al propio usuario que hace la consulta
                    let page = parseInt(req.query.page); // Es String !!!
                    if (typeof req.query.page === "undefined" || req.query.page === null || req.query.page === "0") { //
                       // Puede no venir el param
                        page = 1;
                    }
                    usersRepository.getUsersPg(filter, options, page).then( result=>{
                        let lastPage = result.total / 5;
                        if (result.total % 5 > 0) { // Sobran decimales
                            lastPage = lastPage + 1;
                        }
                        let pages = []; // paginas mostrar
                        for (let i = page - 2; i <= page + 2; i++) {
                            if (i > 0 && i <= lastPage) {
                                pages.push(i);
                            }
                        }
                        let response = {
                            users: result.users,
                            pages: pages,
                            currentPage: page,
                            admin: admin,
                            isLogedIn: req.session.user
                        }
                        res.render("user/list.twig", response)
                        return;
                    }).catch(error => {
                        res.render("error.twig",
                            {
                                message: "Error listando usuarios",
                                error: error
                            });
                    });
                }else{//si usuario administrador, lista sin paginacion.
                    usersRepository.getUsers(filter, options).then(users =>{ //renderizamos el listado de usuarios de acuerdo con el criterio filter
                        let response = {
                            users: users,
                            admin: admin,
                            isLogedIn: req.session.user
                    }
                        res.render("user/list.twig", response);
                    }).catch(error => {
                        res.render("error.twig",
                            {
                                message: "Error listando usuarios",
                                error: error
                            });
                    });;
                }
            }

            }

        ).catch(error => {
            res.render("error.twig",
                {
                    message: "Error listando usuarios - posiblemente no esté logueado.",
                    error: error
                });
        });


    });
    /**
     * Elimina usuarios segun una lista de ids embebida en la URL. Los usuarios a eliminar no pueden ser administradores.
     * Al eliminar un usuario se ha de eliminar toda la información relativa a los mismos, los datos, publicaciones, amistades...
     */
    app.get('/users/list/delete/:ids', function (req,res) {//Los administradores no se pueden eliminar. No tienen atributo 'rol'
        let idsToDelete = req.params.ids.split(',').map( id => ObjectId(id));
        let filter = { _id: {$in: idsToDelete} , rol:{$exists:false}  }
        usersRepository.findUser({email: {$in: [req.session.user]}}, {}).then( result =>{
            if(result.length != 0) {
                if (result[0].role === undefined) { //si el usuario identificado no tiene rol es que no es administrador. No puede borrar.
                    res.render("error.twig",
                        {
                            message: "Error borrando usuarios.",
                            error: "El usuario identificado no tiene privilegios de administrador."
                        });
                }else{//Si es administrador, borramos.
                    //Primero eliminamos las publicaciones de los usuarios que serán borrados..
                    deletePublications({userID:{$in: idsToDelete}}, function (result){
                        if(result == null){ //Si hay error, paramos y lo enseñamos al usuario.
                            res.redirect("/users/list" +
                                "?message="+ "Error eliminando publicaciones. No se pudo eliminar los registros." +
                                "&messageType=alert-danger");
                        }
                        return;
                    });
                    //Ahora eliminamos los mensajes de los usuarios que serán borrados..
                    deleteMessages({_id: {$in: idsToDelete}}, function(result){
                        if(result == null){ //Si hay error, paramos y lo enseñamos al usuario.
                            res.redirect("/users/list" +
                                "?message="+ "Error eliminando mensajes.. No se pudo eliminar los registros." +
                                "&messageType=alert-danger");
                        }
                        return;
                    })
                    //El último paso es eliminar a los usuarios en sí
                    usersRepository.deleteUsers(filter,{}).then( result=>{
                                res.redirect('/users/list' +  "?message=Registros correctamente eliminados" +
                                    "&messageType=alert-success");

                        }

                    ).catch(error => {
                        res.render("error.twig",
                            {
                                message: "Error borrando usuarios aqui.",
                                error: error
                            });
                    });
                }
            }
        });

    });
    //Elimina publicaciones de acuerdo con un criterio. Retorna null si ha habido error.
    function deletePublications(filterCriteria, callback){
        publicationsRepository.deletePublications(filterCriteria, {}).then(result=>{
            callback(true);
        }).catch(err=> callback(null));
    }
    //Elimina mensajes de acuerdo con un criterio. Retorna null si ha habido error.
    function deleteMessages(filterCriteria, callback){
        usersRepository.getUsers(filterCriteria,
            {email:1, _id:0}).then(emailsToDelete => {
            messagesRepository.deleteMessages(
                {$or: [{senderEmail: {$in:emailsToDelete}}, {receiverEmail: {$in:emailsToDelete}}]}
                , {}).then(result=>{
                callback(true);
            }).catch(err=> callback(null));
        }).catch(err => callback(null));

    }
    app.get('/users/login', function (req, res) {
        res.render("login.twig",{
            isLogedIn:false
        });
    });
    app.get('/users/logout', function (req, res) {
        req.session.user = null;
        res.render("login.twig",{
            isLogedIn:false
        });
    });
    app.get('/users/signup', function (req, res) {
        res.render("signup.twig",{
            isLogedIn:false
        });
    });
    /**
     * Método que envía los datos de inicio de sesión a la base de datos, para identificar al usuario.
     * Recibe como parámetro una función, en la que se encripta la contraseña para que esta viaje de manera segura.
     * Una vez encriptada, se busca el conjunto email-contraseña(hash) en la base de datos mediante el repositorio de usuarios.
     *  Una vez se inicia sesión :
     *     si el usuario es administrador -> se le redirige a la vista de todos los usuarios de la app.
     *     si el usuario NO es administrador->se le redirige a la vista “Ver listado de usuarios de la red social”
     */
    app.post('/users/login', function (req, res) {
        if (!req.body.email ||! req.body.contraseña ) {
            res.redirect("/users/login" +
                "?message=Debes rellenar todos los campos para iniciar sesión." +
                "&messageType=alert-danger ");
        } else if (!req.body.email.match(emailRegexp)) {
            res.redirect("/users/login" +
                "?message=El formato del email es incorrecto." +
                "&messageType=alert-danger ");
        } else {

            let securePassword = app.get("crypto").createHmac('sha256', app.get('clave'))
                .update(req.body.contraseña).digest('hex');
            let filter = {
                email: req.body.email,
                password: securePassword
            }
            let options = {};

            usersRepository.findUser(filter, options).then(user => {
                if (user == null || user.length <= 0) {
                    req.session.user = null;

                    res.redirect("/users/login" +
                        "?message=Email o password incorrecto" +
                        "&messageType=alert-danger ");

                } else {
                    req.session.user = user[0].email;
                    //TODO : CAMBIAR CUANDO LA FUNCIONALIDAD DEL REQUISITO 2 ESTÉ HECHA
                    res.redirect("/publications/add" );
                    /*
                    res.render("/nombreVistauserList.twig")
                    {

                            isLogedIn:( req.session.user!=null && req.session.user!= 'undefined')

                    };
                    */

                }
            }).catch(error => {
                req.session.user = null;
                res.redirect("/users/login" +
                    "?message=Se ha producido un error al buscar el usuario" +
                    "&messageType=alert-danger ");
            })
            ;
        }
    });
    /**
     * Método mediante el cual se envían los datos necesarios para registrarse en la aplicación.
     * De nuevo, la contraseña será encriptada para que esta viaje de forma segura.
     * Si el usuario se registra correctamente, se le reenviará a la vista de login, para que inicie sesión si así lo desea.
     * El registro requiere email, nombre , apellidos y contraseña (así como un campo de repetición de la misma)
     * si alguno de los anteriores campos no ha sido completado o tiene errores, el usuario será notificado mediante mensajes de error.
     * -El email debe tener el formato nombre@dominio . El nombre y los apellidos no pueden contener números,
     * y la contraseña ha de contener al menos letras y números.
     * (Los mensajes de error se envían por medio de la URL)
     */
    app.post('/users/signup', function (req, res) {

        if (!req.body.email|| !req.body.contraseña || !req.body.repContra || !req.body.nombre || !req.body.apellidos) {
            res.redirect("/users/signup" +
                "?message=Debes rellenar todos los campos para registrarte como usuario." +
                "&messageType=alert-danger ");
        } else if (!req.body.email.match(emailRegexp)) {
            res.redirect("/users/signup" +
                "?message=El formato del email es incorrecto. Debe ser parecido al siguiente: nombre@dominio.dominio" +
                "&messageType=alert-danger ");
        } else if (!req.body.nombre.match(nombreYapellidosRegExp) || !req.body.apellidos.match(nombreYapellidosRegExp)) {

            res.redirect("/users/signup" +
                "?message=Los campos de nombre y apellidos solamente aceptan letras , espacios o guiones." +
                "&messageType=alert-danger ");
        } else if (req.body.contraseña != req.body.repContra) {

            res.redirect("/users/signup" +
                "?message=Las contraseñas no coinciden." +
                "&messageType=alert-danger ");

        } else {

            usersRepository.findUser({email: req.body.email},{}).then(user=>{//buscando el amail
                if(user.length==0){
                    let securePassword = app.get("crypto").createHmac('sha256', app.get('clave'))
                        .update(req.body.contraseña).digest('hex');
                    let user = {
                        email: req.body.email,
                        name: req.body.nombre,
                        surname: req.body.apellidos,
                        password: securePassword

                    }
                    usersRepository.createUser(user).then(userId => {
                        res.redirect("/users/login" + '?message= ¡Te has registrado con éxito! Inicia sesión:' + "&messageType=alert-info");
                    }).catch(error => {
                        res.redirect("/users/signup" + '?message=Se ha producido un error al registrar tu usuario. Inténtalo de nuevo' + "&messageType=alert-danger");
                    });
                }else
                    res.redirect("/users/signup" + '?message=Ya existe un usuario con ese correo electrónico.' + "&messageType=alert-danger");
            }).catch(error=>res.redirect("/users/signup" + '?message=Se ha producido un error al registrar tu usuario, inténtalo de nuevo.' + "&messageType=alert-danger"));

        }
    });
}