window.onargumentschanged = function (event) {
    var arguments = event.data;
    console.log(dpd.sports);
    dpd.sports.get(arguments[0]).then (function (sport) {
        console.log(sport);
        $('bungalow-header name').html(sport.name);
  

    
            dpd.exercises.get({'sport': sport.id}, function (exercises) {
                var $exerciseCollection = $('#collection_exercise');
                for (var i = 0; i < exercises.length; i++) {
                    var exercise = exercises[i];

                    var elm = document.createElement('bungalow-item');
                    $elm = $(elm);
                    $elm.html('<name>' + exercise.name + '</name>');
                    $elm.attr('uri', 'bungalow:exercise:' + exercise.id);
                    $elm.attr('type', 'exercise');
                    $exerciseCollection.append($elm);
                }
            });
            
       });
}