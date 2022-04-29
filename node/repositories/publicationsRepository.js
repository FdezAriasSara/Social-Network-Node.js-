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
            publicationsOfUser = await publicationsCollection.find({userID: userObjectID}, {publications: 1, _id:0})?.toArray();

            if(publicationsOfUser.length > 0 ){ //Si ya tiene publicaciones, insertamos la nueva
                await publicationsCollection.update({userID: userObjectID}, {$push: { publications: publication } } );
            }else{ //Si no tiene publicaciones, creamos el documento
                await publicationsCollection.insert({userID: userObjectID, publications: [publication] });
            }


            const result = await publicationsCollection.insertOne(publication);
            return result.insertedId;
        } catch (error) {
            throw (error);

        }
    },
    findAllPublicationsByAuthorAndPage: async function (authorObjectID, page){

        const limit = 10; //TOTAL_PUBLICATIONS_PER_PAGE
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