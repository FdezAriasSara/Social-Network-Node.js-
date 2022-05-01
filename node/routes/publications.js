const {ObjectId} = require("mongodb");
const {v4: uuidv4} = require("uuid")
module.exports = function (app, usersRepository, publicationsRepository) {

    const TOTAL_PUBLICATIONS_PER_PAGE = 9;

    //GET LISTAR PUBLICACIONES
    app.get("/publications/list", function (req, res) {


        let page = parseInt(req.query.page); // Es String !!!
        if (typeof req.query.page === "undefined"
            || req.query.page === null
            || req.query.page === "0") {
            // Puede no venir el param
            page = 1;
        }

        //busco el usuario logeado para obtener sus publicaciones
        //BORRAR CUANDO ALGUIEN IMPLEMENTE EL LOGN
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

                        //Formateamos la fecha para que no salga con hora,minuto,segundos y zona horaria:
                        realPublications.forEach(pub => {
                            pub.dateofcreation = new Date(pub.dateofcreation).toLocaleString().split(',')[0]
                        })

                        res.render("publications/publications.twig",
                            {
                                publications: realPublications,
                                isLogedIn:( req.session.user!=null && req.session.user!= 'undefined'),
                                pages: pages,
                                currentPage: page
                            });

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




    //GET FORMULARIO DE CREACION DE PUBLICACION
    app.get("/publications/add", function (req, res) {
        //BORRAR DESPUES DE QUE ALGUIEN IMLPEMENTE EL LOGIN:
        req.session.user = "elseñor@delanoche.com"
        res.render("publications/add.twig",{isLogedIn: ( req.session.user!=null && req.session.user!= 'undefined')});
    });


    //POST FORMULARIO DE CREACION DE PUBLICACION
    app.post("/publications/add", async function (req, res){

        //BORRAR DESPUES DE QUE ALGUIEN IMLPEMENTE EL LOGIN:
        req.session.user = "elseñor@delanoche.com"
        if(req.session.user == null || req.session.user == undefined){
            res.render("login.twig")
        }else{

            let errorMessage = "";
            let emptyFields = false;
            if(! req.body.description){
                errorMessage += "La descripción es un campo obligatorio. - "
                emptyFields = true;
            }

            if(! req.body.title){
                errorMessage += "El título es un campo obligatorio."
                emptyFields = true;
            }

            if( emptyFields){
                res.redirect("/publications/add" +
                    "?message="+ errorMessage +
                    "&messageType=alert-danger");
                return;
            }

            let publication;
            if(req.files != null){//Se envió imagen

                //Generate random UUID unique for the image
                let randomUUIDForImage = uuidv4();
                 publication = {
                    dateofcreation: new Date(),
                    content: req.body.description,
                    title: req.body.title,
                    pictureURL: randomUUIDForImage + '.png'
                };


            }else{


                publication = {
                    dateofcreation: new Date(),
                    content: req.body.description,
                    title: req.body.title
                };

            }



            //Find ObjectID of current user:
            let currentID = await usersRepository.findUser({email: req.session.user},
                {email:0,name:0,surname:0,password:0,_id:1});



            await publicationsRepository.insertPublication(publication, ObjectId(currentID[0]._id))
                .then( publicationDocumentID => {
                    if(req.files != null){

                        let imagen = req.files.imgPubli;
                        imagen.mv(app.get("uploadPath") + '\\public\\images\\' + randomUUIDForImage + '.png',
                            function (err) {
                                if(err){
                                    res.send("Error al subir la imagen.");
                                }else{
                                    res.redirect("/publications/list");
                                }
                            });

                    }else{
                        res.redirect("/publications/list");
                    }



                })
                .catch(error => {
                    res.redirect("/publications/add" +
                        "?message=Error al registrar la publicacion, intentelo de nuevo:"+error +
                        "&messageType=alert-danger");
                })


        }




    });

}