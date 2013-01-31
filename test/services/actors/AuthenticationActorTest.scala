package services.actors

import org.scalatest.{BeforeAndAfterAll, WordSpec}
import akka.actor.{Actor, ActorSystem, ActorRef}
import akka.testkit.TestActorRef
import akka.pattern.ask
import concurrent.duration._
import concurrent.Await

class AuthenticationActorTest extends WordSpec with BeforeAndAfterAll {

  implicit val system = ActorSystem("MySpec")
  implicit val timeout = akka.util.Timeout(5 seconds)

  val mockedActor: ActorRef = TestActorRef(new Actor {

    override def receive = {
      case ReadUser(lookup) ⇒ sender ! UserNotFound(lookup)
    }
  })

  override protected def afterAll() {
    system.shutdown()
  }

  trait MockedActorProvider extends ActorProvider {

    def actorFor(clazz: Class[_ <: Actor]) = mockedActor
    def createActor(clazz: Class[_ <: Actor]) = mockedActor
  }

  val authenticationActor = TestActorRef(new AuthenticationActor with MockedActorProvider)
  "Authentication actor" must {
    "not allow to authenticate User that does not exist" in {
      val response = Await.result(authenticationActor ? AuthorizationCommand(UsernamePasswordToken("password", "password")), 5 seconds)
      assert(response === AuthorizationFailure())
    }
  }
}
