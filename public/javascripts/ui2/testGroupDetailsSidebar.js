testGroupSideBar = {
  name: 'testGroupSideBar',
  nodes: [
    {
      id: 'level-2', text: 'Test Assertions', img: 'icon-folder', expanded: true, group: true,
      nodes: [{
        id: 'level-2-1', text: 'Level 2.1', img: 'icon-folder', count: 3,
        nodes: [
          {id: 'level-2-1-1', text: 'Level 2.1.1', img: 'icon-folder'},
          {id: 'level-2-1-2', text: 'Level 2.1.2', icon: 'fa-star-empty', count: 67},
          {id: 'level-2-1-3', text: 'Level 2.1.3', icon: 'fa-star-empty'}
        ]
      },
        {id: 'level-2-2', text: 'Level 2.2', img: 'icon-page'},
        {id: 'level-2-3', text: 'Level 2.3', icon: 'icon-search'}
      ]
    }
  ],
  onClick: function (event) {

  }
};
