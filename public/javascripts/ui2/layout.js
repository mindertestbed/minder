var pstyle = 'border: 1px solid #dfdfdf; padding: 0px;';

mainLayout = {
  name: 'mainLayout',
  panels: [
    {type: 'top', size: 79, resizable: false, style: pstyle},
    {type: 'left', size: 240, resizable: true, style: pstyle},
    {type: 'main', style: pstyle},
    {
      type: 'right',
      size: 200,
      resizable: true,
     // hidden: true,
      style: pstyle
    },
    {type: 'bottom', size: 100, resizable: true, hidden: true, style: pstyle, content: 'bottom'},
    {type: 'preview', size: 100, resizable: true, hidden: true, style: pstyle, content: 'preview'}
  ]
};

