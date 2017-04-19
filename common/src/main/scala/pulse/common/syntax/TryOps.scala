package pulse.common.syntax
import cats.~>
import fs2.util.Attempt

import scala.util.{Either, Failure, Success, Try}

trait TryOps {
  implicit val transform = new (Try ~> Attempt) {
    override def apply[A](fa: Try[A]): Attempt[A] = fa match {
      case Success(c) => Right(c)
      case Failure(f) => Left (f)
    }
  }
}
