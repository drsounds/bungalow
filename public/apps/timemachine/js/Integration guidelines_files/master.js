$(document).ready(function(){

	$("#main h2").each(function(i) {
		var current = $(this);
		var headingID = current.attr("id");
	    current.attr("id", headingID);
	    $("#sidebar ul#toc").append("<li><a href='#" + headingID + "' >" + 
	        current.html() + "</a></li>");
	});
	/*
	function goToByScroll(id){
	      $('html,body').animate({scrollTop: $("#"+id).offset().top},'slow');
	}
	$("#sidebar ul li a").click( function() {
		var current = $(this).attr('id');
		goToByScroll(current);
	});
	*/
});
  
  
  