package services

import concurrent.Future

trait AuthenticationService {

  def authenticate(authenticationToken: AuthenticationToken): Future[Boolean]

  def authorize(authenticationToken: AuthenticationToken): Future[UserCredentials]

  def logout(userCredentials: UserCredentials): Future[UserCredentials]
}

trait AuthenticationToken

case class UsernamePasswordToken(username: String, password: String) extends AuthenticationToken

case class UserCredentials(sessionKey: String)