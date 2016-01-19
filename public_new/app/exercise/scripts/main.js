window.onargumentschanged = function (event) {
    var arguments = event.data;
    console.log(dpd.exercises);
    dpd.exercises.get(arguments[0]).then (function (exercise) {
        console.log(exercise);
        $('bungalow-header name').html(exercise.name);
  

    
       });
}