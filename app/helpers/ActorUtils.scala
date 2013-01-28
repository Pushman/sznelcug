package helpers

import akka.actor._
import akka.pattern.ask
import concurrent.Future
import concurrent.duration._
import helpers.Forward

object ActorUtils {

  import play.api.Play.current
  import play.api.libs.concurrent.Akka
  import play.api.libs.concurrent.Execution.Implicits.defaultContext

  def withActor(creator: ActorRef => ActorRef)(message: Any): Future[Any] = {
    implicit val timeout = akka.util.Timeout(5 seconds)
    val promiseActor = Akka.system.actorOf(Props(new PromiseActor()))
    val createdActor = creator(promiseActor)

    val future: Future[Any] = promiseActor ? Forward(createdActor, message)
    future andThen {
      case _ => createdActor ! PoisonPill; promiseActor ! PoisonPill
    }
  }
}

class PromiseActor extends Actor {

  import context.{become, unbecome}

  def receive = {
    case Forward(target, message) => {
      become(awaitingForResponse(sender))
      target ! message
    }
  }

  def awaitingForResponse(replyTo: ActorRef): Receive = {
    case message => {
      unbecome()
      replyTo forward message
    }
  }
}

case class Forward(target: ActorRef, message: Any)