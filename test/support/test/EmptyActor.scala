package support.test

import akka.actor.Actor

class EmptyActor extends Actor {

  override def receive = Actor.emptyBehavior
}