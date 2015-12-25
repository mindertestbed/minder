//the singleton authentication object
var Authentication = {
  /**
   * Username
   */
  userName: null,

  /**
   * Password hash
   */
  passwordHash: null,
  /**
   * My challenge
   */
  clientNonce: null,
  /**
   * Request counter
   */
  requestCounter: null,
  /**
   *
   */
  serverBaseURI: 'http://localhost:9000',
  /**
   * These fields will be initialized after first realm exchange phase with the server
   */
  realm: null,
  /**
   * The random challenge received from the server
   */
  serverNonce: null,
  /**
   * The actual url to submit the login data to.
   */
  loginUrl: null,

  /**
   * The url where we receive the initial realm and nonce values
   */
  realmUrl: null,

  //Digestrealm="rest@minder.gov.tr",nonce="B8A1A7604A4E623F8DC883CB17A4F90C"

  /**
   * Creates a login form in a w2popup
   * and calls success function if login suceeds
   * @param id
   * @param url
   */
  startLogin: function () {
    console.log("Realm URL: " + Authentication.realmUrl)
    $.ajax({
      type: 'GET',
      async: false,
      url: Authentication.realmUrl,
      success: function (data) {
        //should be possible
        alert(response.getResponseHeader('WWW-Authenticate'))
      },
      error: function (response, textStatus, errorThrown) {
        var digest = response.getResponseHeader('WWW-Authenticate')
        digest = digest.replace(/=/g, ':').replace(/xDigest\s*/g, '').replace(/realm/g, '"realm"').replace(/nonce/g, '"nonce"');
        digest = JSON.parse('{' + digest + '}');

        Authentication.realm = digest.realm;
        Authentication.serverNonce = digest.nonce;

        console.log("Realm: " + Authentication.realm)
        console.log("Server Nonce: " + Authentication.serverNonce)
      }
    });
  },

  /**
   * After obtaining username and password, when the user clicks 'Send' button,
   * this function will be called
   * @param username
   * @param password
   */
  authenticate: function (username, password) {
    //first obtain realm and other stuff
    Authentication.startLogin()

    console.log("Username: " + username)
    console.log("Password: " + password)

    //calculate my nonce and save temporary values
    Authentication.clientNonce = CryptoFunctions.generateClientNonce()
    Authentication.passwordHash = CryptoFunctions.sha256(password)
    Authentication.userName = username
    //initialize request counter
    Authentication.requestCounter = 0 //reset to 0, ajax function will do it 1 and do first call

    console.log("Client Nonce: " + Authentication.clientNonce)
    console.log("Password hash: " + Authentication.passwordHash)

    RestUtil.restCall({
      url: Authentication.loginUrl,
      async: false,
      method: 'GET',
      success: function (data) {
        console.log('succes: ' + data);
        //save the authentication data into local storage
        Authentication.persist()

        if (Authentication.successCallback !== undefined) {
          Authentication.successCallback()
        } else{

          console.log('Undefined');
        }
      },
      error: function () {
        w2alert("Login Failed!")
      }
    })

    w2ui.loginForm.unlock();
  },
  persist: function () {
    if (typeof(Storage) !== "undefined") {
      localStorage.userName = Authentication.userName;
      localStorage.passwordHash = Authentication.passwordHash;
      localStorage.clientNonce = Authentication.clientNonce;
      localStorage.requestCounter = Authentication.requestCounter;
      localStorage.realm = Authentication.realm;
      localStorage.serverNonce = Authentication.serverNonce;
    }
  },
  restore: function () {
    if (typeof(Storage) !== "undefined") {
      if (localStorage.userName !== undefined && localStorage.userName !== null) {
        Authentication.userName = localStorage.userName;
        Authentication.passwordHash = localStorage.passwordHash;
        Authentication.clientNonce = localStorage.clientNonce;
        Authentication.requestCounter = localStorage.requestCounter;
        Authentication.realm = localStorage.realm;
        Authentication.serverNonce = localStorage.serverNonce;
      }
    }
  },
  logout: function(){
    Authentication.userName = null;
    Authentication.passwordHash = null;
    Authentication.clientNonce = null;
    Authentication.requestCounter = null;
    Authentication.realm = null;
    Authentication.serverNonce = null;
    if (typeof(Storage) !== "undefined") {
      localStorage.clear()
    }
    if(Authentication.logoutCallback !== undefined){
      Authentication.logoutCallback()
    }
  },
  isAuthenticated: function () {
    console.log("Authentication.userName: " + Authentication.userName)
    console.log('Is null: '+ (Authentication.userName !== null))
    if (Authentication.userName !== null) {
      console.log(Authentication.userName)
      return true
    }else {
      Authentication.restore()
      console.log('Restore: '+ Authentication.userName)
      console.log('Restore: '+ (Authentication.userName !== null))
      return Authentication.userName !== null
    }
  }
}
