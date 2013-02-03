package services.actors

import akka.actor.Actor
import support.ActorProvider

class ServicesActor extends Actor {
  provider: ActorProvider =>

  provider.createActor[UsersReadModelActor]
  provider.createActor[UsersWriteModelActor]
  provider.createActor[AuthenticationActor]

  override def receive: Receive = Actor.emptyBehavior
}