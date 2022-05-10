const {ObjectId} = require("mongodb");
var log4js = require("log4js");
var logger = log4js.getLogger();
logger.level = "debug"
module.exports = function (app, usersRepository, publicationsRepository, messagesRepository) {
    const emailRegexp = new RegExp("\\w*\\@\\w*\\.\\w*");

    /**
     * Para poder listar usuarios hay que estar autenticados en la aplicación, esto se consigue a través
     * del userSessionRouter y por eso no se comprueba aquí.
     * Si el usuario identificado es Administrador, se le listarán todos los usuarios, excepto los administradores
     * ya que no se pueden eliminar a sí mismos.
     * El usuario con rol normal, lista a los demás usuarios no admins y se excluye a sí mismo.
     * En esta app, solo los administradores tienen definido el campo 'rol'.
     */
    app.get('/users/list', function (req, res) {
        logger.info("[GET] - [/users/list]")
        let filter = {role: {$exists: false}}; //Nunca listaremos al admin ya que el admin no se puede eliminar a sí mismo.
        let options = {};
        let admin = true;
        let searchCriteria = "";
        //Buscamos el usuario autenticado para saber si es admin o no y cambiar el filtrado de búsqueda
        //según el resultado.
        usersRepository.findUser({email: {$in: [req.session.user]}}, {}).then(result => {
                if (result.length != 0) {
                    if (result[0].role === undefined) { // Si no tiene definido el campo rol significa que no es un administrador.
                        //Los usuarios sin rol administrador tienen la lista con paginación.
                        admin = false;
                        //Crearemos el criterio de listado de usuarios en función de si se ha insertado un parámetro de
                        //búsqueda o no.
                        if (req.query.search != null && typeof (req.query.search) != "undefined" && req.query.search != "") {
                            filter = {
                                email: {$nin: [result[0].email]}, //No incluimos el usuario que pide la lista
                                $or: [
                                    {email: {$regex: ".*" + req.query.search + ".*", $options: 'i'}}, //Email,nombre,apellidos coincide con parámetro búsqueda
                                    {surname: {$regex: ".*" + req.query.search + ".*", $options: 'i'}},
                                    {name: {$regex: ".*" + req.query.search + ".*", $options: 'i'}}],
                                role: {$exists: false}//No se incluyen administradores
                            }
                            searchCriteria = req.query.search;
                        } else {
                            filter = {email: {$nin: [result[0].email]}, role: {$exists: false}} //excluimos al propio usuario que hace la consulta
                        }

                        let page = parseInt(req.query.page); // Es String !!!
                        if (typeof req.query.page === "undefined" || req.query.page === null || req.query.page === "0") { //
                            // Puede no venir el param
                            page = 1;
                        }
                        let user = result[0];
                        usersRepository.getUsersPg(filter, options, page).then(result => {
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

                            let filterCanNotSend = {
                                $or:[
                                    {_id: user._id},
                                    {'_id': {'$in' : user.friendships}},
                                    {'_id': {'$in' : user.invitesReceived}},
                                    {'_id': {'$in' : user.invitesSent}}
                                ]
                            };
                            usersRepository.findUser( filterCanNotSend, {} ).then( async canNotSendUsers => {
                                let canNotSendUsersMails = canNotSendUsers.map(element => element.email);
                                let response = {
                                    users: result.users,
                                    pages: pages,
                                    currentPage: page,
                                    admin: admin,
                                    isLoggedIn: req.session.user,
                                    searchCriteria: searchCriteria,
                                    canNotSendUsers: canNotSendUsersMails
                                }
                                logger.info(req.session.user+" ha accedido con éxito a la lista de usuarios");
                                res.render("user/list.twig", response)
                                return;
                            }).catch(error => {
                                logger.error("Se ha producido un error cuando el usuario  " + req.session.user.id + " ha accedido a la lista de usuarios");
                                res.render("error.twig",
                                    {
                                        message: "Error listando usuarios",
                                        error: error
                                    });
                            });
                        }).catch(error => {
                            logger.error("Se ha producido un error cuando el usuario  "+req.session.user.id+" ha accedido a la lista de usuarios");
                            res.render("error.twig",
                                {
                                    message: "Error listando usuarios",
                                    error: error
                                });
                        });
                    } else {//si usuario administrador, lista sin paginacion.
                        usersRepository.getUsers(filter, options).then(users => { //renderizamos el listado de usuarios de acuerdo con el criterio filter
                            let response = {
                                users: users,
                                admin: admin,
                                isLoggedIn: req.session.user
                            }
                            logger.warn("El administrador ha accedido a la lista de usuarios");
                            res.render("user/list.twig", response);
                        }).catch(error => {
                            logger.error("Se ha producido un error al listar usuarios mediante el usuario administrador");
                            res.render("error.twig",
                                {
                                    message: "Error listando usuarios",
                                    error: error
                                });
                        });

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
    app.get('/users/list/delete/:ids', function (req, res) {//Los administradores no se pueden eliminar. No tienen atributo 'rol'
        logger.info("[GET] - [/users/list/delete:ids]")
        let idsToDelete = req.params.ids.split(',').map(id => ObjectId(id));
        let filter = {_id: {$in: idsToDelete}, rol: {$exists: false}}
        usersRepository.findUser({email: {$in: [req.session.user]}}, {}).then(result => {
            if (result.length != 0) {
                if (result[0].role === undefined) { //si el usuario identificado no tiene rol es que no es administrador. No puede borrar.
                    res.render("error.twig",
                        {
                            message: "Error borrando usuarios.",
                            error: "El usuario identificado no tiene privilegios de administrador."
                        });
                } else {//Si es administrador, borramos.
                    //Primero eliminamos las publicaciones de los usuarios que serán borrados..
                    deletePublications(idsToDelete, function (result) {
                        if (result == null) { //Si hay error, paramos y lo enseñamos al usuario.
                            logger.error("Error inesperado al eliminar  las publicaciones del usuario "+filter._id);
                            res.redirect("/users/list" +
                                "?message="+ "Error eliminando publicaciones. No se pudo eliminar los registros." +
                                "&messageType=alert-danger");
                        }else{
                            //Ahora eliminamos los mensajes de los usuarios que serán borrados..
                            deleteMessages({_id: {$in: idsToDelete}}, function(result){
                                if(result == null){ //Si hay error, paramos y lo enseñamos al usuario.
                                    logger.error("Error inesperado al eliminar  mensajes del usuario "+filter._id);
                                    res.redirect("/users/list" +
                                        "?message="+ "Error eliminando mensajes.. No se pudo eliminar los registros." +
                                        "&messageType=alert-danger");
                                }else{
                                    //Ahora eliminamos amistades e invitaciones de los usuarios a eliminar
                                    deleteFriendShipsAndInvites(idsToDelete, function(result){
                                        if(result == null){ //Si hay error, paramos y lo enseñamos al usuario.
                                            logger.error("Error inesperado al eliminar  amistades/invitaciones del usuario "+filter._id);
                                            res.redirect("/users/list" +
                                                "?message="+ "Error eliminando las amistades/invitaciones.. No se pudo eliminar los registros." +
                                                "&messageType=alert-danger");
                                        }else{
                                            //El último paso es eliminar a los usuarios en sí
                                            usersRepository.deleteUsers(filter,{}).then( result=>{
                                                logger.warn("Se han eliminado los siguientes usuarios con éxito:");
                                                idsToDelete.forEach(id=>logger.info(id));
                                                res.redirect('/users/list' +  "?message=Registros correctamente eliminados" +
                                                        "&messageType=alert-success");

                                                }

                                            ).catch(error => {
                                                logger.error("Error en el borrado de usuarios.");
                                                res.render("error.twig",
                                                    {
                                                        message: "Error borrando usuarios aqui.",
                                                        error: error
                                                    });
                                            });
                                        }
                                    });
                                }
                            })
                        }
                    });


                }
            }
        });

    });

    /**
     * Elimina invitaciones enviadas,recibidas así como amistades en las que se encuentra un usuario que
     * va ser eliminado. Si userID 1 va ser eliminado y es amigo de userID2, hay que borrar como amigo
     * (al userID1) en userID2.
     * Si userID3 va ser eliminado y mandó petición de amistad a userID4 y recibió una de userID5, la
     * petición ha de ser eliminada de las recibidas en userID4 y de las enviadas en userID5.
     * @param idsToDelete
     * @param callback
     */
    function deleteFriendShipsAndInvites(idsToDelete, callback) {
        usersRepository.deleteFriendshipsAndInvites({},
            {
                $pull: {
                    invitesSent: {$in: idsToDelete},
                    invitesReceived: {$in: idsToDelete},
                    friendships: {$in: idsToDelete}
                }
            }).then(result => {
            callback(true);
        }).catch(err => callback(null));
    }

    //Elimina publicaciones de acuerdo con un criterio. Retorna null si ha habido error.
    function deletePublications(idsToDelete, callback) {
        publicationsRepository.deletePublications({userID: {$in: idsToDelete}}).then(result => {
            callback(true);
        }).catch(err => callback(null));
    }

    //Elimina mensajes de acuerdo con un criterio. Retorna null si ha habido error.
    function deleteMessages(filterCriteria, callback) {
        usersRepository.getUsers(filterCriteria,
            {projection: {email: 1, _id: 0}}).then(emailsToDelete => {
            let emailArray = []
            Object.keys(emailsToDelete).map(function (email) { emailArray.push(emailsToDelete[email].email)});
            messagesRepository.deleteMessages(
                {$or: [{senderEmail: {$in: emailArray}}, {receiverEmail: {$in: emailArray}}]}
                , {}).then(result => {
                callback(true);
            }).catch(err => callback(null));
        }).catch(err => callback(null));

    }

    app.get('/users/login', function (req, res) {
        logger.info("[GET] - [/users/login]")
        res.render("login.twig", {
            isLoggedIn: false
        });
    });
    app.get('/users/logout', function (req, res) {
        logger.info(req.session.user+" ha cerrado sesión");
        logger.info("[GET] - [/users/logout]")
        req.session.user = null;
        res.render("login.twig", {
            isLoggedIn: false
        });
    });
    app.get('/users/signup', function (req, res) {
        logger.info("[GET] - [/users/signup]")
        res.render("signup.twig", {
            isLoggedIn: false
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
        logger.info("[POST] - [/users/login]")
        if (!req.body.email || !req.body.contraseña) {
            logger.error("Error en el inicio de sesión: se han dejado campos sin rellenar.");
            res.redirect("/users/login" +
                "?message=Debes rellenar todos los campos para iniciar sesión." +
                "&messageType=alert-danger ");
        } else if (!req.body.email.match(emailRegexp)) {
            logger.error("Error en el inicio de sesión: El email tiene un formato incorrecto.")
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
                    logger.error("Error en el inicio de sesión: Los datos son válidos, pero no coinciden con la base de datos.")
                    res.redirect("/users/login" +
                        "?message=Email o password incorrecto" +
                        "&messageType=alert-danger ");

                } else {
                    req.session.user = user[0].email;
                    if(user.role!="undefined"){
                        logger.warn("El administrador ha iniciado sesión");
                    }else{
                        logger.info("El usuario: " + req.session.user + " ha iniciado sesión.");
                    }

                    res.redirect("/users/list");


                }
            }).catch(error => {
                req.session.user = null;
                logger.error("Error inesperado en el inicio de sesión.")
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
        logger.info("[POST] - [/users/signup]")

        if (!req.body.email || !req.body.contraseña || !req.body.repContra || !req.body.nombre || !req.body.apellidos) {
            logger.error("Error en registro: No se han rellenado todos los campos.")
            res.redirect("/users/signup" +
                "?message=Debes rellenar todos los campos para registrarte como usuario." +
                "&messageType=alert-danger ");
        } else if (!emailRegexp.test(req.body.email)) {
            logger.error("Error en registro:El formato del email no es correcto.")
            res.redirect("/users/signup" +
                "?message=El formato del email es incorrecto. Debe ser parecido al siguiente: nombre@dominio.dominio" +
                "&messageType=alert-danger ");
        } else if (req.body.contraseña != req.body.repContra) {
            logger.error("Error en registro: La contraseñas no coinciden.")
            res.redirect("/users/signup" +
                "?message=Las contraseñas no coinciden." +
                "&messageType=alert-danger ");

        } else {

            usersRepository.findUser({email: req.body.email}, {}).then(user => {//buscando el amail
                if (user.length == 0) {
                    let securePassword = app.get("crypto").createHmac('sha256', app.get('clave'))
                        .update(req.body.contraseña).digest('hex');
                    let user = {
                        email: req.body.email.trim(),
                        name: req.body.nombre.trim(),
                        surname: req.body.apellidos.trim(),
                        password: securePassword

                    }
                    usersRepository.createUser(user).then(userId => {
                        logger.info("Usuario registrado con éxito: " + userId);
                        res.redirect("/users/login" + '?message= ¡Te has registrado con éxito! Inicia sesión:' + "&messageType=alert-info");
                    }).catch(error => {
                        logger.error("Se ha producido un error inesperado en el registro");
                        res.redirect("/users/signup" + '?message=Se ha producido un error al registrar tu usuario. Inténtalo de nuevo' + "&messageType=alert-danger");
                    });
                } else{
                    logger.error("Error en registro: El correo electrónico ya existe.");
                res.redirect("/users/signup" + '?message=Ya existe un usuario con ese correo electrónico.' + "&messageType=alert-danger");
                }
            }).catch(error => {
                logger.error("Se ha producido un error inesperado en el registro");
                res.redirect("/users/signup" + '?message=Se ha producido un error al registrar tu usuario, inténtalo de nuevo.' + "&messageType=alert-danger");
            });
        }

    });
}


