module.exports = {
    mongoClient: null, app: null, init: function (app, mongoClient) {
        this.mongoClient = mongoClient;
        this.app = app;
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






            return publicationsOfUser._id;
        } catch (error) {
            throw (error);

        }
    },
    findAllPublicationsByAuthorAndPage: async function (authorObjectID, page){

        const limit = 9; //TOTAL_PUBLICATIONS_PER_PAGE
        const client = await this.mongoClient
            .connect(this.app.get('connectionStrings'));
        const database = client.db("redsocial");
        const collectionName = 'publications';
        const publicationsCollection = database.collection(collectionName);

        let publicationsDoc = await publicationsCollection.find({userID: authorObjectID}, {publications: 1, _id:0}).toArray();

        const totalPublicationsFromUser = publicationsDoc[0].publications.length;

        let startOfPage = (page-1)*limit
        let publicationsArray = publicationsDoc[0].publications.slice(startOfPage, startOfPage+limit )

        return {publicationsArray: publicationsArray, total: totalPublicationsFromUser};

    },
    findAllPublicationsByAuthor: async function (authorObjectID){
        const client = await this.mongoClient
            .connect(this.app.get('connectionStrings'));
        const database = client.db("redsocial");
        const collectionName = 'publications';
        const publicationsCollection = database.collection(collectionName);

        let publicationsDocument = await publicationsCollection.find({userID: authorObjectID}, {publications: 1, _id:0}).toArray();


        return publicationsDocument;

    }

}