package helpers

import akka.actor._
import concurrent.duration._

trait ChildDispatcher {
  this: Actor =>

  def dispatchToChild(creator: ActorRef => Props): Receive = {
    case message => {
      implicit val ec = context.system.dispatcher
      val s = sender
      val child: ActorRef = context.actorOf(creator(s))
      context.system.scheduler.scheduleOnce(5 seconds) {
        child ! PoisonPill
      }
      child forward message
    }
  }
}