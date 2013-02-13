package services.actors.support

import akka.actor.{Props, Actor}

trait ActorsConfiguration[A] {

  def actorConfiguration(clazz: Class[_ <: Actor]): Option[A]
}

trait MapActorsConfiguration[A] extends ActorsConfiguration[A] {

  def actorDetailsMap: Map[Class[_ <: Actor], A]

  override def actorConfiguration(clazz: Class[_ <: Actor]): Option[A] = actorDetailsMap.get(clazz)
}

case class ActorDetails(path: String, name: Option[String], props: Props)

trait ActorDetailsActorsConfiguration extends ActorsConfiguration[ActorDetails] {

  def nameFor = getFromConfiguration(_.name) _

  def pathFor = getFromConfiguration(_.path) _

  def propsFor = getFromConfiguration(_.props) _

  private def getFromConfiguration[A](mapper: ActorDetails => A)(clazz: Class[_ <: Actor]): A =
    mapper(actorConfiguration(clazz).getOrElse(throw new IllegalStateException(clazz.toString)))
}