const {ObjectId} = require('mongodb');
const {sendInvite} = require("../repositories/usersRepository");
module.exports = function (app, usersRepository) {

    const TOTAL_INVITES_PER_PAGE = 5;
    const TOTAL_FRIENDS_PER_PAGE = 5;

    /**
     * Devuelve la lista de invitaciones de amistad recibidas por el usuario en sesión
     */
    app.get('/friends/invites', function(req, res) {
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
                res.render("error.twig",
                    {
                        message: "Error listando las invitaciones de amistad del usuario " + user,
                        error: error
                    });
            })
        }).catch(error => {
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
                res.render("error.twig",
                    {
                        message: "Error listando las amistades del usuario " + user,
                        error: error
                    });
            })
        }).catch(error => {
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
        let filter = { email: req.session.user};
        usersRepository.findUser( filter, {} ).then(async users => {
            //Obtenemos el usuario en sesión para usarlo para encontrar las invitaciones recibidas
            if (users.length < 1) {
                //Si no encontramos el usuario, error
                res.status(500);
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
                //Obtenemos el usuario en sesión para usarlo para encontrar las invitaciones recibidas
                if (usersParam.length < 1) {
                    //Si no encontramos el usuario, error
                    res.status(500);
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
                    res.render("error.twig",
                        {
                            message: "Error encontrando al usuario",
                            error: error
                        });
                })
            }).catch(error => {
                res.render("error.twig",
                    {
                        message: "Error encontrando al usuario",
                        error: error
                    });
            })
        }).catch(error => {
            res.render("error.twig",
                {
                    message: "Error encontrando al usuario",
                    error: error
                });
        })
    });
}