package controllers.helpers

import play.api.data.Form
import concurrent.{Promise, Future}

object FormHelpers {

  implicit def toExtendedForm[A](form: Form[A]) = new ExtendedForm(form)
}

case class ExtendedForm[A](form: Form[A]) {

  def addValidation[B](validator: Form[A] => Future[Form[A]]): Future[Form[A]] =
    form.fold(
      Promise.successful(_).future,
      value => validator(form)
    )
}