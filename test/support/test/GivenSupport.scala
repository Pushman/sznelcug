package support.test

import akka.actor.{ActorRef, Actor}
import akka.testkit.TestActorRef

object GivenSupport {

  final class Given[A <: Actor](actor: TestActorRef[A]) {

    def given(behavior: Actor.Receive) {
      actor.underlying.become(behavior)
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