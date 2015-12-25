/**
 * Update a W2ui layout part with a jquery object
 * @param jqueryObject
 */
function layoutUpdate(w2uiPart, where, jqueryObject, titl) {
  w2uiPart.content(where, $().w2layout({
    name: where,
    title: titl,
    panels: [{type: 'main', content: jqueryObject, title: titl}]
  }));
}
