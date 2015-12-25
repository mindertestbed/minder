//the singleton authentication object
var RestUtil = {
  prepareAuthenticateHeader: function (response) {
    header = 'xDigest username="' + Authentication.userName + '",realm="' + Authentication.realm + '",' +
      'nonce="' + Authentication.serverNonce + '",uri="' + Authentication.serverBaseURI + '",nc="' + Authentication.requestCounter + '",cnonce="' + Authentication.clientNonce +
      '",response="' + response + '"'
    return header;

  },
  calculateResponseValue: function (restVerb) {
    console.log('username ' + Authentication.userName)
    console.log('password ' + Authentication.passwordHash)
    console.log('realm ' + Authentication.realm)
    console.log('restVerb ' + restVerb)
    console.log('uri ' + Authentication.serverBaseURI)
    console.log('nonce ' + Authentication.serverNonce)
    console.log('cnonce ' + Authentication.clientNonce)
    console.log('nc ' + Authentication.requestCounter)

    /*
     * Calculate HA1 = MD5("<userName>:<realm>:<password>")
     * */
    var ha1 = Authentication.userName + ":" + Authentication.realm + ":" + Authentication.passwordHash

    var hashHA1 = CryptoFunctions.md5(ha1);

    console.log("HA1= " + hashHA1);


    /*
     * Calculate HA2 = MD5("<verb>:<URi>")
     * */
    var ha2 = restVerb + ":" + Authentication.serverBaseURI
    var hashHA2 = CryptoFunctions.md5(ha2);
    console.log("HA2= " + hashHA2);

    /*
     * Calculate Response = MD5("HA1:\NONCE:\NC:CNONCE:\HA2)
     * */
    var response = hashHA1 + ':\\' + Authentication.serverNonce + ':\\' + Authentication.requestCounter + ':' + Authentication.clientNonce + ':\\' + hashHA2;
    console.log("HA3" + response)
    var responseValue = CryptoFunctions.md5(response);

    console.log("HA3 HASH: " + responseValue)
    return responseValue;
  },
  restCall: function (ajaxObj) {
    if (Authentication.userName === null) {
      throw("Not authenticated yet")
    }

    Authentication.requestCounter++

    var response = RestUtil.calculateResponseValue('POST');
    var authHeader = RestUtil.prepareAuthenticateHeader(response);

    ajaxObj.headers = {
      'Authorization': authHeader,
      'Content-Type': 'application/json'
    };

    $.ajax(ajaxObj)
  }
}
