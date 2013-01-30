package services.actors

import akka.actor.{ActorRefFactory, Props, ActorRef, Actor}
import akka.routing.RoundRobinRouter

trait HasContext {

  protected def context: ActorRefFactory
}

trait ActorProvider {

  def actorFor(clazz: Class[_ <: Actor]): ActorRef

  def createActor(clazz: Class[_ <: Actor]): ActorRef
}

trait ActorsConfiguration {

  def propsFor(clazz: Class[_ <: Actor]): Props

  def nameFor(clazz: Class[_ <: Actor]): Option[String]

  def pathFor(clazz: Class[_ <: Actor]): String
}

trait DefaultActorProvider extends ActorProvider with DefaultActorsConfiguration {
  this: HasContext =>

  override def actorFor(clazz: Class[_ <: Actor]) =
    context.actorFor(pathFor(clazz))

  override def createActor(clazz: Class[_ <: Actor]) =
    nameFor(clazz).map(name => context.actorOf(propsFor(clazz), name)).getOrElse(context.actorOf(propsFor(clazz)))
}

trait DefaultActorsConfiguration extends ActorsConfiguration {

  private def defaultRouter: RoundRobinRouter = new RoundRobinRouter(1)

  private def actors = {
    Map[Class[_ <: Actor], (String, Option[String], Props)](
      (classOf[ServicesActor] ->("services", Some("services"),
        Props(new ServicesActor with DefaultActorProvider with HasContext))),
      (classOf[UsersReadModelActor] ->("/user/services/usersReadModelActor", Some("usersReadModelActor"),
        Props(new UsersReadModelActor).withRouter(defaultRouter))),
      (classOf[UsersWriteModelActor] ->("/user/services/usersWriteModelActor", Some("usersWriteModelActor"),
        Props(new UsersWriteModelActor))),
      (classOf[AuthenticationActor] ->("/user/services/authenticationActor", Some("authenticationActor"),
        Props(new AuthenticationActor with DefaultActorProvider with HasContext)))
    )
  }


  override def propsFor(clazz: Class[_ <: Actor]) = {
    actors.get(clazz).collect {
      case (path, name, creator) => creator
    }.getOrElse(throw new IllegalStateException(clazz.toString))
  }

  override def nameFor(clazz: Class[_ <: Actor]) = {
    actors.get(clazz).collect {
      case (path, name, creator) => name
    }.getOrElse(throw new IllegalStateException(clazz.toString))
  }

  override def pathFor(clazz: Class[_ <: Actor]) =
    actors.get(clazz).collect {
      case (path, name, creator) => path
    }.getOrElse(throw new IllegalStateException(clazz.toString))
}