smallGridTestGroupList = {
  name: 'smallGridTestGroupList',
  header: 'Test Groups',
  show : {
    toolbar : true,
    header         : true,
  },
  searches: [
    { field: 'fname', caption: 'Name', type: 'text' },
  ],
  buttons:{
  //  search: undefined,
  },
  columns: [
    { field: 'fname', caption: 'Name', size: '100%', sortable: true }
  ],
  onAdd: function (event) {
    w2alert('add');
  },
  onEdit: function (event) {
    w2alert('edit');
  },
  onDelete: function (event) {
    console.log('delete has default behaviour');
  },
  onDblClick: function(event){
    smallGridTestGroupList.event = event;
    w2alert(smallGridTestGroupList.testGroupList[event.recid].groupName)
  }
};
