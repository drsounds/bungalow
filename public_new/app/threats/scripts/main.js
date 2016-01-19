window.onargumentschanged = function (event) {
    var arguments = event.data;
    console.log(dpd.threats);
    dpd.threats.get(function (threats) {
        console.log(threats);
        fill_table(threats);
    });
}

function fill_table(threats) {
    var $tbody = $('table tbody');
    $tbody.html("");

    for (var i = 0; i < threats.length; i++) {
        var threat = threats[i];
        console.log(threat);
        var tr = document.createElement('tr');
        tr.innerHTML += '<td><i class="fa fa-bug"></i></a></td>';
        
        
        tr.innerHTML += '<td>' + threat.code + '</td>';
        
        tr.innerHTML += '<td><a class="btn btn-danger" onclick="delete(\'' + threat.id + '\')">Delete</a></td>';
        tr.innerHTML += '<td><a class="btn btn-default" data-uri="bungalow:threat:' + threat.id + '">View</a></td>';
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

    var threat = {
        
        'code': $('#field_code').val(),
        
    };
    console.log(threat);
    dpd.threats.post(threat, function (err, done) {
        dpd.threats.get(function (threats) {
            fill_table(threats);
        });
    });
    $('#form').fadeOut();
    return false;
}