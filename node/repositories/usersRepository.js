const {ObjectId} = require("mongodb");
module.exports = {

    mongoClient: null,
    app: null,
    init: function ( app, mongoClient ){
        this.mongoClient = mongoClient;
        this.app = app;
    },
    createUser: async function (user){

        try {
            const client = await this.mongoClient
                .connect(this.app.get('connectionStrings'));
            const database = client.db("redsocial");
            const collectionName = 'users';
            const usersCollection = database.collection(collectionName);
            let userComplete = {
                ...user,
                invitesSent: [],
                invitesReceived: [],
                friendships:[]
            }
            const result = await usersCollection.insertOne(userComplete);
            return result.insertedId;
        } catch (error) {
            throw (error);

        }


    },

    findUser: async function (filter, options) {
        try {

            const client = await this.mongoClient.connect(this.app.get('connectionStrings'));
            const database = client.db("redsocial");
            const collectionName = 'users';
            const usersCollection = database.collection(collectionName);
            const users = await usersCollection.find(filter, options).toArray();
            return users;
        } catch (error) {
            throw (error);
        }
    },


    findUserByStringId: async function (stringId){
        try {

            const client = await this.mongoClient.connect(this.app.get('connectionStrings'));
            const database = client.db("redsocial");
            const collectionName = 'users';
            const usersCollection = database.collection(collectionName);
            const users = await usersCollection.find({_id:ObjectId(stringId)}, {}).toArray();
            return users;
        } catch (error) {
            throw (error);
        }
    },

    findInvitesReceivedBy: async function ( receiverObjectID ){

        try {
            const client = await this.mongoClient.connect(this.app.get('connectionStrings'));
            const database = client.db("redsocial");
            const collectionName = 'users';
            const usersCollection = database.collection(collectionName);

            //Encuentra el usuario con ObjectID
            let filterUsers = {_id: receiverObjectID};
            //Las opciones: solo seleccionamos el ID del receptor y la lista de ID's de usuarios
            // de los qie tenemos invitaciones recibidas
            let options = {  invitesReceived: 1, _id:0}

            const invitesReceived = await usersCollection.find(filterUsers, options).toArray();
            return invitesReceived;
        } catch (error) {
            throw (error);
        }

    },

    findInvitesSentBy: async function ( senderObjectID ){

        try {
            const client = await this.mongoClient.connect(this.app.get('connectionStrings'));
            const database = client.db("redsocial");
            const collectionName = 'users';
            const usersCollection = database.collection(collectionName);

            //Encuentra el usuario con ObjectID
            let filterUsers = {_id: senderObjectID};
            //Las opciones: solo seleccionamos el ID del emisor y la lista de ID's de usuarios
            // que han recibido su invitación
            let options = {  invitesSent: 1, _id:0}

            const invitesSent = await usersCollection.find(filterUsers, options).toArray();
            return invitesSent;
        } catch (error) {
            throw (error);
        }

    }
    ,

    makeFriends: async function ( senderObjectID, receiverObjectID ){

        try {
            const client = await this.mongoClient.connect(this.app.get('connectionStrings'));
            const database = client.db("redsocial");
            const collectionName = 'users';
            const usersCollection = database.collection(collectionName);


            //Encuentra el usuario con ObjectID --> miraremos el emisor primero
            let filterUsers = {_id: senderObjectID};
            //Las opciones: solo seleccionamos el ID del emisor y la lista de ID's de usuarios
            // que han recibido su invitación
            //Actualizamos la lista de peticiones enviadas por el emisor, ahora ya no contiene la enviada al receptor
            await usersCollection.update(filterUsers, {$pull: { invitesSent: receiverObjectID } } );

            //Establecemos la amistad entre el emisor y el receptor

            await usersCollection.update(filterUsers, {$push: { friendships: receiverObjectID } } );



            /////////HACEMOS LO MISMO CON EL RECEPTOR: BORRAMOS LA PETITION RECIBIDA

            //Encuentra el usuario con ObjectID --> miraremos el RECEPTOR
            filterUsers = {_id: receiverObjectID};
            //Las opciones: solo seleccionamos el ID del receptor y la lista de ID's de usuarios
            // que le han enviado solicitud

            //Actualizamos la lista de peticiones enviadas por el emisor, ahora ya no contiene la enviada al receptor
            await usersCollection.update(filterUsers, {$pull: { invitesReceived: senderObjectID } } );

            //Establecemos la amistad entre el receptor y el emisor

            await usersCollection.update(filterUsers, {$push: { friendships: senderObjectID } } );



        } catch (error) {
            throw (error);
        }

    },
//Checkea que el usuario con ID MyObjectId es amigo de OtherUserObjectId
    isFriendOf: async function (MyObjectId, OtherUserObjectId) {
        try {

            const client = await this.mongoClient.connect(this.app.get('connectionStrings'));
            const database = client.db("redsocial");
            const collectionName = 'users';
            const usersCollection = database.collection(collectionName);

            const users = await usersCollection.find({_id: MyObjectId}, {}).toArray();
            const myself = users[0];
            let friendShipExists = false;
            for(let i = 0; i < myself.friendships.length; i++){
                if(myself.friendships[i].equals(OtherUserObjectId)){
                    friendShipExists = true;
                    break;
                }
            }

            return friendShipExists;
        } catch (error) {
            throw (error);
        }
    }




}