var UserToolbar = {
  name: 'userToolbar',
  onClick: function (target, data) {
    if (data.subItem !== undefined) {
      switch (data.subItem.id) {
        case 'change-password':
          w2ui.mainLayout.toggle('left', window.instant);
          break;
        case 'view-profile':
          w2ui.mainLayout.toggle('right', window.instant);
          break;
        case 'logout':
          Authentication.logout()
          break;
      }
    } else {
      switch (target) {
        case 'loginItem':
          break;
      }
    }
  },
  items: [{
    type: 'menu',
    id: 'userItem',
    caption: Authentication.userName,
    icon: 'fa-user',
    items: [
      {text: 'Change Password', icon: 'fa-key', id: 'change-password'},
      {text: 'View Profile', icon: 'fa-picture', id: 'view-profile'},
      {text: 'Logout', icon: 'fa-signout', id: 'logout'}
    ]
  }]
};
