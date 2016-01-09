MainToolbar = {
  name: 'mainToolbar',
    items: [
    { type: 'check',  id: 'item1', caption: 'Check', icon: 'fa-check', checked: true },
    { type: 'break',  id: 'break0' },
    { type: 'menu',   id: 'item2', caption: 'Menu', icon: 'fa-table', count: 17, items: [
      { text: 'Item 1', icon: 'fa-camera', count: 5 },
      { text: 'Item 2', icon: 'fa-picture', disabled: true },
      { text: 'Item 3', icon: 'fa-glass', count: 12 }
    ]},
    { type: 'break', id: 'break1' },
    { type: 'radio',  id: 'item3',  group: '1', caption: 'Radio 1', icon: 'fa-star', checked: true },
    { type: 'radio',  id: 'item4',  group: '1', caption: 'Radio 2', icon: 'fa-heart' },
    { type: 'break', id: 'break2' },
    { type: 'spacer' },
    { type: 'drop',
      id: 'item5',
      caption: 'Drop Down',
      icon: 'fa-plus',
      html: '<div style="padding: 10px">Drop down</div>' },
    { type: 'break', id: 'break3' },
    { type: 'html',  id: 'item6',
      html: '<div style="padding: 3px 10px;">'+
      ' Input:'+
      '    <input size="10" style="padding: 3px; border-radius: 2px; border: 1px solid silver"/>'+
      '</div>'
    },
    { type: 'spacer' },
    {type: 'menu',   id: 'viewMenu', caption: 'View', icon: 'fa-table', count: 4, items: [
      {  id: 'groupMenu',  caption: 'Group Menu', icon: 'fa-eye-open' },
      {  id: 'jobStream',  caption: 'Job Stream', icon: 'fa-eye-open' },
      {  id: 'preivew',  caption: 'Preview', icon: 'fa-eye-open' },
      {  id: 'console',  caption: 'Console', icon: 'fa-eye-open' }
    ]}
  ],
    onClick: function (target, data) {
    if(data.subItem !== undefined){
      switch (data.subItem.id){
        case 'groupMenu':
          w2ui.mainLayout.toggle('left', window.instant);
          break;
        case 'jobStream':
          w2ui.mainLayout.toggle('right', window.instant);
          break;
        case 'preivew':
          w2ui.mainLayout.toggle('preview', window.instant);
          break;
        case 'console':
          w2ui.mainLayout.toggle('bottom', window.instant);
          break;
      }
    } else {
      switch (target) {
        case 'viewMenu':
          break;
      }
    }
  }
};
