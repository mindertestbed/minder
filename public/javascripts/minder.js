function minderAccordion(id){
  var ogl = "#" + id + " > div";

  //hide the contents
  $(ogl + ".content").hide();

  var titles = $(ogl + ".title");

  titles.each(function( index ) {
    $( this ).addClass('thin-' + (index % 2));
  });


  var triggers = $("#" + id + " .trigger");

  triggers.click(function(){
    //get the parent that has class title.

    var parent = $(this).closest('.title');
    if(parent.hasClass("closed")){
      parent.next().slideUp();
      titles.slideDown();
      parent.removeClass("closed");
    } else{
      parent.addClass("closed");
      titles.slideUp();
      parent.slideDown();
      parent.next().slideDown();
    }
  });
}

function createFormDialog(id, sourceUrl, action, dialogId, titl, w, h){
  createFormDialog2($("#" + id), sourceUrl, action, dialogId, titl, w, h);
}

function createFormDialog2(elm, sourceUrl, action, dialogId, titl, w, h){
  ww = typeof w !== 'undefined' ? w : '50%';
  hh = typeof w !== 'undefined' ? w : '500';
  var frm = $( "#" + dialogId + " > form");
  elm.on('click', function(event){
     event.stopPropagation();

     var dialog = $( "#" + dialogId ).dialog({
         autoOpen: false,
         height: hh,
         width: ww,
         title: titl,
         modal: true,
         buttons: {
             "Ok": function(){
               //frm[0].innerHTML = "<div align='center'>Sending Request..</div>"
               $.ajax({
                 type: frm.attr('method'),
                 url: frm.attr('action'),
                 data: frm.serialize(),
                 success: function (data) {
                    window.location.reload();
                 },
                 error: function (jqXHR, textStatus, errorMessage) {
                     frm[0].innerHTML = jqXHR.responseText;
                     frm[0].reset();
                 }
               });
             },
             Cancel: function() {
               dialog.dialog( "close" );
             }
         },
         close: function() {
           frm[0].reset();
         }
       });

     frm[0].setAttribute('action', action);
     $.ajax({
          type: 'GET',
          url: sourceUrl,
          success: function (data) {
            frm[0].innerHTML = data;
          },
          error: function (jqXHR, textStatus, errorMessage) {
              frm[0].innerHTML = jqXHR.responseText;
          }
     });
     dialog.dialog( "open", titl );
  });
}


