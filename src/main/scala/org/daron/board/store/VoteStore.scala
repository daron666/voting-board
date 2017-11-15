package org.daron.board.store

import java.util.concurrent.atomic.AtomicLong

import org.daron.board.models._
import org.daron.board.service.RichString
import scorex.crypto.encode.Base64
import scorex.crypto.hash.Blake2b256

import scala.collection.concurrent.TrieMap
import scala.concurrent.{ExecutionContext, Future}
import scala.language.higherKinds

trait VoteStore[F[_]] {

  def create(r: VoteRequest, b: BoardData): F[VoteRecord]

  def find(voteId: VoteId): F[Option[VoteRecord]]

  def getAllByElectionId(electionId: ElectionId): F[List[VoteRecord]]

  def lastHash(electionId: ElectionId): F[Option[String]]

}

class VoteSimpleStore(implicit ec: ExecutionContext) extends VoteStore[Future] with RichString {

  private val data = new TrieMap[VoteId, VoteRecord]
  private val idGenerator = new AtomicLong(0)

  override def create(r: VoteRequest, b: BoardData) = lastHash(r.electionId).flatMap { prevHash =>
    val id = VoteId(idGenerator.getAndIncrement)
    val timestamp = b.voteTimestamp
    val electionId = r.electionId
    val m = r.m
    val signatureData = b.signatureData
    val hashString = hash(r.m, prevHash)
    val record = VoteRecord(id, timestamp, electionId, m, hashString, signatureData)
    data.put(id, record)
    Future.successful(record)
  }

  override def find(voteId: VoteId) = Future.successful(data.get(voteId))

  override def getAllByElectionId(electionId: ElectionId) = Future.successful{
    data.filter(_._2.electionId == electionId).values.toList.sortBy(_.voteId.id)
  }

  override def lastHash(electionId: ElectionId): Future[Option[String]] = Future.successful{
    data.filter(_._2.electionId == electionId).values.toList.sortBy(_.voteId.id).lastOption.map(_.voteHash)
  }

  //todo move from store to independent service
  def hash(message: String, prevHash: Option[String]): String = {
    val msg = Base64.decode(message.toBase64String)
    val toHash = prevHash.fold(msg) { old => Array.concat(msg, Base64.decode(old))}
    Base64.encode(Blake2b256.hash(toHash))
  }

}
