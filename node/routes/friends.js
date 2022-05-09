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
            //Obtenemos el usuario en sesión para usar su _id para encontrar las invitaciones recibidas
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
            usersRepository.findInvitesReceivedBy(user._id).then(invites => {
                res.render("friends/invites.twig", {invites: invites});
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
}