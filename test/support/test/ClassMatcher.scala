package support.test

import org.scalatest.matchers.{MatchResult, Matcher}
import reflect.ClassTag

object ClassMatcher {
  def instanceOf[T: ClassTag] = {
    new ClassMatcher(implicitly[ClassTag[T]].runtimeClass)
  }
}

class ClassMatcher[T](clazz: Class[T]) extends Matcher[Any] {

  def apply(left: Any) =
    MatchResult(clazz.isInstance(left), "is instance", "is not instance")
}