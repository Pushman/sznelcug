package services.actors.support

import akka.actor.{ActorRefFactory, Props, ActorRef, Actor}
import akka.routing.RoundRobinRouter
import reflect.ClassTag
import services.actors.{AuthenticationActor, UsersWriteModelActor, UsersReadModelActor, ServicesActor}

trait HasContext {

  protected def context: ActorRefFactory
}

trait ActorProvider {

  def actorFor[T <: Actor : ClassTag]: ActorRef

  def createActor[T <: Actor : ClassTag]: ActorRef
}

trait ActorsConfiguration[A] {

  def actorDetails(clazz: Class[_ <: Actor]): Option[A]
}

case class ActorDetails(path: String, name: Option[String], props: Props)

trait ConfigurableActorProvider extends ActorProvider with ActorsConfiguration[ActorDetails] with HasContext {

  override def actorFor[T <: Actor : ClassTag] =
    context.actorFor(pathFor(classFromTag))

  override def createActor[T <: Actor : ClassTag] =
    nameFor(classFromTag).map(name => context.actorOf(propsFor(classFromTag), name)).
      getOrElse(context.actorOf(propsFor(classFromTag)))

  private def classFromTag[T <: Actor : ClassTag]: Class[_ <: Actor] =
    implicitly[ClassTag[T]].runtimeClass.asInstanceOf[Class[_ <: Actor]]

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

  override def actorDetailsMap = {
    Map(
      (classOf[ServicesActor] -> ActorDetails("services", Some("services"),
        Props(new ServicesActor with DefaultActorProvider))),
      (classOf[UsersReadModelActor] -> ActorDetails("/user/services/usersReadModelActor", Some("usersReadModelActor"),
        Props(new UsersReadModelActor with LoggedActor).withRouter(defaultRouter))),
      (classOf[UsersWriteModelActor] -> ActorDetails("/user/services/usersWriteModelActor", Some("usersWriteModelActor"),
        Props(new UsersWriteModelActor))),
      (classOf[AuthenticationActor] -> ActorDetails("/user/services/authenticationActor", Some("authenticationActor"),
        Props(new AuthenticationActor with DefaultActorProvider with LoggedActor)))
    )
  }
}