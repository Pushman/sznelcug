package support.test


case class Captor[A]() {
  private var value: Option[A] = None

  def caught(s: A) {
    value = Some(s)
  }

  def get = value.get

  def apply() = get
}
