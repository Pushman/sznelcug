package support.datetime

import org.joda.time.DateTime

trait Clock {

  def now: DateTime
}