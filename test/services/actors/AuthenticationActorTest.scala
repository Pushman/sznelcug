package services.actors

import org.scalatest.WordSpec
import akka.actor.ActorRef
import akka.testkit.TestActorRef
import akka.pattern.ask
import concurrent.duration._
import concurrent.Await
import domain.models.User
import org.scalatest.matchers.ShouldMatchers
import support.test.{EmptyActor, MockedActorProvider, TestSystem}
import support.test.given

class AuthenticationActorTest extends WordSpec with TestSystem with ShouldMatchers {

  implicit val timeout = akka.util.Timeout(5 seconds)

  val validToken = UsernamePasswordToken("username", "password")
  val validUser = User(validToken.username, validToken.password)
  val invalidToken = UsernamePasswordToken("invalid", "password")

  val userWithUpdatedSessionKey = User(0, validToken.username, validToken.password, "sessionKey")

  val mockedReadActor = TestActorRef(new EmptyActor)
  val mockedWriteActor = TestActorRef(new EmptyActor)

  trait ActorMocks extends MapActorsConfiguration[ActorRef] {

    override def actorDetailsMap = Map(
      classOf[UsersReadModelActor] -> mockedReadActor,
      classOf[UsersWriteModelActor] -> mockedWriteActor
    )
  }

  val authenticationActor = TestActorRef(new AuthenticationActor with MockedActorProvider with ActorMocks)

  "Authentication actor" must {
    "not allow to authenticate User that does not exist" in {
      mockedReadActor given (sender => {
        case ReadUser(lookup) => sender ! UserNotFound(lookup)
      })

      val response = Await.result(authenticationActor ? AuthorizationCommand(invalidToken), 1 seconds)

      response should equal(AuthorizationFailure())
    }
    "allow to authenticate User that exists" in {
      mockedReadActor given (sender => {
        case ReadUser(lookup) => sender ! UserFound(validUser)
      })
      mockedWriteActor given (sender => {
        case UpdateUser(user) => sender ! UserUpdated(userWithUpdatedSessionKey)
      })

      val response = Await.result(authenticationActor ? AuthorizationCommand(validToken), 1 seconds)

      response should equal(AuthorizationSuccess(UserCredentials(userWithUpdatedSessionKey.sessionKey)))
    }
  }
}