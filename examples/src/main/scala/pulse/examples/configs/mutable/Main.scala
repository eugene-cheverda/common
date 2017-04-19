package pulse.examples.configs.mutable

import java.io.File

import eu.timepit.refined.auto._
import fs2.interop.cats._
import fs2.{Stream, Task}
import org.log4s
import pulse.common.exceptions._
import pulse.common.{logging, _}
import pulse.config.readers._
import pulse.config.syntax._
import pulse.config.typesafe._
import pulse.config.{Conf, Source}

object Main extends Runner {

  implicit val L = log4s.getLogger

  val reader = for {
    _ <- conf
    a <- get[String]("config.testA")
    b <- get[String]("config.testB")
    s <- subconfig("config.subconfig")
    c <- s.get[String]("testC")
  } yield (a,b,c)

  def program(from: File) = for {
    ce <- Conf.mutable(Source.FileSource(from))
    c <- ce.fold(fa => {
        logging.error(s"Unable to parse file: ${from.getName}, exception: ${fa.getCause}").unsafeRun()
        Stream.empty
      }, fb => Stream.emit(fb))
    v <- Stream.eval(reader(c))
    _ <- Stream.eval(logging.info(s"$v"))
  } yield ()

  protected def run(args: List[String]): Task[Unit] = args match {
    case h :: _ => program(new File(h)).run
    case Nil    => Task.fail(NotSupportedException("filename is required"))
  }
}
