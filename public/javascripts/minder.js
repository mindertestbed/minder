function executeScripts(target) {
  target.find("script").each(function (i) {
    eval($(this).text());
  });
}

function createFormDialog(elm, sourceUrl, action, dialogId, titl, target, w, h) {
  if (typeof(w) === 'undefined') w = '50%';
  if (typeof(h) === 'undefined') h = '500';

  var frm = $("#" + dialogId + " > form");
  elm.on('click', function (event) {
    event.stopPropagation();

    var dialog = $("#" + dialogId).dialog({
      autoOpen: false,
      title: titl,
      modal: true,
      buttons: {
        "Ok": function () {
          if (typeof(target) === 'undefined' || target == null) {
            dialog.dialog("close");
          } else {
            $.ajax({
              type: frm.attr('method'),
              url: frm.attr('action'),
              data: frm.serialize(),
              success: function (data) {
                target[0].innerHTML = data;
                executeScripts(target);
                dialog.dialog("close");
              },
              error: function (jqXHR, textStatus, errorMessage) {
                frm[0].innerHTML = jqXHR.responseText;
                frm[0].reset();
              }
            });
          }
        },
        Cancel: function () {
          dialog.dialog("close");
        }
      },
      close: function () {
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
    dialog.dialog("open", titl);
  });
}


function createMultipartFormDialog(elm, sourceUrl, action, dialogId, titl, target, w, h) {
  if (typeof(w) === 'undefined') w = '50%';
  if (typeof(h) === 'undefined') h = '500';

  //
  var frm = $("#" + dialogId + " > form");
  elm.on('click', function (event) {
    event.stopPropagation();
    var dialog = $("#" + dialogId).dialog({
      autoOpen: false,
      height: h,
      width: w,
      title: titl,
      modal: true,
      buttons: {
        "Ok": function () {
          var processedData = new FormData(frm[0]);
          frm.html("<div align='center'><i>Sending Data...</i></div>")
          $.ajax({
            type: frm.attr('method'),
            url: frm.attr('action'),
            data: processedData,
            contentType: false,
            processData: false,
            success: function (data) {
              target.html(data);
              dialog.dialog("close");
            },
            error: function (jqXHR, textStatus, errorMessage) {
              frm.html(jqXHR.responseText);
              frm[0].reset();
            }
          });
        },
        Cancel: function () {
          dialog.dialog("close");
        }
      },
      close: function () {
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
    dialog.dialog("open", titl);
  });
}

function deleteWithDialog(elm, action, dialog, title, category, item, target) {
  var deleteUrl;

  //select all deletegroup divs and register delete event on them.
  elm.on('click', function () {
    var deleteDialog = dialog.dialog({
      resizable: false,
      height: 200,
      width: "50%",
      title: title,
      autoOpen: false,
      modal: true,
      buttons: {
        "Delete": function () {
          $.ajax({
            type: 'GET',
            url: action,
            success: function (data) {
              target.html(data);
            },
            error: function (jqXHR, textStatus, errorMessage) {
              showError(jqXHR.responseText);
            }
          });

          $(this).dialog("close");
        },
        Cancel: function () {
          $(this).dialog("close");
        }
      }
    });

    dialog.find("span.itemtype")[0].innerHTML = category;
    dialog.find("span.itemname")[0].innerHTML = item;
    deleteDialog.dialog("open");
  });
}

function showError(data) {
  var dialog = $("#errorDialog").dialog({
    resizable: false,
    height: "600",
    width: "800",
    autoOpen: true,
    modal: true,
    buttons: {
      'Ok': function () {
        $(this).dialog("close");
      }
    }
  });
  dialog[0].innerHTML = data;
}

function showReportDialog(dialogId, id) {
  var dialog = $("#" + dialogId).dialog({
    autoOpen: true,
    title: "Select report type",
    modal: true,
    buttons: {
      "Ok": function () {
        if ($("#isPdf").is(':checked')) {
          window.location.href = "/viewReport?testRunId=" + id + "&type=pdf";
        } else {
          window.location.href = "/viewReport?testRunId=" + id + "&type=xml";
        }
      },
      Cancel: function () {
        dialog.dialog("close");
      }
    },
    close: function () {
    }
  });
}


function deleteWithDialog2(action, dialog, title, category, item) {
  var deleteUrl;
  var deleteDialog = dialog.dialog({
    resizable: false,
    height: 200,
    width: "50%",
    title: title,
    autoOpen: false,
    modal: true,
    buttons: {
      "Delete": function () {
        $.ajax({
          type: 'GET',
          url: action,
          success: function (data) {
            location.reload()
          },
          error: function (jqXHR, textStatus, errorMessage) {
            showError(jqXHR.responseText);
          }
        });

        $(this).dialog("close");
      },
      Cancel: function () {
        $(this).dialog("close");
      }
    }
  });

  dialog.find("span.itemtype")[0].innerHTML = category;
  dialog.find("span.itemname")[0].innerHTML = item;
  deleteDialog.dialog("open");
}


function simpleAjaxGet(dialogId, url, title, message) {
  var dlgSel = $("#" + dialogId)
  dlgSel[0].innerHTML = message

  var dialog = dlgSel.dialog({
    autoOpen: false,
    title: title,
    modal: true,
    buttons: {
      "Ok": function () {
        $.ajax({
          type: 'GET',
          url: url,
          success: function (data) {
            dialog.dialog("close");
          },
          error: function (jqXHR, textStatus, errorMessage) {
            showError(jqXHR.responseText)
            dialog.dialog("close");
          }
        });
      },
      Cancel: function () {
        dialog.dialog("close");
      }
    }
  });

  dialog.dialog("open", title);
}

/**
 * Added function for play ajax navigation
 */
function ajaxRouteGet(playAjaxObject, params, successTarget, failTarget) {
  if (!isArray(params))
    params = [params];

  if (typeof successTarget === 'undefined' || successTarget === null) {
    showError("The success function cannot be empty")
    return;
  }

  var successFunctionDelegate = successTarget;
  //if the user provided a JQuery object, then use its html function
  if (!isFunction(successTarget)) {
    //is it a dialog
    if (typeof successTarget.repaint === 'function') {
      //yes
      successTarget.repaint("")
      spin(successTarget.contentPane[0])
      successFunctionDelegate = function (data) {
        successTarget.contentPane[0].spinner.stop()
        successTarget.repaint(data)
      }
    } else if (typeof successTarget.html === 'function') {
      //no its not a dialog, but it has a html() function
      spin(successTarget[0])
      successFunctionDelegate = function (data) {
        successTarget[0].spinner.stop()
        successTarget.html(data)
      }
    } else {
      showError("The success function parameter must be either a function, a closable dialog or an object that has 'html()' function")
      return;
    }
  }

  playAjaxObject.apply(null, params).ajax()
    .done(
    function (data) {
      successFunctionDelegate(data)
    })
    .fail(
    function (data) {
      if (typeof failTarget === 'undefined' || failTarget === null) {
        showError(data.responseText)
      } else {
        failTarget(data.responseText);
      }
    }
  )
}


//check if my object is an array or not
function isArray(myObject) {
  return myObject.constructor.toString().indexOf("Array") > -1;
}


//check if my object is a function
function isFunction(myObject) {
  return myObject.constructor.toString().indexOf("Function") > -1;
}

//check if my object is a jquery object
function isJQuery(myObject) {
  return myObject.constructor.toString().indexOf("return new jQuery.fn.init") > -1;
}


function decorateAsClosable(target, title, close) {
  //check if I am already closable
  if (target.hasClass('closable')) return target[0].closableObject;

  target.empty();
  target.append(
    '<table class="header"><tr><td class="tabexpandNoBorder">' +
    '<div style="padding-left:10px;">' +
    title + '</div></td>' +
    '<td class="tabshrink closableButton"><h4><a href="#" style="padding: 10px;" onclick="closeClosableParent(event, this);">' +
    'X' +
    '</a></hd></td></tr></table>')

  target.addClass('closable')
  target.addClass('bevel2')
  target.append('<div class="paddedContentPane"></div>');
  target.contentPane = target.children().last();
  target.repaint = function(data){
    target.contentPane.html(data)
  };
  target[0].closeFunction = close;
}


function closeClosableParent(event, theLink) {
  event.preventDefult();
  event.stopPropogation();
  var theParent = $(theLink).parents(".closable");
  theParent.hide();
  if (theParent[0].closeFunction != null) {
    theParent[0].closeFunction();
  }
}

function loadingDiv() {
  return '<div align="center">Loading...</div>';
}

/**
 * This function takes a NON JQUERY elemnt. So if you will
 * provde a JQUERY object please do this: target[0]
 * @param target
 */
function spin(target) {
  var opts = {
    lines: 13 // The number of lines to draw
    , length: 28 // The length of each line
    , width: 14 // The line thickness
    , radius: 42 // The radius of the inner circle
    , scale: 0.5 // Scales overall size of the spinner
    , corners: 1 // Corner roundness (0..1)
    , color: '#000' // #rgb or #rrggbb or array of colors
    , opacity: 0.25 // Opacity of the lines
    , rotate: 0 // The rotation offset
    , direction: 1 // 1: clockwise, -1: counterclockwise
    , speed: 1 // Rounds per second
    , trail: 60 // Afterglow percentage
    , fps: 20 // Frames per second when using setTimeout() as a fallback for CSS
    , zIndex: 2e9 // The z-index (defaults to 2000000000)
    , className: 'spinner' // The CSS class to assign to the spinner
    , top: '50%' // Top position relative to parent
    , left: '50%' // Left position relative to parent
    , shadow: false // Whether to render a shadow
    , hwaccel: false // Whether to use hardware acceleration
    , position: 'absolute' // Element positioning
  }

  target.spinner = new Spinner(opts).spin(target);
}