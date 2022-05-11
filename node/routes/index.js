var express = require('express');
var router = express.Router();

/* GET home page. */
router.get('/', function(req, res, next) {
    let isLoggedIn= false;
    if(req.session.user != null || req.session.user != undefined) {
        isLoggedIn=true;
    }
        res.render('home.twig', {isLoggedIn: isLoggedIn});
});

module.exports = router;