package services.actors.support

import akka.actor.{ActorRef, Actor}
import reflect.ClassTag

trait ActorProvider {

  def actorFor[T <: Actor : ClassTag]: ActorRef

  def createActor[T <: Actor : ClassTag]: ActorRef
}

trait ConfigurableActorProvider extends ActorProvider {
  this: ActorProvider with ActorDetailsActorsConfiguration with HasContext =>

  override def actorFor[T <: Actor : ClassTag] =
    context.actorFor(pathFor(classFromTag))

  override def createActor[T <: Actor : ClassTag] =
    nameFor(classFromTag) match {
      case Some(name) => context.actorOf(propsFor(classFromTag), name)
      case None => context.actorOf(propsFor(classFromTag))
    }

  private def classFromTag[T <: Actor : ClassTag]: Class[_ <: Actor] =
    implicitly[ClassTag[T]].runtimeClass.asInstanceOf[Class[_ <: Actor]]
}