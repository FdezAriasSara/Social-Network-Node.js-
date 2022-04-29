const {ObjectId} = require("mongodb");
module.exports = function (app, usersRepository, publicationsRepository) {

    const TOTAL_PUBLICATIONS_PER_PAGE = 10;

    app.get("/publications/list", function (req, res) {


        let page = parseInt(req.query.page); // Es String !!!
        if (typeof req.query.page === "undefined"
            || req.query.page === null
            || req.query.page === "0") {
            // Puede no venir el param
            page = 1;
        }

        //busco el usuario logeado para obtener sus publicaciones
        req.session.user = "elseñor@delanoche.com"
        usersRepository.findUser({email: req.session.user}, {})
            .then(users => {

                //Cuando recibo el user, obtengo el _id y busco las publicaciones

                if(users.length < 0 ){
                    res.status(500);
                    res.render("/error",
                        {
                            message: "Error listando tus publicaciones, tu email no existe",
                            error: error
                        });
                    return;
                }
                let user = users[0];
                publicationsRepository.findAllPublicationsByAuthorAndPage( ObjectId(user._id), page)
                    .then((publications) => {


                        let lastPage = publications.total / TOTAL_PUBLICATIONS_PER_PAGE;
                        if (publications.total % TOTAL_PUBLICATIONS_PER_PAGE > 0) { // Sobran decimales
                            lastPage = lastPage + 1;
                        }
                        let pages = []; // paginas mostrar
                        for (let i = page - 2; i <= page + 2; i++) {
                            if (i > 0 && i <= lastPage) {
                                pages.push(i);
                            }
                        }


                        let realPublications = publications.publicationsArray
                        res.render("publications.twig", {publications: realPublications,
                            isLogedIn:( req.session.user!=null && req.session.user!= 'undefined'),
                        pages: pages, currentPage: page});
                    })
                    .catch((error) => {
                        res.send("Se ha producido un error al listar las publicaciones del usuario: " + error)
                    })



            })
            .catch(error => {
                res.render("/error",
                    {
                        message: "Error listando tus publicaciones",
                        error: error
                    });
            })



    });


}