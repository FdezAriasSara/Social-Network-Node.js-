const {ObjectId} = require("mongodb");
const {v4: uuidv4} = require("uuid")
var log4js = require("log4js");
var logger = log4js.getLogger();
logger.level = "debug"

module.exports = function (app, usersRepository, publicationsRepository) {

    const TOTAL_PUBLICATIONS_PER_PAGE = 5;

    //GET LISTAR PUBLICACIONES
    app.get("/publications/list", function (req, res) {

        logger.info("[GET] - [/publications/list")

        let page = parseInt(req.query.page); // Es String !!!
        if (typeof req.query.page === "undefined"
            || req.query.page === null
            || req.query.page === "0") {
            // Puede no venir el param
            page = 1;
        }

        //busco el usuario logueado para obtener sus publicaciones




        //Si no estoy logueado, me manda a la pagina de inicio de sesion
        if(!req.session.user){
            res.render("/login")
            ;        }




        usersRepository.findUser({email: req.session.user}, {})
            .then(async users => {

                //Cuando recibo el user, obtengo el _id y busco las publicaciones



                if(users.length < 0 ){

                    //Como lo que se devuelve al buscar un usuario es una array,
                    // si no contiene nada significa que no encontró el usuario
                    res.status(500);
                    logger.error("Error reconociendo usuario: no existe")
                    res.render("error.twig",
                        {
                            message: "Error reconociendo usuario: no existe",
                            error: new Error()
                        });
                    return;
                }



                //Si se devuelve algo en la array, debería ser un solo usuario, se selecciona el primer elemento
                let user = users[0];

                //Metodo para reutilizar código, renderiza las publicaciones del usuario que se pase como parametro
                await renderPublicationsOf(user._id,page, req,res);



            })
            .catch(error => {
                logger.error("Error listando tus publicaciones")
                res.render("error.twig",
                    {
                        message: "Error listando tus publicaciones",
                        error: error
                    });
            })



    });




    //GET FORMULARIO DE CREACION DE PUBLICACION
    app.get("/publications/add", function (req, res) {



        logger.info("[GET] - [/publications/add]")
        res.render("publications/add.twig",{isLoggedIn: ( req.session.user )});
    });


    //POST FORMULARIO DE CREACION DE PUBLICACION
    app.post("/publications/add", async function (req, res){


        logger.info("[POST] - [/publications/add]")

        //Si no estoy logueado, me manda a la pagina de inicio de sesión
        if(!req.session.user){
            logger.error("Usuario no logueado")
            res.redirect("/login")
            ;        }


        if(req.session.user == null || req.session.user == undefined){
            logger.error("Usuario no logueado")
            res.redirect("/login")
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
                logger.warn("Error en formulario de añadir publicacion")
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



            //No chequeo error porque se supone que el loggin funciona (me fio de mis compañeros=+)
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
                                    logger.error("Error al subir la imagen, pero publicación añadida")
                                    res.send("Error al subir la imagen, pero publicación añadida");
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
                        "?message=Error al registrar la publicación, inténtelo de nuevo:"+error +
                        "&messageType=alert-danger");
                })


        }




    });



    //GET LISTAR PUBLICACIONES
    app.get("/publications/friend/:friendId", function (req, res) {
        logger.info("[GET] - [/publications/friend/:friendId] - [friendId: " + req.params.friendId + " ]")

        let page = parseInt(req.query.page); // Es String !!!
        if (typeof req.query.page === "undefined"
            || req.query.page === null
            || req.query.page === "0") {
            // Puede no venir el param
            page = 1;
        }

        //busco el usuario logueado para obtener sus publicaciones




        //Si no estoy logueado, me manda a la pagina de inicio de sesion
        if(!req.session.user){
            res.redirect("/login")
;        }


        //Id textual del amigo
        let friendId = req.params.friendId;

        //Miramos si mi usuario existe
        usersRepository.findUser({email: req.session.user}, {})
            .then( async users => {

                //Cuando recibo el user, obtengo el _id y busco las publicaciones

                if(users.length < 0 ){
                    //Como lo que se devuelve al buscar un usuario es una array,
                    // si no contiene nada significa que no encontró el usuario
                    res.status(500);
                    res.render("error.twig",
                        {
                            message: "Error reconociendo usuario: no existe",
                            error: new Error()
                        });
                    return;
                }
                //Si se devuelve algo en la array, debería ser un solo usuario, se selecciona el primer elemento
                let myself = users[0];


                //Checkeamos que soy amigo del usuario del que pido las publicaciones
                usersRepository.isFriendOf(myself._id, ObjectId(friendId))
                    .then((response)=>{
                        //Si hay respuesta, es true o false.

                        if(response === true){ //Son amigos

                            //Metodo para reutilizar código, renderiza las publicaciones del usuario que se pase como parametro
                            renderPublicationsOf(friendId,true,page, req,res);

                        }else{ //No son amigos

                            res.render("error.twig",
                                {
                                    message: "El usuario " + req.session.user
                                        + " NO es amigo del usuario con ID: " + friendId ,
                                    error: new Error()
                                });


                        }


                    })
                    .catch((error)=>{

                        res.render("error.twig",
                            {
                                message: "Error comprobando si el usuario " + req.session.user
                                    + " es amigo del usuario con ID: " + friendId ,
                                error: error
                            });

                    });




            })
            .catch(error => {
                res.render("error.twig",
                    {
                        message: "Error listando publicaciones del amigo " + friendId ,
                        error: error
                    });
            })



    });




    async function renderPublicationsOf(userStringId, isFriend, page, req, res) {

        publicationsRepository.findAllPublicationsByAuthorAndPage( ObjectId(userStringId), page)
            .then((publications) => {


                //Si el usuario tiene alguna publicacion
                if(publications.total != 0){

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
                            isLoggedIn:( req.session.user!=null && req.session.user!= 'undefined'),
                            pages: pages,
                            currentPage: page,
                            author: publications.author,
                            isFriend: isFriend,
                            authorID: userStringId
                        });

                }else{ //El usuario aun no tiene publicaciones

                    res.render("publications/publications.twig",
                        {
                            publications: [],
                            isLoggedIn:( req.session.user!=null && req.session.user!= 'undefined'),
                            pages: 1,
                            currentPage: 1,
                            author: publications.author,
                            isFriend: isFriend,
                            authorID: userStringId
                        });

                }



            })
            .catch((error) => {
                res.send("Se ha producido un error al listar las publicaciones del usuario: " + error)
            })

    }


}

