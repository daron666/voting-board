package org.daron.board.service

import java.util.concurrent.atomic.AtomicLong

object Index {

  private val current = new AtomicLong(0)

  def get: Long = current.get()

  def inc: Long = current.incrementAndGet()

}
