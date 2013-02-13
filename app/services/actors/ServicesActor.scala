package services.actors

import akka.actor.Actor
import support.ActorProvider

class ServicesActor extends Actor {
  provider: ActorProvider =>

  override def preStart() {
    provider.createActor[UsersReadModelActor]
    provider.createActor[AuthenticationActor]
  }

  override def receive: Receive = Actor.emptyBehavior
}