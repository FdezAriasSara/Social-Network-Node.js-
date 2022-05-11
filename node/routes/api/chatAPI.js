const {ObjectId} = require("mongodb")

module.exports = function (app, publicationsRepository, usersRepository, messagesRepository) {

    app.get("/api/friends/list",  function (req, res) {

        //Este user viene de cuando generamos el token.
        let userEmail = res.user;

        usersRepository.findUser({email: userEmail}, {_id:1, friendships:1})
            .then(async result => {

                let user = result[0];
                let friendships = user.friendships;
                let arrayOfFriends = []
                let error=false;
                for(let i = 0; i < friendships.length; i++){

                    await usersRepository.findUser({_id: friendships[i]},{_id:1,name:1,surname:1})
                        .then(friend => {

                            let realFriend = friend[0];
                            arrayOfFriends.push({_id: realFriend._id, name: realFriend.name, surname: realFriend.surname})

                        })
                        .catch(error => {
                            error=true;
                            res.status(500);
                            res.json({
                                message: "Se ha producido un error al encontrar al amigo con id: " + friendships[i],
                                error: error

                            })

                        })
                    if(error){
                        return;}
                }
                if(!error) {

                    //Ordena los nombre por orden alfabetico
                    arrayOfFriends.sort((a, b) => a.name - b.name);


                    res.status(200);
                    res.json({friends: arrayOfFriends})
                    return;
                }
            })
            .catch(error => {

                res.status(500);
                res.json({
                    message: "Se ha producido un error al listar amigos",
                    error: error

                })

            })

    });


    //LOGIN EN API CON JSONWEBTOKEN
    app.post('/api/users/login', function (req, res) {

        try {

            let securePassword = app.get("crypto").createHmac('sha256', app.get('clave'))
                .update(req.body.password).digest('hex');

            let filter = {
                email: req.body.email,
                password: securePassword
            }

            let options = {}
            usersRepository.findUser(filter, options).then(users => {

                let user = users[0]

                if (user == null) {
                    res.status(401) //Unauthorized
                    res.json({
                        message: "Usuario no autorizado",
                        authenticated: false
                    })
                } else {
                    let token = app.get('jwt').sign(
                        {user: user.email, time: Date.now() / 1000}, "secreto"
                    )
                    req.session.user = user.email;

                    res.status(200);
                    res.json({
                        message: "usuario autorizado",
                        authenticated: true,
                        token: token
                    })
                }

            }).catch(error => {
                res.status(401);
                res.json({
                    message: "Se ha producido un error al verificar credenciales",
                    authenticated: false
                })
            })


        } catch (e) {
            res.status(500);
            res.json({
                message: "Se ha producido un error al verificar las credenciales.",
                authenticated: false
            })
        }


    });


    app.post("/api/message/add", async function (req, res) {

        //Si en el cuerpo de la peticoon no hay ni receptor ni contenido del mensaje
        if(!req.body.receiverEmail || !req.body.text){

            res.status(401);
            res.json({
                message: "Falta email receptor o contenido del mensaje"
            })
            return;
        }

        try {




            let sender = await usersRepository.findUser({email: res.user},{})
            let receiver = await usersRepository.findUser({email: req.body.receiverEmail},{}) ;

            sender = sender[0]
            receiver = receiver[0]

           let areFriends =  await usersRepository.isFriendOf( sender._id, receiver._id)


            if(areFriends === true){

                let message = {
                    senderEmail : res.user,
                    receiverEmail : req.body.receiverEmail,
                    text : req.body.text,
                    leido : false
                }
                messagesRepository.createMessage(message)
                    .then( (resultId) => {

                        res.status(200);
                        res.json({
                            message: "Mensaje creado con exito."
                        })


                    })
                    .catch( (error) => {

                        res.status(500);
                        res.json({
                            message: "Error al crear mensaje entre" + res.user + " y " + req.body.receiverEmail + ".",
                            error: error
                        })

                    })

            }else{

                res.status(500);
                res.json({
                    message: "" + res.user + " y " + req.body.receiverEmail + " no son amigos"
                })

            }



        } catch (e) {
            res.status(500);
            res.json({
                message: "Se ha producido un error al crear el mensaje."
            })
        }

    });

    app.post('/api/conversation', async function (req,res){

        try{

            let myself = res.user;

            let otherUser = null;

            let thereIsError = false;
            let message = ""
           await usersRepository.findUser({_id: ObjectId(req.body.idOtherUser)}, {})
                .then( otherUsers => {
                    if(otherUsers.length <= 0){
                        thereIsError=true;
                        message = "No existe usuario con id: " + req.body.idOtherUser
                    }
                    otherUser = otherUsers[0];
                })
                .catch(error => {
                    res.status(500);
                    res.json({
                        message: "Error identificando ID del otro usuario",
                        error: error
                    })
                })

            if(thereIsError){
                res.status(500);
                res.json({
                    message: message,
                    error: new Error()
                })
                return;
            }


            //primero pillamos los mensajes que le envié yo y luego los que me envió él.
            //Unimos las dos arrays
             let messages = await messagesRepository.getMessagesInConversionOf(myself, otherUser.email)
           // messages =messages.concat( await messagesRepository.getMessagesFromTo(otherUser.email, myself))

         //   messages.sort( (a,b) => b.date - a.date);

            res.status(200);
            res.json({
                messages: messages
            })

        }catch(e){
            res.status(500);
            res.json({
                message: "Se ha producido un error al leer conversación",
                error: e.message
            })
        }

    });

}