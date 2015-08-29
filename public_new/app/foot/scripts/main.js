function issue() {
    dpd.cares.post({}).then(function (care) {
        console.log(care);
    })
}

dpd.cares.get({}).then(function (cares) {
    var $tbody = $('table tbody');
    cares.forEach(function (care) {
        console.log(care);
        var tr = document.createElement('tr');
        tr.innerHTML = '<tr><td><a data-uri="spotify:care:' + care.id + '">Foot care</a>    </td></tr>';
        $tbody.append(tr);
    });
});