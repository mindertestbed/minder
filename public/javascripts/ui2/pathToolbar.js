pathToolbar = {
  name: 'pathToolbar',
    items: [
    { type: 'html',  id: 'label',
      html: '<div>'+
      ' Path:'+
      '</div>'
    },
    { type: 'menu',   id: 'tgPath', caption: 'AS4 Tests', icon: 'fa-folder-close', items: [
      { text: 'TA 01', icon: 'fa-star'},
      { text: 'TA 01', icon: 'fa-star'},
      { text: 'TA 01', icon: 'fa-star'}
    ]},
    { type: 'menu',   id: 'taPath', caption: 'TA 02', icon: 'fa-star', items: [
      { text: 'Test Case 1', icon: 'fa-file'},
      { text: 'Test Case 2', icon: 'fa-file'},
      { text: 'Test Case 3', icon: 'fa-file'}
    ]},
    { type: 'menu',   id: 'tcPath', caption: 'TestCase2', icon: 'fa-file', items: [
      { text: 'TestCase2-IBM ', icon: 'fa-file'},
      { text: 'TestCase2-Domibus', icon: 'fa-file'},
      { text: 'TestCase2-2', icon: 'fa-file'}
    ]},
    { type: 'button',  id: 'jobPath', caption: 'TestCase2-IBM', icon: 'fa-star'},
  ]
};
