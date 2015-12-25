var CryptoFunctions = {
  generateClientNonce: function () {
    var cryptoObj = window.crypto || window.msCrypto; // for IE 11
    var buf = new Uint8Array(4);
    cryptoObj.getRandomValues(buf)
    var hex = CryptoFunctions.ua2hex(buf).toString().toUpperCase();
    return hex
  },
  ua2hex: function (ua) {
    var h = '';
    for (var i = 0; i < ua.length; i++) {
      h += ua[i].toString(16);
    }
    return h;
  },
  sha256: function (data) {
    return CryptoJS.SHA256(data).toString().toUpperCase()
  },
  md5: function (data) {
    return CryptoJS.MD5(data).toString().toUpperCase()
  }
}
