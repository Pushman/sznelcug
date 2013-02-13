package support.test

import akka.actor.{ActorRef, Actor}
import akka.testkit.TestActorRef

trait GivenSupport {

  final class Given[A <: Actor](actor: TestActorRef[A]) {

    def given(behavior: PartialFunction[Any, Any]) {
      actor.underlying.become(respondToSender(behavior))
    }

    def respondToSender(behavior: PartialFunction[Any, Any]): Actor.Receive = {
      case msg if behavior.isDefinedAt(msg) =>
        actor.underlying.sender ! behavior(msg)
    }

    def given(behavior: ActorRef => Actor.Receive) {
      actor.underlying.become(behaviorWithSender(behavior))
    }

    private def behaviorWithSender(body: ActorRef => Actor.Receive): Actor.Receive = {
      case message =>
        body(actor.underlying.sender)(message)
    }
  }

  implicit def given[A <: Actor](actor: TestActorRef[A]) =
    new Given(actor)
}