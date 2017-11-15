package org.daron.board

import scala.concurrent.{Await, Future}

trait RichFuture {

  implicit class FutureWithOps[A](f: Future[A]) {
    def await: A = Await.result[A](f, scala.concurrent.duration.Duration.Inf)
  }

}
