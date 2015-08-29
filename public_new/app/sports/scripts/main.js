window.onargumentschanged = function (event) {
    var arguments = event.data;
    console.log(dpd.sports);
    dpd.sports.get(function (sports) {
        console.log(sports);
        fill_table(sports);
    });
}

function fill_table(sports) {
    var $tbody = $('table tbody');
    $tbody.html("");

    for (var i = 0; i < sports.length; i++) {
        var sport = sports[i];
        console.log(sport);
        var tr = document.createElement('tr');
        
        
        tr.innerHTML += '<td><a data-uri="bungalow:sport:' + sport.id + '">' + sport.name + '</a></td>';
        
        
        tr.innerHTML += '<td>' + sport.aquatic + '</td>';
        
        tr.innerHTML += '<td><a class="btn btn-danger" onclick="delete(\'' + sport.id + '\')">Delete</a></td>';
        tr.innerHTML += '<td><a class="btn btn-default" onclick="delete(\'' + sport.id + '\')">View</a></td>';
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

    var sport = {
        
        'name': $('#field_name').val(),
        
        'aquatic': $('#field_aquatic').val(),
        
    };
    console.log(sport);
    dpd.sports.post(sport, function (err, done) {
        dpd.sports.get(function (sports) {
            fill_table(sports);
        });
    });
    $('#form').fadeOut();
    return false;
}