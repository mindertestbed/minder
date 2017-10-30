function minderAccordion(selector) {
  var ogl = selector + " > div";
  //hide the contents
  $(ogl + ".content").hide();

  var titles = $(ogl + ".title");

  var triggers = $(ogl + ".title .trigger");

  triggers.each(function (index) {
    $(this).addClass('hand');
  });

  triggers.click(function () {
    //get the parent that has class title.
    var parent = $(this).closest('.title');

    var nxt = parent.next();
    if (parent.hasClass("closed")) {
      nxt.slideUp();
      titles.slideDown();
      parent.removeClass("closed");
    } else {
      parent.addClass("closed");
      titles.slideUp();
      parent.slideDown();
      nxt.slideDown();
    }
  });
}


function executeScripts(target) {
  target.find("script").each(function (i) {
    eval($(this).text());
  });
}

/**
 * Creates a form rendering its contents from sourceUrl and
 * setting its action (post) to action
 * @param sourceUrl
 * @param action
 * @param titl
 * @param target
 * @param w
 * @param h
 */
function createFormDialog(sourceUrl, action, titl, target, multiPart, w, h) {
  if (typeof(w) === 'undefined' || w === null) w = '50%';
  if (typeof(h) === 'undefined' || h === null) h = '500';
  if (multiPart === undefined || multiPart === null) multiPart = false;

  var frm = $("#mainDialog > form");
  var vnt = $(window.event);
  vnt.stop();

  var dialog = $("#mainDialog").dialog({
    autoOpen: false,
    title: titl,
    height: h,
    width: w,
    modal: true,
    buttons: {
      "Ok": function () {
        if (typeof(target) === 'undefined' || target == null) {
          dialog.dialog("close");
        } else {
          var ajaxObject = {
            type: frm.attr('method'),
            url: frm.attr('action'),
            success: function (data) {
              target.html(data);
              frm.html("")
              dialog.dialog("close");
            },
            error: function (jqXHR, textStatus, errorMessage) {
              frm.html(jqXHR.responseText)
            },
          }

          if (multiPart) {
            ajaxObject.data = new FormData(frm[0]);
            ajaxObject.contentType = false;
            ajaxObject.processData = false;
          } else {
            ajaxObject.data = frm.serialize();
          }

          $.ajax(ajaxObject);

        }
      },
      Cancel: function () {
        dialog.dialog("close");
      }
    },
    close: function () {
      frm.html("")
      frm[0].removeAttribute('action')
      frm[0].removeAttribute('enctype')
    }
  });

  frm[0].setAttribute('action', action);
  if (multiPart)
    frm[0].setAttribute('enctype', 'multipart/form-data');

  spin(frm[0])
  dialog.dialog("open", titl);

  $.ajax({
    type: 'GET',
    url: sourceUrl,
    success: function (data) {
      stopSpin(frm[0])
      frm.html(data);
    },
    error: function (jqXHR, textStatus, errorMessage) {
      dialog.dialog('close')
      showError(jqXHR.responseText);
    }
  });

}

function deleteWithDialog(action, dialog, title, category, item, target) {
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
            if (target !== undefined) {
              if(isFunction(target)){
                target()
              } else if(isJQuery(target)) {
                target.html(data);
              }
            } else {
              location.reload()
            }
          },
          error: function (jqXHR, textStatus, errorMessage) {
            dialog.dialog('close')
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

  dialog.find("span.itemtype").html(category);
  dialog.find("span.itemname").html(item);
  deleteDialog.dialog("open");
}

function simpleAjaxGet(dialogId, url, title, message) {
  var dlgSel = $("#" + dialogId)
  dlgSel.html(message)

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
  if (typeof successTarget === 'undefined' || successTarget === null) {
    successTarget = $('.ajaxContent')
  }

  var successFunctionDelegate = successTarget;
  var spinner = null
  //if the user provided a JQuery object, then use its html function
  if (!isFunction(successTarget)) {
    //is it a dialog
    if (typeof successTarget.repaint === 'function') {
      //yes
      successTarget.repaint("")
      spin(successTarget.contentPane[0])
      spinner = successTarget.contentPane[0].spinner
      successFunctionDelegate = function (data) {
        spinner.stop()
        successTarget.repaint(data)
      }
    } else if (typeof successTarget.html === 'function') {
      //no its not a dialog, but it has a html() function
      spin(successTarget[0])
      spinner = successTarget[0].spinner
      successFunctionDelegate = function (data) {
        spinner.stop()
        successTarget.html(data)
      }
    } else {
      showError("The success function parameter must be either a function, a closable dialog or an object that has 'html()' function")
      return;
    }
  }


  var ajaxObject = {}

  //check if we are a form
  if (typeof  playAjaxObject.serialize === 'function') {
    //this is a form
    ajaxObject.data = playAjaxObject.serialize()
    ajaxObject.type = playAjaxObject.attr('method')
    ajaxObject.url = playAjaxObject.attr('action')
  } else {
    if (!isArray(params)) {
      params = [params];
    }
    ajaxObject.url = playAjaxObject.apply(null, params).url
  }

  $.ajax(ajaxObject).done(function (data) {
    successFunctionDelegate(data)
  }).fail(function (data) {
    if (spinner != null)
      spinner.stop()

    if (typeof failTarget === 'undefined' || failTarget === null) {
      showError(data.responseText)
    } else {
      if (isFunction(failTarget)) {
        failTarget(data.responseText)
      } else {
        failTarget.html(data.responseText);
      }
    }
  })
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
  target.repaint = function (data) {
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

function stopSpin(target) {
  target.spinner.stop()
}


function ajaxCancelJob(index, name) {

  var dlgSel = $("#canceldiv")
  dlgSel.html('The job with [' + name + '] will be removed from the queue. <br />Do you want to continue?')

  var dialog = dlgSel.dialog({
    autoOpen: false,
    title: 'Remove Job From the Queue',
    modal: true,
    buttons: {
      "Ok": function () {
        $.ajax({
          type: 'GET',
          url: '/cancelJob?index=' + index,
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

  dialog.dialog("open");

}

function ajaxCancelActiveJob(name) {
  var dlgSel = $("#canceldiv")
  dlgSel.html('The job [' + name +
    '] is running. <br /> It might be waiting for manual interaction. <br />' +
    'Are you still sure that you want to cancel it?');

  var dialog = dlgSel.dialog({
    autoOpen: false,
    title: 'Cancel Active Job',
    modal: true,
    buttons: {
      "Ok": function () {
        $.ajax({
          type: 'GET',
          url: '/cancelActiveJob',
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

  dialog.dialog("open");

}

function enqueue(jobName, jobId, visibility) {

  var dialogElm = $("#jobEnqueueDialog")
  var select = $("#jobEnqueueDialog > .visibilitySelect")
  select.val(visibility)

  var dialog = dialogElm.dialog({
    autoOpen: false,
    title: 'Enqueue job ' + jobName + '?',
    modal: true,
    width: 550,
    height: 400,
    buttons: {
      "Ok": function () {
        var theUrl = jsRoutes.controllers.TestQueueController.enqueueJob(jobId, select.val())
        $.ajax({
          type: 'GET',
          url: theUrl.url,
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
  dialog.dialog("open");
}



