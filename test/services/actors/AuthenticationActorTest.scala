package services.actors

import _root_.support.datetime.Clock
import _root_.support.test._
import org.scalatest.WordSpec
import akka.actor.ActorRef
import akka.testkit.TestActorRef
import domain.models.User
import org.scalatest.matchers.ShouldMatchers
import support._
import org.eligosource.eventsourced.core.Message
import org.joda.time.DateTime

class AuthenticationActorTest extends WordSpec with TestSystem with ShouldMatchers {

  import AuthenticationActorTest._

  private val validToken = UsernamePasswordToken("username", "password")
  private val validUser = User(validToken.username, validToken.password)
  private val invalidToken = UsernamePasswordToken("invalid", "password")

  private val mockedReadActor = TestActorRef(new EmptyActor)
  private val mockedWriteActor = TestActorRef(new EmptyActor)

  trait ActorMocks extends MapActorsConfiguration[ActorRef] {

    override def actorDetailsMap = Map(
      classOf[UsersReadModelActor] -> mockedReadActor,
      classOf[UsersWriteModelActor] -> mockedWriteActor
    )
  }

  val authenticationActor = TestActorRef(new AuthenticationActor
    with MockedActorProvider with MockedEventsourcedProcessorsProvider with ActorMocks with Clock {
    override def now = LAST_LOGIN_DATE
  })

  "Authentication Actor" when {
    "passed AuthorizationCommand from not existing User" must {
      "forbid authentication of that User" in {
        mockedReadActor given (sender => {
          case ReadUser(lookup) => sender ! UserNotFound()
        })

        val response = authenticationActor ?? AuthorizationCommand(invalidToken)

        response should equal(AuthorizationFailure())
      }
    }
    "passed AuthorizationCommand from existing User" must {
      "allow to authenticate User that exists" in {
        val updatedUser = givenAuthenticatedUser

        val response = authenticationActor ?? AuthorizationCommand(validToken)

        response should equal(AuthorizationSuccess(UserCredentials(updatedUser().sessionKey)))
      }
      "update that User's last login date" in {
        val updatedUser = givenAuthenticatedUser

        authenticationActor ?? AuthorizationCommand(validToken)

        updatedUser().lastLoginDate should be(Some(LAST_LOGIN_DATE))
      }
      def givenAuthenticatedUser = {
        mockedReadActor given (sender => {
          case ReadUser(lookup) => sender ! UserFound(validUser)
        })
        val userCaptor = Captor[User]()
        mockedWriteActor given (sender => {
          case msg: Message => {
            userCaptor.caught(msg.event.asInstanceOf[UpdateUser].user)
            sender ! UserUpdated()
          }
        })
        userCaptor
      }
    }
  }
}

object AuthenticationActorTest {
  val LAST_LOGIN_DATE = new DateTime(2010, 1, 1, 0, 0)
}