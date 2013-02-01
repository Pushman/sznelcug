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

trait ActorsConfiguration[A] {

  def actorDetails(clazz: Class[_ <: Actor]): Option[A]
}

case class ActorDetails(path: String, name: Option[String], props: Props)

trait ConfigurableActorProvider extends ActorProvider with ActorsConfiguration[ActorDetails] with HasContext {

  override def actorFor(clazz: Class[_ <: Actor]) =
    context.actorFor(pathFor(clazz))

  override def createActor(clazz: Class[_ <: Actor]) =
    nameFor(clazz).map(name => context.actorOf(propsFor(clazz), name)).getOrElse(context.actorOf(propsFor(clazz)))

  private def nameFor = getFromConfiguration(_.name) _

  private def pathFor = getFromConfiguration(_.path) _

  private def propsFor = getFromConfiguration(_.props) _

  private def getFromConfiguration[A](mapper: ActorDetails => A)(clazz: Class[_ <: Actor]): A =
    actorDetails(clazz).map(mapper).getOrElse(throw new IllegalStateException(clazz.toString))
}

trait DefaultActorProvider extends ConfigurableActorProvider with DefaultActorsConfiguration

trait MapActorsConfiguration[A] extends ActorsConfiguration[A] {

  def actorDetailsMap: Map[Class[_ <: Actor], A]
  
  override def actorDetails(clazz: Class[_ <: Actor]): Option[A] = actorDetailsMap.get(clazz)
}

trait DefaultActorsConfiguration extends MapActorsConfiguration[ActorDetails] {

  private def defaultRouter: RoundRobinRouter = new RoundRobinRouter(1)

  override def actorDetailsMap  = {
    Map(
      (classOf[ServicesActor] -> ActorDetails("services", Some("services"),
        Props(new ServicesActor with DefaultActorProvider))),
      (classOf[UsersReadModelActor] -> ActorDetails("/user/services/usersReadModelActor", Some("usersReadModelActor"),
        Props(new UsersReadModelActor).withRouter(defaultRouter))),
      (classOf[UsersWriteModelActor] -> ActorDetails("/user/services/usersWriteModelActor", Some("usersWriteModelActor"),
        Props(new UsersWriteModelActor))),
      (classOf[AuthenticationActor] -> ActorDetails("/user/services/authenticationActor", Some("authenticationActor"),
        Props(new AuthenticationActor with DefaultActorProvider)))
    )
  }
}