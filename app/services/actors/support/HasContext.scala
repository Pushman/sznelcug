package services.actors.support

import akka.actor.ActorRefFactory

trait HasContext {

  protected def context: ActorRefFactory
}
