const proxy = require('http-proxy-middleware');

module.exports = function (app) {
    app.use(
        proxy(
            [
                '/bgupload/dtres/backend/picture/upload',
                "/business-aggregate-admin"
            ],
            {
                target: 'http://localhost:8888/',
                changeOrigin: true,
            }
        )
    );
};
