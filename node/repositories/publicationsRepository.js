const {ObjectId} = require("mongodb");
module.exports = {
    mongoClient: null, app: null, usersRepository: null,

    init: function (app, mongoClient, usersRepository) {
        this.mongoClient = mongoClient;
        this.app = app;
        this.usersRepository = usersRepository;
    },
    insertPublication: async function (publication, userObjectID) {
        try {
            const client = await this.mongoClient
                .connect(this.app.get('connectionStrings'));
            const database = client.db("redsocial");
            const collectionName = 'publications';
            const publicationsCollection = database.collection(collectionName);

            let publicationsOfUser = []
            publicationsOfUser = await publicationsCollection.find({userID: userObjectID}, {publications: 1, _id:1})?.toArray();

            if(publicationsOfUser.length == 0){//Nunca ha hecho publicaciones
                //Si no tiene publicaciones, creamos el documento
                await publicationsCollection.insert({userID: userObjectID, publications: [publication] });
            }else{
                if(publicationsOfUser[0].publications.length > 0 ){ //Si ya tiene publicaciones, y
                    //la array de publicaciones NO está vacia insertamos la nueva
                    await publicationsCollection.update({userID: userObjectID}, {$push: { publications: publication } } );
                }else{
                    //Si el documento de publicaciones existe, pero por un casual, la array de publicaciones está vacía, la creamos
                    //añadiendo la publicacion
                    await publicationsCollection.insert({userID: userObjectID, publications: [publication] });
                }
            }





            client.close();
            return publicationsOfUser._id;
        } catch (error) {
            throw (error);

        }
    },
    findAllPublicationsByAuthorAndPage: async function (authorStringId, page){

        const limit = 5; //TOTAL_PUBLICATIONS_PER_PAGE
        const client = await this.mongoClient
            .connect(this.app.get('connectionStrings'));
        const database = client.db("redsocial");
        const collectionName = 'publications';
        const publicationsCollection = database.collection(collectionName);

        let publicationsDoc = await publicationsCollection.find({userID: ObjectId(authorStringId)}, {publications: 1, _id:0}).toArray();

        let usersArray = await this.usersRepository.findUserByStringId(authorStringId)

        let authorEmail = usersArray[0].email;

        //En qué publicacion empieza la página
        let startOfPage = (page-1)*limit

        let publicationsArray //Array de publicaciones a devolver
        let totalPublicationsFromUser = 0; //Numero total de publicaciones
        //Si hay publicaciones de ese autor aun
        if(publicationsDoc.length > 0){

            publicationsArray = publicationsDoc[0].publications.slice(startOfPage, startOfPage+limit )
            totalPublicationsFromUser = publicationsDoc[0].publications.length;
        }



        client.close();
        return {publicationsArray: publicationsArray, total: totalPublicationsFromUser, author: authorEmail};

    },
    findAllPublicationsByAuthor: async function (authorObjectID){
        const client = await this.mongoClient
            .connect(this.app.get('connectionStrings'));
        const database = client.db("redsocial");
        const collectionName = 'publications';
        const publicationsCollection = database.collection(collectionName);

        let publicationsDocument = await publicationsCollection.find({userID: authorObjectID}, {publications: 1, _id:0}).toArray();

        client.close();
        return publicationsDocument;

    },
    deletePublications:async function (filter) {
        try {
            const client = await this.mongoClient
                .connect(this.app.get('connectionStrings'));
            const database = client.db("redsocial");
            const collectionName = 'publications';
            const publicationsCollection = database.collection(collectionName);
            const result = await publicationsCollection.deleteMany(filter);
            client.close();
            return result;
        } catch (error) {
            throw (error);
        }
    }

}