window.onargumentschanged = function (event) {
    console.log(event.data);
    $('#hashtag').html('#' + event.data[0]);
}