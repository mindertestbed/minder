function showUpdateTextAreaDialog(elemId, targetField, targetElem, action) {
  var editor = $("#updateTextAreaDialog > textArea");
  var dialogSelector = $("#updateTextAreaDialog");
  //target elem is a hidden input
  editor.val(targetElem.val());
  showUpdateDialog(elemId, targetField, targetElem, editor, dialogSelector, action)
}

function showUpdateOptionDialog(elemId, targetField, targetElem, action, converter, options){
  var editor = $("#updateOptionDialog > select");

  editor.find('option').remove().end()

  for (option in options){
    editor.append(new Option(option, options[option]));
  }

  var dialogSelector = $("#updateOptionDialog");
  //target elem is a hidden input
  editor.val(targetElem.val());
  showUpdateDialog(elemId, targetField, targetElem, editor, dialogSelector, action, converter)

}

function showUpdateInputTextDialog(elemId, targetField, targetElem, action) {
  var editor = $("#updateInputTextDialog > input");
  var dialogSelector = $("#updateInputTextDialog");
  editor.val(targetElem.val());
  showUpdateDialog(elemId, targetField, targetElem, editor, dialogSelector, action)
}

function showUpdateDialog(elemId, targetField, targetElem, editor, dialogSelector, action, converterName) {
  var wid = targetElem.width();
  var hei = targetElem.height();

  if (wid < 550)
    wid = 550;
  if (hei < 400)
    hei = 400;

  var dialog = dialogSelector.dialog({
    autoOpen: true,
    width: "" + wid,
    height: "" + hei,
    modal: true,
    buttons: {
      "Ok": function () {
        var jsn = {}
        jsn['id'] = elemId;
        jsn['field'] = targetField;
        jsn['newValue'] = editor.val();
        if (converterName !== undefined && converterName !== null) {
          jsn['converter'] = converterName;
        }
        $.ajax({
          type: "POST",
          url: action,
          contentType: 'application/json; charset=UTF-8', // This is the money shot
          data: JSON.stringify(jsn),
          success: function (data) {
            var kk = JSON.parse(data)
            targetElem.val(kk.value)
            targetElem.text(kk.value)
            targetElem.trigger('change')
            dialog.dialog("close");
          },
          error: function (jqXHR, textStatus, errorMessage) {
            showError(jqXHR.responseText)
          }
        });
      },
      Cancel: function () {
        dialog.dialog("close");
      }
    },
    close: function () {
    }
  });
}

function updateBr(value, target) {
  var newd = value.replace(/</g, '&gt;')
  var newd = newd.replace(/>/g, '&lt;')
  var newd = newd.split(/\n/g).join("<br/>");
  var newd = newd.replace(/ /g, '&nbsp;')
  target[0].innerHTML = newd;
}

function bindValues(elem) {
  var selnext = elem.next();
  selnext.change(function () {
    smartUpdate(selnext, elem)
  });
  smartUpdate(selnext, elem)
}


function smartUpdate(selnext, elem) {
  var slnv = selnext.val()
  if (slnv == null || slnv.length == 0) elem[0].innerHTML = '<b>No Content. Click To Add</b>'
  else
    updateBr(slnv, elem)
}
