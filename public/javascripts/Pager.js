function Pager(maxPages) {
  this.maxPages = maxPages
  this.$ = $(this)
  
  var pager = createJQElement('nav')
  pager.addClass('specialPager')
  pager.attr('aria-label', 'Page Nav')
  var ul = createJQElement('ul', pager)
  ul.addClass('pagination pagination-sm')
  ul.addClass('specialPager')
  var li = createJQElement('li', ul)
  var leftA = createJQElement('a', li)
  leftA.attr('href', '#')
  leftA.attr('aria-label', 'Prev')
  var span = createJQElement('span', leftA)
  span.attr('aria-hidden', 'true')
  span.html('&laquo;')
  
  
  li = createJQElement('li', ul)
  var infoSpan = createJQElement('span', li)
  var pageSpan = createJQElement('span', infoSpan)
  pageSpan.text(1)
  var maxSpan = createJQElement('span', infoSpan)
  maxSpan.text("/" + maxPages)
  
  li = createJQElement('li', ul)
  var rightA = createJQElement('a', li)
  rightA.attr('href', '#')
  rightA.attr('aria-label', 'Next')
  span = createJQElement('span', rightA)
  span.attr('aria-hidden', 'true')
  span.html('&raquo;')
  
  
  this.setMax = function (max) {
    this.maxPages = max
    maxSpan.text('/' + max)
  }
  
  this.getPage = function () {
    return parseInt(pageSpan.text()) - 1
  }
  
  this.setPage = function (page) {
    pageSpan.text(page + 1)
  }
  
  this.nextPage = function () {
    var page = this.getPage()
    if (page == this.maxPages - 1)
      return
    page++
    this.setPage(page)
    this.$.trigger('pageChanged', page)
  }
  
  this.prevPage = function () {
    var page = this.getPage()
    if (page == 0)
      return
    
    page--
    this.setPage(page)
    this.$.trigger('pageChanged', page)
  }
  
  this.getDOM = function () {
    return pager;
  }
  
  rightA.on('click', function () {
    this.nextPage()
  }.bind(this))
  
  leftA.on('click', function () {
    this.prevPage()
  }.bind(this))
  
}