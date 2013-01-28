package controllers.helpers

import play.api.libs.concurrent.Akka
import concurrent.Future
import play.api.mvc._
import play.api.mvc.Results._
import akka.pattern.after
import scala.concurrent.duration._
import play.api.libs.concurrent.Execution.Implicits._
import play.api.Play.current

object AsyncAction {

  implicit def timeout: FiniteDuration = 2.seconds

  implicit def onTimeout: Result = RequestTimeout(s"Unable sender complete action in $timeout")

  implicit def requestTimeout(timeout: FiniteDuration, onTimeout: Result): Future[Result] =
    after(timeout, Akka.system.scheduler) {
      Future.successful(onTimeout)
    }

  def apply(body: Request[_] => Future[Result])(implicit timeout: FiniteDuration, onTimeout: Result): Action[AnyContent] =
    Action {
      implicit request => Async {
        val result: Future[Result] = body(request)

        Future firstCompletedOf Seq(result, requestTimeout(timeout, onTimeout))
      }
    }
}