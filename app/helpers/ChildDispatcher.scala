package helpers

import akka.actor._
import concurrent.duration._

trait ChildDispatcher {
  this: Actor =>

  def dispatchToChild(creator: => ActorRef => Actor)(implicit timeout: FiniteDuration): Receive = {
    case message => {
      implicit val ec = context.system.dispatcher
      val s = sender
      val child: ActorRef = context.actorOf(Props(creator(s)))
      context.system.scheduler.scheduleOnce(timeout) {
        child ! PoisonPill
      }
      child forward message
    }
  }
}