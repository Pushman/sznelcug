package services.actors.support

import akka.actor.Actor

trait LoggedActor extends Actor {

  private val log = akka.event.Logging(context.system, this)
  
  abstract override def receive = {
    case any => {
      log.debug("message: {}", any)
      super.receive(any)
    }
  }

  override def preStart() {
    log.debug("preStart")
    super.preStart()
  }

  override def postStop() {
    log.debug("postStop")
    super.postStop()
  }
}