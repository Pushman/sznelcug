package services.actors

import akka.actor.{ActorPath, ChildActorPath, Props, Actor}
import helpers.LoggedActor
import akka.routing.RoundRobinRouter


object ServicesActor {

  def actorName: String = "services"
  def actorPath: String = "/user/" + actorName
}

class ServicesActor extends Actor {

  context.actorOf(Props(new UsersReadModelActor with LoggedActor).withRouter(new RoundRobinRouter(0)), name = "usersReadModelActor")
  context.actorOf(Props(new UsersWriteModelActor).withRouter(new RoundRobinRouter(0)), name = "usersWriteModelActor")

  override def receive: Receive = Actor.emptyBehavior
}