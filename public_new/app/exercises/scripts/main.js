window.onargumentschanged = function (event) {
    var arguments = event.data;
    console.log(dpd.exercises);
    dpd.exercises.get(function (exercises) {
        console.log(exercises);
        fill_table(exercises);
    });
}

function fill_table(exercises) {
    var $tbody = $('table tbody');
    $tbody.html("");

    for (var i = 0; i < exercises.length; i++) {
        var exercise = exercises[i];
        console.log(exercise);
        var tr = document.createElement('tr');
        
        
        tr.innerHTML += '<td>' + exercise.time + '</td>';
        
        
        tr.innerHTML += '<td><a data-uri="bungalow:bungalow:' + exercise.bungalow.id + '">' + exercise.bungalow.name + '</a></td>';
        
        
        tr.innerHTML += '<td>' + exercise.distance + '</td>';
        
        tr.innerHTML += '<td><a class="btn btn-danger" onclick="delete(\'' + exercise.id + '\')">Delete</a></td>';
        tr.innerHTML += '<td><a class="btn btn-default" data-uri="bungalow:exercise:' + exercise.id + '">View</a></td>';
        $tbody.append(tr);
    }
}

function show_add(event) {
    $('#form').popover({
        'anchor': '#addbtn'
    });
}

function add(event) {
    event.preventDefault();

    var exercise = {
        
        'time': $('#field_time').val(),
        
        'bungalow': $('#field_bungalow').val(),
        
        'distance': $('#field_distance').val(),
        
    };
    console.log(exercise);
    dpd.exercises.post(exercise, function (err, done) {
        dpd.exercises.get(function (exercises) {
            fill_table(exercises);
        });
    });
    $('#form').fadeOut();
    return false;
}