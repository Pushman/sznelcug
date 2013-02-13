package support.test

import services.actors.support.{ActorsConfiguration, EventsourcedProcessorsProvider}
import akka.actor.{Actor, ActorRef}
import reflect.ClassTag

trait MockedEventsourcedProcessorsProvider extends EventsourcedProcessorsProvider {
  this: EventsourcedProcessorsProvider with ActorsConfiguration[ActorRef] =>

  override def processorOf[T <: Actor : ClassTag] = actorConfiguration(classFromTag).get

  private def classFromTag[T <: Actor : ClassTag]: Class[_ <: Actor] =
    implicitly[ClassTag[T]].runtimeClass.asInstanceOf[Class[_ <: Actor]]
}
