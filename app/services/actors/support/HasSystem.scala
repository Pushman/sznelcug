package services.actors.support

import akka.actor.ActorSystem

trait HasSystem extends HasContext {

  protected def system: ActorSystem
}