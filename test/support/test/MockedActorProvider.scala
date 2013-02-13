package support.test

import akka.actor.{ActorRef, Actor}
import reflect.ClassTag
import services.actors.support.{ActorsConfiguration, ActorProvider}


trait MockedActorProvider extends ActorProvider {
  this: ActorProvider with ActorsConfiguration[ActorRef] =>

  override def actorFor[T <: Actor : ClassTag] = actorConfiguration(classFromTag).get

  override def createActor[T <: Actor : ClassTag] = actorConfiguration(classFromTag).get

  private def classFromTag[T <: Actor : ClassTag]: Class[_ <: Actor] =
    implicitly[ClassTag[T]].runtimeClass.asInstanceOf[Class[_ <: Actor]]
}