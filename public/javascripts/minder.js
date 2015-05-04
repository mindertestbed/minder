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

function createFormDialog(elm, sourceUrl, action, dialogId, titl, target, w, h) {
  if (typeof(w) === 'undefined') w = '50%';
  if (typeof(h) === 'undefined') h = '500';

  var frm = $("#" + dialogId + " > form");
  elm.on('click', function (event) {
    event.stopPropagation();

    var dialog = $("#" + dialogId).dialog({
      autoOpen: false,
      height: "100%",
      width: "100%",
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
              dialog.dialog("close");
            },
            error: function (jqXHR, textStatus, errorMessage) {
              frm[0].innerHTML = jqXHR.responseText;
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
              target[0].innerHTML = data;
              executeScripts(target);
            },
            error: function (jqXHR, textStatus, errorMessage) {
              alert(jqXHR.responseText);
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

function createJob(testCaseId) {
  $.ajax({
    type: 'GET',
    url: '/getCreateJobEditorView?testCaseId=' + testCaseId,
    success: function (data) {
      showCreateDialog(testCaseId, data)
    },
    error: function (jqXHR, textStatus, errorMessage) {
      showError(jqXHR.responseText);
    }
  });
}

function showCreateDialog(testCaseId, data) {
  var frm = $('#mainForm');
  frm[0].innerHTML = data;
  executeScripts(frm);
  var search = "#rcRoot" + testCaseId;
  var parr = $(search);

  var dialog = $('#mainDialog').dialog({
    autoOpen: false,
    height: 500,
    width: 800,
    title: 'Create Job',
    modal: true,
    buttons: {
      "Create": function () {
        $.ajax({
          type: 'post',
          url: '/doCreateJob',
          data: frm.serialize(),
          success: function (data) {
            parr[0].innerHTML = data;
            executeScripts(parr);
            dialog.dialog("close");
          },
          error: function (jqXHR, textStatus, errorMessage) {
            frm[0].innerHTML = jqXHR.responseText;
            executeScripts(frm);
          }
        });
      },
      Cancel: function () {
        dialog.dialog("close");
      }
    }
  });

  dialog.dialog("open", 'Create Job');
}

function editJob(testCaseId, id, name) {
  //perform ajax to get the edit form
  $.ajax({
    type: 'GET',
    url: '/editJobForm?id=' + id,
    success: function (data) {
      showEditDialog(testCaseId, id, data, name)
    },
    error: function (jqXHR, textStatus, errorMessage) {
      showError(jqXHR.responseText);
    }
  });
}

function showEditDialog(testCaseId, id, data, title) {
  var form = $("#mainForm");
  form[0].innerHTML = data;
  executeScripts(form);
  var parent = $("#rcRoot" + testCaseId).parent();

  var dialog = $("#mainDialog").dialog({
    autoOpen: false,
    height: 600,
    width: 1100,
    title: 'Job' + title,
    modal: true,
    buttons: {
      "Save": function () {
        $.ajax({
          type: 'post',
          url: '/doEditJob',
          data: form.serialize(),
          success: function (data) {
            parent[0].innerHTML = data;
            executeScripts(parent);
            dialog.dialog("close");
          },
          error: function (jqXHR, textStatus, errorMessage) {
            form[0].innerHTML = jqXHR.responseText;
            form[0].reset();
          }
        });
      },
      Cancel: function () {
        dialog.dialog("close");
      }
    },
    close: function () {
      form[0].reset();
    }
  });

  dialog.dialog("open", title);
}


function displayJob(id, name, userId) {
  //perform ajax to get the run dialog
  $.ajax({
    type: 'GET',
    url: '/displayJob?id=' + id,
    success: function (data) {
      showRunDialog(id, data, name, userId)
    },
    error: function (jqXHR, textStatus, errorMessage) {
      showError(jqXHR.responseText);
    }
  });
}

function showRunDialog(jobId, data, title, userId) {
  var runDialog = $("#runDialog");
  runDialog[0].innerHTML = data
  executeScripts(runDialog);

  //here we have the handle to the inner table where the test status is shown.
  //we will update it during the test.
  //see: jobDetailView>div #testStatus.
  var targetStatusDiv = $('#testStatus')

  var dialog = runDialog.dialog({
    autoOpen: false,
    height: 650,
    width: 1200,
    title: 'Job ' + title,
    modal: true,
    buttons: {
      "Run": function () {
        runTest(runDialog, jobId, targetStatusDiv, userId)
      },
      "Close": function () {
        dialog.dialog("close");
      }
    }
  });

  dialog.dialog("open", title);
}

function runTest(runDialog, jobId, targetStatusDiv, userId) {
  $.ajax({
    type: 'GET',
    url: '/runTest?id=' + jobId + '&userId=' + userId,
    success: function (data) {
      targetStatusDiv[0].innerHTML = data;
      executeScripts(targetStatusDiv);
      if (data.indexOf("TEST_STATUS:1") != -1 || data.indexOf("TEST_STATUS:0") != -1) {
        syncTest(runDialog, jobId, targetStatusDiv, userId);
      }
    },
    error: function (jqXHR, textStatus, errorMessage) {
      showError(jqXHR.responseText);
    }
  });
}

function syncTest(runDialog, jobId, targetStatusDiv, userId) {
  setTimeout(function () {
    $.ajax({
      type: 'GET',
      url: '/syncTest?id=' + jobId + '&userId=' + userId,
      success: function (data) {
        targetStatusDiv[0].innerHTML = data;
        executeScripts(targetStatusDiv);
        if (data.indexOf("TEST_STATUS:1") != -1 || data.indexOf("TEST_STATUS:0") != -1) {
          syncTest(runDialog, jobId, targetStatusDiv, userId);
        }
      },
      error: function (jqXHR, textStatus, errorMessage) {
        showError(jqXHR.responseText);
      }
    });
  }, 500);
}


function deleteJob(testCaseId, idd, name) {
  var parent = $("#rcRoot" + testCaseId).parent();
  var deleteDialog = $("#dialog-confirm").dialog({
    resizable: false,
    height: 200,
    width: "50%",
    title: 'Delete Job',
    autoOpen: false,
    modal: true,
    buttons: {
      "Delete": function () {
        $.ajax({
          type: 'GET',
          url: '/doDeleteJob?id=' + idd,
          success: function (data) {
            parent[0].innerHTML = data;
            executeScripts(parent);
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

  deleteDialog.find("span.itemtype")[0].innerHTML = 'Job';
  deleteDialog.find("span.itemname")[0].innerHTML = name;
  deleteDialog.dialog("open");
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
            alert(jqXHR.responseText);
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