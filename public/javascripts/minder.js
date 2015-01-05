function minderAccordion(selector){
  var ogl = selector + " > div";

  //hide the contents
  $(ogl + ".content").hide();

  var titles = $(ogl + ".title");

  var triggers = $(ogl + ".title .trigger");

  triggers.each(function( index ) {
      $(this).addClass('hand');
  });

  triggers.click(function(){
    //get the parent that has class title.
    var parent = $(this).closest('.title');

    var nxt = parent.next();
    if(parent.hasClass("closed")){
      nxt.slideUp();
      titles.slideDown();
      parent.removeClass("closed");
    } else{
      parent.addClass("closed");
      titles.slideUp();
      parent.slideDown();
      nxt.slideDown();
    }
  });
}

function executeScripts(target){
  target.find("script").each(function(i) {
      eval($(this).text());
  });
}

function createFormDialog(elm, sourceUrl, action, dialogId, titl, target, w, h){
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
               $.ajax({
                 type: frm.attr('method'),
                 url: frm.attr('action'),
                 data: frm.serialize(),
                 success: function (data) {
                    target[0].innerHTML = data;

                    executeScripts(target);

                    dialog.dialog( "close" );
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


