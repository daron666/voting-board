package org.daron.board

import org.daron.board.models._
import org.daron.board.service.{BoardService, RichString}
import org.daron.board.store.{ElectionSimpleStore, VoteSimpleStore}
import org.scalatest.{FlatSpec, Matchers}
import scorex.crypto.encode.Base64
import scorex.crypto.signatures.{Curve25519, PrivateKey, PublicKey}

class MainFlowSpec extends FlatSpec with Matchers with RichFuture with RichString {

  import scala.concurrent.ExecutionContext.Implicits.global

  def getEstore = new ElectionSimpleStore
  def getVstore = new VoteSimpleStore


  case class KeyPair(privateK: PrivateKey, publicK: PublicKey)

  it should "proc the happy path" in {

    def createVoteRequest(electionId: ElectionId)(kp: KeyPair, m: String): VoteRequest = {
      val messageToSign = Base64.decode(m.toBase64String)
      val sign = Curve25519.sign(kp.privateK, messageToSign)
      val signatureData = SignatureData(Base64.encode(kp.publicK), Base64.encode(sign))
      VoteRequest(electionId, m, signatureData)
    }

    val eStore = getEstore
    val vStore = getVstore

    val service = new BoardService(eStore, vStore)

    val voterKeys1 = (KeyPair.apply _).tupled(Curve25519.createKeyPair(scorex.utils.Random.randomBytes()))
    val voterKeys2 = (KeyPair.apply _).tupled(Curve25519.createKeyPair(scorex.utils.Random.randomBytes()))
    val voterKeys3 = (KeyPair.apply _).tupled(Curve25519.createKeyPair(scorex.utils.Random.randomBytes()))
    val voterKeys4 = (KeyPair.apply _).tupled(Curve25519.createKeyPair(scorex.utils.Random.randomBytes()))
    val butlerKeys = (KeyPair.apply _).tupled(Curve25519.createKeyPair(scorex.utils.Random.randomBytes()))


    val start = System.currentTimeMillis()
    val end = start + 1000000

    //first of all we need to establish election
    val election = eStore.create(start, end).await
    val electionId = election.id

    //now we got an election id so we can participate in election

    val create = createVoteRequest(electionId) _

    val r1 = create(voterKeys1, "1")
    val r2 = create(voterKeys2, "2")
    val r3 = create(voterKeys3, "3")
    val r4 = create(voterKeys4, "4")

    //voting itself
    val vr1 = service.vote(r1).await
    val vr2 = service.vote(r2).await
    val vr3 = service.vote(r3).await
    val vr4 = service.vote(r4).await

    //total number should be the same as number of successful votes
    vStore.getAllByElectionId(electionId).await should have length 4


    //verifying hash chain
    val hash1 = vStore.hash("1", None)
    hash1 shouldEqual vr1.voteHash

    val hash2 = vStore.hash("2", Some(hash1))
    hash2 shouldEqual vr2.voteHash

    val hash3 = vStore.hash("3", Some(hash2))
    hash3 shouldEqual vr3.voteHash

    val hash4 = vStore.hash("4", Some(hash3))
    hash4 shouldEqual vr4.voteHash

  }

}
