package support.datetime

import org.joda.time.DateTime

trait RealClock extends Clock {

  override def now = new DateTime()
}