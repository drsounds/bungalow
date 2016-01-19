window.onargumentschanged = function (event) {
    var arguments = event.data;
    console.log(dpd.bungalows);
    dpd.bungalows.get(function (bungalows) {
        console.log(bungalows);
        fill_table(bungalows);
    });
}

function fill_table(bungalows) {
    var $tbody = $('table tbody');
    $tbody.html("");

    for (var i = 0; i < bungalows.length; i++) {
        var bungalow = bungalows[i];
        console.log(bungalow);
        var tr = document.createElement('tr');
        tr.innerHTML += '<td><i class="fa fa-home"></i></a></td>';
        
        
        tr.innerHTML += '<td><a data-uri="bungalow:bungalow:' + bungalow.id + '">' + bungalow.name + '</a></td>';
        
        tr.innerHTML += '<td><a class="btn btn-danger" onclick="delete(\'' + bungalow.id + '\')">Delete</a></td>';
        tr.innerHTML += '<td><a class="btn btn-default" data-uri="bungalow:bungalow:' + bungalow.id + '">View</a></td>';
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

    var bungalow = {
        
        'name': $('#field_name').val(),
        
    };
    console.log(bungalow);
    dpd.bungalows.post(bungalow, function (err, done) {
        dpd.bungalows.get(function (bungalows) {
            fill_table(bungalows);
        });
    });
    $('#form').fadeOut();
    return false;
}