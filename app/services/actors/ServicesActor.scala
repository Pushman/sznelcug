package services.actors

import akka.actor.Actor

class ServicesActor extends Actor {
  provider: ActorProvider =>

  provider.createActor(classOf[UsersReadModelActor])
  provider.createActor(classOf[UsersWriteModelActor])
  provider.createActor(classOf[AuthenticationActor])

  override def receive: Receive = Actor.emptyBehavior
}