window.onargumentschanged = function (event) {
    var arguments = event.data;
    console.log(dpd.bungalows);
    dpd.bungalows.get(arguments[0]).then (function (bungalow) {
        console.log(bungalow);
        $('bungalow-header name').html(bungalow.name);
  

    
            dpd.exercises.get({'bungalow': bungalow.exercise.id}, function (exercises) {
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