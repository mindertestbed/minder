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
  if(typeof(w)==='undefined') w = '50%';
  if(typeof(h)==='undefined') h = '500';

  var frm = $( "#" + dialogId + " > form");
  elm.on('click', function(event){
     event.stopPropagation();

     var dialog = $( "#" + dialogId ).dialog({
         autoOpen: false,
         height: h,
         width: w,
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
            executeScripts(frm);
          },
          error: function (jqXHR, textStatus, errorMessage) {
              frm[0].innerHTML = jqXHR.responseText;
          }
     });
     frm[0].innerHTML = "..."
     dialog.dialog( "open", titl );
  });
}


function createMultipartFormDialog(elm, sourceUrl, action, dialogId, titl, target, w, h){
  if(typeof(w)==='undefined') w = '50%';
  if(typeof(h)==='undefined') h = '500';

 //
  var frm = $( "#" + dialogId + " > form");
     elm.on('click', function(event){
     event.stopPropagation();

     var dialog = $( "#" + dialogId ).dialog({
         autoOpen: false,
         height: h,
         width: w,
         title: titl,
         modal: true,
         buttons: {
             "Ok": function(){

               var processedData = new FormData(frm[0]);


               frm[0].innerHTML = "<div align='center'><i>Sending Data...</i></div>"

               $.ajax({
                 type: frm.attr('method'),
                 url: frm.attr('action'),
                 data: processedData,
                 contentType: false,
                 processData: false,
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
     frm[0].setAttribute('enctype', 'multipart/form-data')
     $.ajax({
          type: 'GET',
          url: sourceUrl,
          success: function (data) {
            frm[0].innerHTML = data;
            executeScripts(frm);
          },
          error: function (jqXHR, textStatus, errorMessage) {
              frm[0].innerHTML = jqXHR.responseText;
          }
     });
     frm[0].innerHTML = "<div align='center'><i>Loading...</i></div>"
     dialog.dialog( "open", titl );
  });
}

function deleteWithDialog(elm, action, dialog, title, category, item, target){
    var deleteUrl;

    //select all deletegroup divs and register delete event on them.
    elm.on('click', function( ) {
      var deleteDialog = dialog.dialog({
        resizable: false,
        height:200,
        width:"50%",
        title:title,
        autoOpen: false,
        modal: true,
        buttons: {
          "Delete": function() {
           $.ajax({
            type: 'GET',
            url: action,
            success: function (data) {
              target[0].innerHTML = data;
              executeScripts(target);
            },
            error: function (jqXHR, textStatus, errorMessage) {
                alert(jqXHR.responseText);
            }
           });

          $( this ).dialog( "close" );
          },
          Cancel: function() {
          $( this ).dialog( "close" );
          }
        }
      });

     dialog.find("span.itemtype")[0].innerHTML = category;
     dialog.find("span.itemname")[0].innerHTML = item;
     deleteDialog.dialog( "open" );
    });
}

function showError(data){
  var dialog = $("#errorDialog").dialog({
      resizable: false,
      height:"600",
      width:"800",
      autoOpen: true,
      modal: true,
      buttons: {
        'Ok': function() {
          $( this ).dialog( "close" );
        }
      }
  });
  dialog[0].innerHTML = data;
}

function createRunConfiguration(testCaseId){
  $.ajax({
    type: 'GET',
    url: '/createRunConfigurationForm?testCaseId='+testCaseId,
    success: function (data) {
      showCreateDialog(testCaseId, data)
    },
    error: function (jqXHR, textStatus, errorMessage) {
      showError(jqXHR.responseText);
    }
  });
}

function showCreateDialog(testCaseId, data){
  var frm = $('#mainForm');
  frm[0].innerHTML = data;
  executeScripts(frm);
  var search = "#rcRoot" + testCaseId;
  var parr = $(search);

  var dialog = $('#mainDialog').dialog({
       autoOpen: false,
       height: 500,
       width: 800,
       title: 'Create Run Configuration',
       modal: true,
       buttons: {
           "Create": function(){
             $.ajax({
               type: 'post',
               url: '/doCreateRunConfiguration',
               data: frm.serialize(),
               success: function (data) {
                  parr[0].innerHTML = data;
                  executeScripts(parr);
                  dialog.dialog( "close" );
               },
               error: function (jqXHR, textStatus, errorMessage) {
                  frm[0].innerHTML = jqXHR.responseText;
                  executeScripts(frm);
               }
             });
           },
           Cancel: function() {
             dialog.dialog( "close" );
           }
       }
   });

   dialog.dialog( "open", 'Create Run Configuration' );
}

function editRunConfiguration(testCaseId, id, name){
  //perform ajax to get the edit form
   $.ajax({
      type: 'GET',
      url: '/editRunConfigurationForm?id='+id,
      success: function (data) {
        showEditDialog(testCaseId, id, data, name)
      },
      error: function (jqXHR, textStatus, errorMessage) {
        showError(jqXHR.responseText);
      }
   });
}

function showEditDialog(testCaseId, id, data, title){
  var form = $( "#mainForm" );
  form[0].innerHTML = data;
  executeScripts(form);
  var parent = $("#rcRoot" + testCaseId).parent();

    var dialog = $( "#mainDialog" ).dialog({
       autoOpen: false,
       height: 600,
       width: 1100,
       title: 'Run Configuration' + title,
       modal: true,
       buttons: {
           "Save": function(){
             $.ajax({
               type: 'post',
               url: '/doEditRunConfiguration',
               data: form.serialize(),
               success: function (data) {
                  parent[0].innerHTML = data;
                  executeScripts(parent);
                  dialog.dialog( "close" );
               },
               error: function (jqXHR, textStatus, errorMessage) {
                   form[0].innerHTML = jqXHR.responseText;
                   form[0].reset();
               }
             });
           },
           Cancel: function() {
             dialog.dialog( "close" );
           }
       },
       close: function() {
         form[0].reset();
       }
   });

   dialog.dialog( "open", title );
}


function displayRunConfiguration(id, name, userId){
   //perform ajax to get the run dialog
   $.ajax({
      type: 'GET',
      url: '/displayRunConfiguration?id='+id,
      success: function (data) {
        showRunDialog(id, data, name, userId)
      },
      error: function (jqXHR, textStatus, errorMessage) {
        showError(jqXHR.responseText);
      }
   });
}

function showRunDialog(runConfigurationId, data, title, userId){
  var runDialog = $( "#runDialog" );
  runDialog[0].innerHTML = data
  executeScripts(runDialog);

  //here we have the handle to the inner table where the test status is shown.
  //we will update it during the test.
  //see: runConfigurationDetailView>div #testStatus.
  var targetStatusDiv = $('#testStatus')

  var dialog = runDialog.dialog({
     autoOpen: false,
     height: 650,
     width: 1200,
     title: 'Run Configuration' + title,
     modal: true,
     buttons: {
       "Run": function() {
          runTest(runDialog, runConfigurationId, targetStatusDiv, userId)
       },
       Cancel: function() {
         dialog.dialog( "close" );
       }
     }
   });

   dialog.dialog( "open", title );
}

function runTest(runDialog, runConfigurationId, targetStatusDiv, userId){
   setTimeout(function(){
      $.ajax({
        type: 'GET',
        url: '/runOrSyncTest?id=' + runConfigurationId + '&userId=' + userId ,
        success: function(data){


          if(data.indexOf("TESTFINISHED") != -1){
            alert(data);
          }else if (data.indexOf("TESTFAILED") != -1){
            alert(data)
          } else{
             targetStatusDiv[0].innerHTML = data;
             executeScripts(targetStatusDiv);
             runTest(runDialog, runConfigurationId, targetStatusDiv, userId);
          }
        },
        error: function (jqXHR, textStatus, errorMessage) {
          showError(jqXHR.responseText);
        }
      });
  }, 1000);
}


function deleteRunConfiguration(testCaseId, idd, name){
  var parent = $("#rcRoot" + testCaseId).parent();
    var deleteDialog = $("#dialog-confirm").dialog({
      resizable: false,
      height:200,
      width:"50%",
      title:'Delete Run Configuration',
      autoOpen: false,
      modal: true,
      buttons: {
        "Delete": function() {
         $.ajax({
            type: 'GET',
            url: '/doDeleteRunConfiguration?id=' + idd,
            success: function (data) {
              parent[0].innerHTML = data;
              executeScripts(parent);
            },
            error: function (jqXHR, textStatus, errorMessage) {
              showError(jqXHR.responseText);
            }
         });
         $( this ).dialog( "close" );
        },
        Cancel: function() {
        $( this ).dialog( "close" );
        }
      }
    });

   deleteDialog.find("span.itemtype")[0].innerHTML = 'run configuration';
   deleteDialog.find("span.itemname")[0].innerHTML = name;
   deleteDialog.dialog( "open" );
}


