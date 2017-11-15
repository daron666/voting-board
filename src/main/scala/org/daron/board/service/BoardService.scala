package org.daron.board.service


import org.daron.board.store.{ElectionSimpleStore, ElectionStore, VoteSimpleStore, VoteStore}
import scorex.crypto.signatures.{Curve25519, PublicKey, Signature}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global


class BoardService(electionStore: ElectionSimpleStore, voteStore: VoteSimpleStore) extends RichString {
  private val seed = scorex.utils.Random.randomBytes()
  private val (privateKey, publicKey) = Curve25519.createKeyPair(seed)

  import org.daron.board.models._
  import scorex.crypto.encode.Base64

  def validate(r: VoteRequest): Future[Boolean] = Future.successful{
    val signature = Signature @@ Base64.decode(r.signatureData.data)
    val pk = PublicKey @@ Base64.decode(r.signatureData.pk)
    val messageToSign = Base64.decode(r.m.toBase64String)
    Curve25519.verify(signature, messageToSign, pk)
  }

  def sign(r: VoteRequest): Future[BoardData] = Future.successful{
    val signature = Curve25519.sign(privateKey, Base64.decode(r.m.toBase64String))
    val signatureData = SignatureData(Base64.encode(publicKey), Base64.encode(signature))
    val timestamp = System.currentTimeMillis
    BoardData(timestamp, signatureData)
  }

  def startElection(start: Long = System.currentTimeMillis(), end: Long = System.currentTimeMillis() + (24L * 60 * 60 * 1000)): Future[Election] = {
    electionStore.create(start, end)
  }

  def vote(r: VoteRequest): Future[VoteRecord] = for {
    validated <- validate(r)
    if validated
    b <- sign(r)
    vote <- voteStore.create(r, b)
  } yield vote

}
