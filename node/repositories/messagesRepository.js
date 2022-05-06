const {ObjectId} = require("mongodb");
module.exports = {
    mongoClient: null,
    app: null,
    init: function ( app, mongoClient ){
        this.mongoClient = mongoClient;
        this.app = app;
    },
    createMessage: async function (message) {

        try {
            const client = await this.mongoClient
                .connect(this.app.get('connectionStrings'));
            const database = client.db("redsocial");
            const collectionName = 'messages';
            const usersCollection = database.collection(collectionName);

           let messageWithDate = {
               ...message,
               date: new Date()
           }
            const result = await usersCollection.insertOne(messageWithDate);
            return result.insertedId;
        } catch (error) {
            throw (error);

        }

    }
}