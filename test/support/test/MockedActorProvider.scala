package support.test

import services.actors.{ActorsConfiguration, ActorProvider}
import akka.actor.{ActorRef, Actor}


trait MockedActorProvider extends ActorProvider with ActorsConfiguration[ActorRef] {

  def actorFor(clazz: Class[_ <: Actor]) = actorDetails(clazz).get

  def createActor(clazz: Class[_ <: Actor]) = actorDetails(clazz).get
}