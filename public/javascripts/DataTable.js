function createJQElement(text, target) {
  var elem = $(document.createElement(text))
  if (target !== undefined && target != null)
    target.append(elem)
  
  return elem
}

function DataTable(targetDiv, initialItemId, levels, levInd, rootTable) {
  var level = levels[0];
  
  if (levInd === undefined) {
    var levelIndex = 0
  } else {
    var levelIndex = levInd
  }
  
  if (rootTable === undefined)
    rootTable = this
  
  var currentPage = 0
  //initialize the target table
  
  var topTable = createJQElement('table', targetDiv)
  topTable.addClass('table table-bordered table-condensed')
  topTable.css('margin', '0')
  topTable.css('padding', '0')
  var topBody = createJQElement('tbody', topTable)
  var topTableRow = createJQElement('tr', topBody)
  var titleCell = createJQElement('td', topTableRow)
  titleCell.addClass('tabshrink')
  var titleSpan = createJQElement('span', titleCell)
  titleSpan.addClass('vertical-text')
  titleSpan.text(levels[0].title)
  
  var pagingCell = null
  var contentRow = topTableRow
  if (level.pageSize !== undefined) {
    pagingCell = createJQElement('td', topTableRow)
    titleCell.attr('rowspan', '2')
    contentRow = createJQElement('tr', topBody)
  }
  
  var contentCell = createJQElement('td', contentRow)
  contentCell.css('margin', '0')
  contentCell.css('padding', '0')
  
  var contentTable = createJQElement('table', contentCell)
  contentTable.addClass('table table-condensed')
  contentTable.css('margin', '0')
  contentTable.css('border', '1px solid #cccccc')
  var contentBody = createJQElement('tbody', contentTable)
  
  backgroundColor = level.backgroundColor
  
  if (level.backgroundColor !== undefined) {
    topTable.css('background-color', level.backgroundColor)
    contentTable.css('background-color', level.backgroundColor)
  }
  
  var pager = null
  if (pagingCell != null) {
    pager = new Pager(100)
    pagingCell.append(pager.getDOM())
  }
  
  var contentFirstRow = createJQElement('tr', contentBody)
  
  var renderCheckBoxes = level.renderCheckBoxes
  var renderDownButtons = level.renderDownButtons
  var columns = level.columns
  var colSpan = 0
  if (renderDownButtons)
    colSpan++
  if (renderCheckBoxes)
    colSpan++
  if (colSpan > 0) {
    var th = createJQElement('th', contentFirstRow)
    th.attr('colspan', colSpan)
  }
  
  for (var j in columns) {
    var th = createJQElement('th', contentFirstRow)
    th.html(columns[j].label)
  }
  
  var successAjax = function (json) {
    var count = json.count
    var content = json.content
    contentBody.empty()
    pager.setMax(Math.ceil(count / level.pageSize))
    
    pager.$.on('pageChanged', function (e, newPage) {
      currentPage = newPage
      this.updateTable()
    }.bind(this))
    
    for (var i in content) {
      var tr = createJQElement('tr', contentBody)
      var targetRow = createJQElement('tr', contentBody)
      targetRow.hide()
      
      if (renderDownButtons) {
        var td = createJQElement('td', tr)
        var btn = createJQElement('button', td)
        var nextLevels = levels.slice(1)
        var targetCell = createJQElement('td', targetRow)
        targetCell.attr('colspan', colSpan + columns.length)
        targetCell.css('padding', 0)
        btn.addClass('btn btn-xs btn-default fa fa-caret-right')
        btn.nextLevel = new DataTable(targetCell, content[i].id, nextLevels, levelIndex + 1, rootTable)
        if (btn.nextLevel.backgroundColor !== undefined) {
          targetRow.css('background-color', btn.nextLevel.backgroundColor)
        }
        
        btn.targetRow = targetRow
        btn.on('click', function () {
          if (this.hasClass('fa-caret-right')) {
            this.addClass('fa-caret-down')
            this.removeClass('fa-caret-right')
            this.nextLevel.updateTable()
          } else {
            this.addClass('fa-caret-right')
            this.removeClass('fa-caret-down')
          }
          this.targetRow.toggle()
        }.bind(btn))
        td.addClass('tabshrink')
        td.css('width', '30px !important')
      }
      
      if (renderCheckBoxes) {
        var td = createJQElement('td', tr)
        var addButton = createJQElement('button', td)
        addButton.level = levelIndex
        addButton.itemId = content[i]['id']
        addButton.addClass('btn btn-xs btn-primary fa fa-plus')
        addButton.css('font-size', '10px')
        addButton.css('padding', '2px 4px 2px 4px')
        addButton.css('line-height', '1.1')
        addButton.dataTable = this
        addButton.on('click', function () {
          itemClicked(this.level, this.itemId)
        }.bind(addButton))
        td.css('width', '30px !important')
        td.addClass('tabshrink')
      }
      
      for (var j in columns) {
        var td = createJQElement('td', tr)
        var resultData = content[i][columns[j].field];
        if (columns[j].formatFunction !== undefined) {
          resultData = columns[j].formatFunction(resultData)
        }
        td.html(resultData)
      }
    }
  }.bind(this)
  
  if (level.pageSize !== undefined) {
    currentPage = 0;
    this.updateTable = function () {
      level.updateFunction(initialItemId, currentPage, level.pageSize).ajax({
        success: successAjax
      })
    }
  } else {
    this.updateTable = function () {
      level.updateFunction(initialItemId, 0, 0).ajax({
        success: successAjax
      })
    }
  }
  
  function itemClicked(level, itemId) {
    rootTable.$.trigger('itemClicked', [level, itemId])
  }
  
  this.$ = $(this)
}

/*
* data Source:
* - levels:
*      - content update function (should return json data)
*      - title
*      - render checkbox?
*      - render refresh button
*      - columns:
*         - name
*         - formatter function
*
*
*
* the object should have a getResult() function that returns:
*   -- the json tree for the current structure and the 'selection' info:
*   {
*     id: 1,
*     type: 'type info',
*     selected: true,
*     items: [
*         {
*            id: 15,
*            type: 'type info',
*            selected: false,
*            items: [....]
*         }, ....
*     ]
*   }
*
* */