package services.actors

import akka.actor.Actor

class ServicesActor extends Actor {
  provider: ActorProvider =>

  provider.createActor(classOf[UsersReadModelActor])
  provider.createActor(classOf[UsersWriteModelActor])

  override def receive: Receive = Actor.emptyBehavior
}