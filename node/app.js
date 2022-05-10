var createError = require('http-errors');
var express = require('express');
var path = require('path');
var cookieParser = require('cookie-parser');
var logger = require('morgan');

let bodyParser = require("body-parser");

const {MongoClient} = require("mongodb");

////////////////MODIFICAR ESTO EN FUNCION DE NUESTROS JS//////////////////////
//let songsRepository = require("./repositories/songsRepository");          //
//var indexRouter = require('./routes/index');                              //
//const usersRepository = require("./repositories/usersRepository.js");     //
//const commentsRepository = require("./repositories/commentsRepository")   //
////////////////MODIFICADO: //////////////////////////////////////////////////
var indexRouter = require('./routes/index');                                //
const usersRepository = require('./repositories/usersRepository')           /////
const publicationsRepository = require('./repositories/publicationsRepository')
const messagesRepository = require('./repositories/messagesRepository')         //                                                                            //
// //////////////////////////////////////////////////////////////////////////////


const url = 'mongodb+srv://admin:admin@cluster0.6uext.mongodb.net/myFirstDatabase?retryWrites=true&w=majority';

//EXPRESS
var app = express();

let rest = require('request');
app.set('rest', rest);


//Access-Control-Allow-origin
app.use(function (req, res, next) {
    res.header("Access-Control-Allow-Origin", "*");
    res.header("Access-Control-Allow-Credentials", "true");
    res.header("Access-Control-Allow-Methods", "POST, GET, DELETE, UPDATE, PUT");
    res.header("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept, token"); // Debemos especificar todas las headers que se aceptan. Content-Type , token
    next();
});

//JWT
let jwt = require('jsonwebtoken');
app.set('jwt', jwt);

let expressSession = require('express-session');
app.use(expressSession(
    {secret: 'abcdefg', resave: true, saveUninitialized: true}
));

let crypto = require("crypto");

let fileUpload = require("express-fileupload");
app.use(fileUpload({
    limits: {fileSize: 50 * 1024 * 1024},
    createParentPath: true
}));
app.set('uploadPath', __dirname);
app.set('clave', 'abcdefg');
app.set('crypto', crypto);


app.use(bodyParser.json());
app.use(bodyParser.urlencoded({extended: true}));

app.set('connectionStrings', url);

////////////////MODIFICAR ESTO EN FUNCION DE NUESTROS JS////////////////////////////////////////////////////7
//const userSessionRouter = require('./routes/userSessionRouter');
//const userAudiosRouter = require('./routes/userAudiosRouter');
//app.use("/songs/add", userSessionRouter);
//app.use("/songs/**", userSessionRouter);
//app.use("/publications", userSessionRouter);
//app.use("/songs/buy", userSessionRouter);
//app.use("/purchases", userSessionRouter);
//app.use("/audios/", userAudiosRouter);
//app.use("/shop/", userSessionRouter)


//const userAuthorRouter = require('./routes/userAuthorRouter');
//app.use("/songs/edit", userAuthorRouter);
//app.use("/songs/delete", userAuthorRouter);

//const userTokenRouter = require('./routes/userTokenRouter');
//app.use("/api/v1.0/songs/", userTokenRouter);

//songsRepository.init(app, MongoClient);
//commentsRepository.init(app, MongoClient)

//require("./routes/songs.js")(app, songsRepository, commentsRepository);
//require("./routes/authors.js")(app);
//require("./routes/comments")(app, commentsRepository)
//require("./routes/api/songsAPIv1.0.js")(app, songsRepository, usersRepository)

//usersRepository.init(app, MongoClient);
//require("./routes/users.js")(app, usersRepository);


////////////////MODIFICADO: ////////////////////////////////////////////////////////////////////////////////////
const userSessionRouter = require('./routes/userSessionRouter');
const userTokenRouter = require('./routes/userTokenRouter');
const adminSessionRouter = require("./routes/adminSessionRouter")

usersRepository.init(app, MongoClient);
publicationsRepository.init(app,MongoClient,usersRepository);
messagesRepository.init(app,MongoClient)



app.use("/publications/**", userSessionRouter);

//No especifico /api/users/login porque para acceder no es necesario token
app.use("/api/friends/list", userTokenRouter);
app.use("/api/message/add", userTokenRouter);
app.use("/api/conversation", userTokenRouter);
app.use("/publications/**", userSessionRouter, adminSessionRouter);
app.use("/users/list", userSessionRouter);
app.use("/friends/*", userSessionRouter,adminSessionRouter);

require("./routes/users.js")(app,usersRepository, publicationsRepository, messagesRepository);
require("./routes/publications.js")(app, usersRepository, publicationsRepository);//                                                              //
require("./routes/api/chatAPI")(app, publicationsRepository, usersRepository,messagesRepository);
require("./routes/friends.js")(app,usersRepository);
                                   ////
//                                                                                //
////////////////////////////////////////////////////////////////////////////////////

// view engine setup
app.set('views', path.join(__dirname, 'views'));
app.set('view engine', 'twig');

app.use(logger('dev'));
app.use(express.json());
app.use(express.urlencoded({extended: false}));
app.use(cookieParser());
app.use(express.static(path.join(__dirname, 'public')));

app.use('/', indexRouter);

// catch 404 and forward to error handler
app.use(function (req, res, next) {
    next(createError(404));
});

// error handler
app.use(function (err, req, res, next) {
    // set locals, only providing error in development
    console.log("Se ha producido un error: " + err);
    res.locals.message = err.message;
    res.locals.error = req.app.get('env') === 'development' ? err : {};

    // render the error page
    res.status(err.status || 500);
    res.render('error');
});


module.exports = app;
