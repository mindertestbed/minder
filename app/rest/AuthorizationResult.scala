package rest

/**
  */
trait AuthorizationResult

case class StaleNonce() extends AuthorizationResult

case class Success(val userName: String) extends AuthorizationResult

case class Failure() extends AuthorizationResult
