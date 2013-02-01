package services.actors

import org.scalatest.WordSpec
import akka.actor.{Actor, ActorRef}
import akka.testkit.TestActorRef
import akka.pattern.ask
import concurrent.duration._
import concurrent.Await
import domain.models.User
import org.scalatest.matchers.ShouldMatchers
import support.test.{MockedActorProvider, TestSystem}

class AuthenticationActorTest extends WordSpec with TestSystem with ShouldMatchers {

  implicit val timeout = akka.util.Timeout(5 seconds)

  val validToken = UsernamePasswordToken("username", "password")
  val validUserLookup = UserLookup(validToken.username, validToken.password)
  val validUser = User(validToken.username, validToken.password)
  val invalidToken = UsernamePasswordToken("invalid", "")

  val userWithUpdatedSessionKey = User(0, validToken.username, validToken.password, "sessionKey")

  val mockedReadActor: ActorRef = TestActorRef(new Actor {

    override def receive = {
      case ReadUser(lookup) if lookup == validUserLookup ⇒ sender ! UserFound(validUser)
      case ReadUser(lookup) ⇒ sender ! UserNotFound(lookup)
    }
  })

  val mockedWriteActor: ActorRef = TestActorRef(new Actor {

    override def receive = {
      case UpdateUser(user) ⇒ sender ! UserUpdated(userWithUpdatedSessionKey)
    }
  })

  trait ActorMocks extends MapActorsConfiguration[ActorRef] {

    override def actorDetailsMap = Map(
      classOf[UsersReadModelActor] -> mockedReadActor,
      classOf[UsersWriteModelActor] -> mockedWriteActor
    )
  }

  val authenticationActor = TestActorRef(new AuthenticationActor with MockedActorProvider with ActorMocks)

  "Authentication actor" must {
    "not allow to authenticate User that does not exist" in {
      val response = Await.result(authenticationActor ? AuthorizationCommand(invalidToken), 1 seconds)
      response should equal(AuthorizationFailure())
    }
    "allow to authenticate User that exists" in {
      val response = Await.result(authenticationActor ? AuthorizationCommand(validToken), 1 seconds)
      response should equal(AuthorizationSuccess(UserCredentials(userWithUpdatedSessionKey.sessionKey)))
    }
  }
}