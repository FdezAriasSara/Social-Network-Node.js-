const {ObjectId} = require("mongodb")

module.exports = function (app, publicationsRepository, usersRepository) {

    app.get("/api/friends/list",  function (req, res) {

        //Este user viene de cuando generamos el token.
        let userEmail = res.user;

        usersRepository.findUser({email: userEmail}, {_id:1, friendships:1})
            .then(async result => {

                let user = result[0];
                let friendships = user.friendships;
                let arrayOfFriends = []

                for(let i = 0; i < friendships.length; i++){

                    await usersRepository.findUser({_id: friendships[i]},{_id:1,name:1,surname:1})
                        .then(friend => {

                            let realFriend = friend[0];
                            arrayOfFriends.push({_id: realFriend._id, name: realFriend.name, surname: realFriend.surname})

                        })
                        .catch(error => {

                            res.status(500);
                            res.json({
                                message: "Se ha producido un error al encontrar al amigo con id: " + friendships[i],
                                error: error

                            })

                        })

                }

                //Ordena los nombre por orden alfabetico
                //arrayOfFriends.sort( (a,b) => (a.name > b.name) ? 1 : ((b.name > a.name) ? -1 : 0 ));


                res.status(200);
                res.send({friends: arrayOfFriends})

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

}