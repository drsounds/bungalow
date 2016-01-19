require(['$api/models', '$api/echonest'], function (models, Echonest) {
    models.application.addEventListener('argumentschanged', function (addEventListener) {
        console.log(event);
    });
});