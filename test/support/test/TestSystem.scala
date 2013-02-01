package support.test

import akka.actor.ActorSystem
import org.scalatest.{Suite, BeforeAndAfterAll}

trait TestSystem extends BeforeAndAfterAll {
  this: BeforeAndAfterAll with Suite =>

  implicit val system = ActorSystem("MySpec")

  override protected def afterAll() {
    system.shutdown()
  }
}