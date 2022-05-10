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
            const messagesCollection = database.collection(collectionName);

           let messageWithDate = {
               ...message,
               date: new Date()
           }
            const result = await messagesCollection.insertOne(messageWithDate);
            client.close();
            return result.insertedId;
        } catch (error) {
            throw (error);

        }

    },

    getMessagesFromTo: async function (fromEmail, toEmail){

        try {
            const client = await this.mongoClient
                .connect(this.app.get('connectionStrings'));
            const database = client.db("redsocial");
            const collectionName = 'messages';
            const messagesCollection = database.collection(collectionName);


            const messages = await messagesCollection.find({senderEmail: fromEmail, receiverEmail: toEmail}, {}).toArray();
            client.close();
            return messages;
        } catch (error) {
            throw (error);

        }

    },
    deleteMessages:async function (filter, options) {
        try {
            const client = await this.mongoClient.connect(this.app.get('connectionStrings'));
            const database = client.db("redsocial");
            const collectionName = 'messages';
            const messagesCollection = database.collection(collectionName);

            const result = await messagesCollection.deleteMany(filter, options);
            client.close();
            return result;
        } catch (error) {
            throw (error);
        }
    }
}