const express = require('express');
const adminSessionRouter = express.Router();
adminSessionRouter.use(function (req, res, next) {
        console.log("adminSessionRouter");
        if (req.session.user === "admin@email.com")  // dejamos correr la petición
        {
            //si es admin, solo puede acceder a listado de usuarios


            res.redirect("/users/list");

        }
        else
        {

            next();
        }
    }
)
;
module.exports = adminSessionRouter;