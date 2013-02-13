package support.test

import akka.actor.Actor
import akka.testkit.TestActorRef
import org.scalatest.matchers.{ShouldMatchers, Matcher}

trait PassedSupport {

  implicit def passed[A <: Actor](actor: TestActorRef[A]) =
    new OngoingPassed(actor)

  final class OngoingPassed(actor: TestActorRef[_]) {

    def passed(message: Any) =
      new OngoingPassedWithMatcher(actor, ShouldMatchers.be(message))

    def passed[A](matcher: Matcher[Any]) =
      new OngoingPassedWithMatcher(actor, matcher)

    def passed[A](clazz: Class[A]) =
      new OngoingPassedWithMatcher(actor, new ClassMatcher(clazz))
  }

  final class OngoingPassedWithMatcher[A](actor: TestActorRef[_], matcher: Matcher[Any]) {

    def respond(body: => Any) {
      actor.underlying.become(behavior(body))
    }

    private def behavior(body: => Any): Actor.Receive = {
      case msg if matcher(msg).matches =>
        actor.underlying.sender ! body
    }

    def response(body: A => Any) {
      actor.underlying.become(behavior(body))
    }

    private def behavior(body: A => Any): Actor.Receive = {
      case msg if matcher(msg).matches =>
        actor.underlying.sender ! body(msg.asInstanceOf[A])
    }
  }

}