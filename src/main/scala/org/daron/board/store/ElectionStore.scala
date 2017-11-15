package org.daron.board.store

import java.util.concurrent.atomic.AtomicLong

import org.daron.board.models.{Election, ElectionId}

import scala.collection.concurrent.TrieMap
import scala.concurrent.Future
import scala.language.higherKinds

trait ElectionStore[F[_]] {

  def create(start: Long, end: Long): F[Election]

  def find(id: ElectionId): F[Option[Election]]

}

class ElectionSimpleStore extends ElectionStore[Future] {

  private val data = new TrieMap[ElectionId, Election]()
  private val idGenerator = new AtomicLong(0)

  override def create(start: Long, end: Long) = {
    val id = ElectionId(idGenerator.getAndIncrement)
    val election = Election(id, start, end)
    data.put(id, election)
    Future.successful(election)
  }

  override def find(id: ElectionId) = Future.successful(data.get(id))
}
