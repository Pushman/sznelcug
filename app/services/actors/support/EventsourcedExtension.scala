package services.actors.support

import akka.actor.{Actor, ActorRef}
import org.eligosource.eventsourced.core.{EventsourcingExtension, Journal}
import org.eligosource.eventsourced.journal.journalio.JournalioJournalProps
import java.io.File
import reflect.ClassTag

trait EventsourcedExtension {
  this: EventsourcedExtension with HasSystem =>

  def journal: ActorRef = Journal(JournalioJournalProps(new File("target/example-1")))(system)

  def eventsourcedExtension = EventsourcingExtension(system, journal)
}

trait EventsourcedProcessorsProvider {

  def processorOf[T <: Actor : ClassTag]: ActorRef
}

trait ConfigurableEventsourcedProcessorsProvider extends EventsourcedProcessorsProvider {
  this: EventsourcedProcessorsProvider with EventsourcedExtension with HasContext with ActorsConfiguration[ActorDetails] =>

  override def processorOf[T <: Actor : ClassTag]: ActorRef =
    eventsourcedExtension.processorOf(actorConfiguration(classFromTag).get.props)(context)

  private def classFromTag[T <: Actor : ClassTag]: Class[_ <: Actor] =
    implicitly[ClassTag[T]].runtimeClass.asInstanceOf[Class[_ <: Actor]]
}