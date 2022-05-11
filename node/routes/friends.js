const {ObjectId} = require('mongodb');
const {v4: uuidv4} = require("uuid");
var log4js = require("log4js");
var logger = log4js.getLogger();
logger.level = "debug";
module.exports = function (app, usersRepository) {

    const TOTAL_INVITES_PER_PAGE = 5;
    const TOTAL_FRIENDS_PER_PAGE = 5;

    /**
     * Devuelve la lista de invitaciones de amistad recibidas por el usuario en sesión
     */
    app.get('/friends/invites', function(req, res) {

        logger.info("[GET] - [/friends/invites]");

        //paginación
        let page = parseInt(req.query.page);
        if (typeof req.query.page === "undefined"
            || req.query.page === null
            || req.query.page === "0") {
            // Puede no venir el param
            page = 1;
        }

        let filter = { email: req.session.user};
        usersRepository.findUser( filter, {} ).then(async users => {
            //Obtenemos el usuario en sesión para usarlo para encontrar las invitaciones recibidas
            if (users.length < 1){
                //Si no encontramos el usuario, error
                res.status(500);
                logger.error("Error reconociendo usuario: no existe");
                res.render("error.twig",
                    {
                        message: "Error reconociendo usuario: no existe",
                        error: new Error()
                    });
                return;
            }
            let user = users[0];
            usersRepository.findInvitesReceivedByUser(user, page, TOTAL_INVITES_PER_PAGE).then(invites => {

                let invitesToShow = [];
                let pages = []; // paginas mostrar

                if (invites.total > 0) {

                    let lastPage = invites.total / TOTAL_INVITES_PER_PAGE;
                    if (invites.total % TOTAL_INVITES_PER_PAGE > 0) { // Sobran decimales
                        lastPage = lastPage + 1;
                    }

                    for (let i = page - 2; i <= page + 2; i++) {
                        if (i > 0 && i <= lastPage) {
                            pages.push(i);
                        }
                    }

                    invitesToShow = invites.invitesReceived;
                } else
                    page = 1;
                res.render("friends/invites.twig",
                    {isLoggedIn:( req.session.user!=null && req.session.user!= 'undefined'),
                        invites: invitesToShow,
                        pages: pages,
                        currentPage: page});
            }).catch(error => {
                logger.error("Error listando las invitaciones de amistad del usuario " + user);
                res.render("error.twig",
                    {
                        message: "Error listando las invitaciones de amistad del usuario " + user,
                        error: error
                    });
            })
        }).catch(error => {
            logger.error("Error encontrando al usuario");
            res.render("error.twig",
                {
                    message: "Error encontrando al usuario",
                    error: error
                });
        })
    });

    /**
     * Devuelve la lista de amigos del usuario en sesión
     */
    app.get('/friends/list', function (req, res){
        logger.info("[GET] - [/friends/list]");
        //paginación
        let page = parseInt(req.query.page);
        if (typeof req.query.page === "undefined"
            || req.query.page === null
            || req.query.page === "0") {
            // Puede no venir el param
            page = 1;
        }
        let filter = { email: req.session.user};
        usersRepository.findUser( filter, {} ).then(async users => {
            //Obtenemos el usuario en sesión para usarlo para encontrar las invitaciones recibidas
            if (users.length < 1){
                //Si no encontramos el usuario, error
                res.status(500);
                logger.error("Error reconociendo usuario: no existe")
                res.render("error.twig",
                    {
                        message: "Error reconociendo usuario: no existe",
                        error: new Error()
                    });
                return;
            }
            let user = users[0];
            usersRepository.findFriendsOfUser(user, page, TOTAL_FRIENDS_PER_PAGE).then(friends => {

                let friendsToShow = [];
                let pages = []; // paginas a mostrar

                if (friends.total > 0) {

                    let lastPage = friends.total / TOTAL_FRIENDS_PER_PAGE;
                    if (friends.total % TOTAL_INVITES_PER_PAGE > 0) { // Sobran decimales
                        lastPage = lastPage + 1;
                    }

                    for (let i = page - 2; i <= page + 2; i++) {
                        if (i > 0 && i <= lastPage) {
                            pages.push(i);
                        }
                    }

                    friendsToShow = friends.friendships;
                } else
                    page = 1;
                res.render("friends/list.twig",
                    {isLoggedIn:( req.session.user!=null && req.session.user!= 'undefined'),
                        friends: friendsToShow,
                        pages: pages,
                        currentPage: page});
            }).catch(error => {
                logger.error("Error listando las amistades del usuario " + user);
                res.render("error.twig",
                    {
                        message: "Error listando las amistades del usuario " + user,
                        error: error
                    });
            })
        }).catch(error => {
            logger.error("Error encontrando al usuario");
            res.render("error.twig",
                {
                    message: "Error encontrando al usuario",
                    error: error
                });
        })
    });

    /**
     * Mandar invitación de amistad a un usuario
     * No podemos enviar una invitación a uno mismo, a amigos, ni a gente con invitaciones
     * pendientes nuestras, tanto recibidas como mandadas
     */
    app.post('/friends/invite/:id', function (req, res){

        logger.info("[POST] - [/friends/invite/" + req.params.id + "]");

        let filter = { email: req.session.user};
        usersRepository.findUser( filter, {} ).then(async users => {
            //Obtenemos el usuario en sesión para usarlo como sender
            if (users.length < 1) {
                //Si no encontramos el usuario, error
                res.status(500);
                logger.error("Error reconociendo usuario: no existe");
                res.render("error.twig",
                    {
                        message: "Error reconociendo usuario: no existe",
                        error: new Error()
                    });
                return;
            }
            let sender = users[0];
            filter = {_id: ObjectId(req.params.id)};
            usersRepository.findUser( filter, {} ).then(async usersParam => {
                //Usando el id de parametro en la request, obtenemos el usuario que recibe la invitación
                if (usersParam.length < 1) {
                    //Si no encontramos el usuario, error
                    res.status(500);
                    logger.error("Error reconociendo usuario: no existe");
                    res.render("error.twig",
                        {
                            message: "Error reconociendo usuario: no existe",
                            error: new Error()
                        });
                    return;
                }
                let receiver = usersParam[0];
                //No podemos enviar una invitación a uno mismo, a amigos, ni a invitaciones
                //pendientes tanto recibidas como mandadas
                filter = {
                    $or:[
                        {_id: sender._id},
                        {'_id': {'$in' : sender.friendships}},
                        {'_id': {'$in' : sender.invitesReceived}},
                        {'_id': {'$in' : sender.invitesSent}}
                    ]
                };
                usersRepository.findUser( filter, {} ).then( async canNotSendUsers => {
                    const canNotSend = canNotSendUsers.some(u => {
                        if (u.email === receiver.email)
                            return true;
                        return false;
                    })
                    if (canNotSend){
                        res.redirect("/users/list" +
                            "?message=No puedes mandar una invitación a ese usuario" +
                            "&messageType=alert-danger");
                    } else {
                        await usersRepository.sendInvite(sender._id, receiver._id);
                        res.redirect("/users/list" +
                            "?message=Invitación enviada con éxito" +
                            "&messageType=alert-info");
                    }
                }).catch(error => {
                    logger.error("Error encontrando al usuario");
                    res.render("error.twig",
                        {
                            message: "Error encontrando al usuario",
                            error: error
                        });
                })
            }).catch(error => {
                logger.error("Error encontrando al usuario");
                res.render("error.twig",
                    {
                        message: "Error encontrando al usuario",
                        error: error
                    });
            })
        }).catch(error => {
            logger.error("Error encontrando al usuario");
            res.render("error.twig",
                {
                    message: "Error encontrando al usuario",
                    error: error
                });
        })
    });

    /**
     * Aceptar una invitación de amistad
     */
    app.post('/friends/accept/:id', function (req, res){

        logger.info("[POST] - [/friends/accept/" + req.params.id + "]");

        let filter = { email: req.session.user};
        usersRepository.findUser( filter, {} ).then(async users => {
            //Obtenemos el usuario en sesión para usarlo como receiver
            if (users.length < 1) {
                //Si no encontramos el usuario, error
                res.status(500);
                logger.error("Error reconociendo usuario: no existe");
                res.render("error.twig",
                    {
                        message: "Error reconociendo usuario: no existe",
                        error: new Error()
                    });
                return;
            }
            let receiver = users[0];
            filter = {_id: ObjectId(req.params.id)};
            usersRepository.findUser( filter, {} ).then(async usersParam => {
                //Usando el id de parametro en la request, obtenemos el usuario que recibe la invitación
                if (usersParam.length < 1) {
                    //Si no encontramos el usuario, error
                    res.status(500);
                    logger.error("Error reconociendo usuario: no existe");
                    res.render("error.twig",
                        {
                            message: "Error reconociendo usuario: no existe",
                            error: new Error()
                        });
                    return;
                }
                let sender = usersParam[0];
                //No podemos aceptar una invitación que no esté en la lista de invitaciones recibidas
                filter = {
                    '_id': {'$in' : receiver.invitesReceived}
                };
                usersRepository.findUser( filter, {} ).then( async canAcceptUsers => {
                    const canAccept = canAcceptUsers.some(u => {
                        if (u.email === sender.email)
                            return true;
                        return false;
                    })
                    if (canAccept){
                        await usersRepository.acceptInvite(sender._id, receiver._id);
                        res.redirect("/friends/invites" +
                            "?message=Invitación aceptada con éxito" +
                            "&messageType=alert-info");
                    } else {
                        res.redirect("/friends/invites" +
                            "?message=No puedes aceptar una invitación de ese usuario" +
                            "&messageType=alert-danger");
                    }
                }).catch(error => {
                    logger.error("Error encontrando al usuario");
                    res.render("error.twig",
                        {
                            message: "Error encontrando al usuario",
                            error: error
                        });
                })
            }).catch(error => {
                logger.error("Error encontrando al usuario");
                res.render("error.twig",
                    {
                        message: "Error encontrando al usuario",
                        error: error
                    });
            })
        }).catch(error => {
            logger.error("Error encontrando al usuario");
            res.render("error.twig",
                {
                    message: "Error encontrando al usuario",
                    error: error
                });
        })
    });
}