package support.test


import akka.actor.ActorRef
import akka.pattern.ask
import concurrent.duration._
import concurrent.Await

trait BlockingAskSupport {

  final class BlockingAsk(actor: ActorRef) {

    private val timeout = 1.seconds

    def ??(message: scala.Any) = {
      Await.result(ask(actor, message)(timeout), timeout)
    }
  }

  implicit def ??(actor: ActorRef) =
    new BlockingAsk(actor)
}