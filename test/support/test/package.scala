package support

import reflect.ClassTag

package object test extends GivenSupport with BlockingAskSupport with PassedSupport {
  def instanceOf[T: ClassTag] = {
    new ClassMatcher(implicitly[ClassTag[T]].runtimeClass)
  }
}