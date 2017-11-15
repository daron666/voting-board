package org.daron.board.store

import org.daron.board.RichFuture
import org.daron.board.models._
import org.scalatest.{FlatSpec, Matchers}

class VoteStoreSpec extends FlatSpec with Matchers with RichFuture {

  import scala.concurrent.ExecutionContext.Implicits.global

  def getStore = new VoteSimpleStore

  private val electionId = ElectionId(1010L)

  it should "create vote record" in {
    val ts = System.currentTimeMillis()
    val message = "some_random_string"
    val clientPk = "clientPk"
    val butlerPk = "butlerPk"
    val sign = "sign"
    val r = VoteRequest(electionId, message, SignatureData(clientPk, sign))
    val b = BoardData(ts, SignatureData(butlerPk, sign))
    val store = getStore
    val vr = store.create(r, b).await

    vr.electionId shouldEqual electionId
    vr.m shouldEqual message
    vr.voteTimestamp shouldEqual ts
    vr.signatureData.pk shouldEqual butlerPk
    vr.signatureData.data shouldEqual sign
    vr.voteHash shouldEqual store.hash(r.m, None)
  }

  it should "find vote record" in {
    val ts = System.currentTimeMillis()
    val message = "some_random_string"
    val clientPk = "clientPk"
    val butlerPk = "butlerPk"
    val sign = "sign"
    val r = VoteRequest(electionId, message, SignatureData(clientPk, sign))
    val b = BoardData(ts, SignatureData(butlerPk, sign))
    val store = getStore
    val vr = store.create(r, b).await

    val validId = vr.voteId
    val invalidId = VoteId(validId.id +1)

    store.find(validId).await shouldBe defined
    store.find(invalidId).await shouldBe None
  }

  it should "get all by electionId" in {
    val ts = System.currentTimeMillis()
    val message = "some_random_string"
    val clientPk = "clientPk"
    val butlerPk = "butlerPk"
    val sign = "sign"
    val electionId2 = electionId.copy(id = electionId.id + 1)
    val electionId3 = electionId.copy(id = electionId.id + 2)
    val r1 = VoteRequest(electionId, message, SignatureData(clientPk, sign))
    val r2 = VoteRequest(electionId2, message, SignatureData(clientPk, sign))
    val b = BoardData(ts, SignatureData(butlerPk, sign))
    val store = getStore
    val vr1 = store.create(r1, b).await
    val vr2 = store.create(r2, b).await


    val res1 = store.getAllByElectionId(electionId).await
    res1 should have length 1
    res1 shouldEqual List(vr1)

    val res2 = store.getAllByElectionId(electionId2).await
    res2 should have length 1
    res2 shouldEqual List(vr2)

    val res3 = store.getAllByElectionId(electionId3).await
    res3 should have length 0
  }

  it should "get lastHash" in {
    val ts = System.currentTimeMillis()
    val message = "some_random_string"
    val clientPk = "clientPk"
    val butlerPk = "butlerPk"
    val sign = "sign"
    val r1 = VoteRequest(electionId, message, SignatureData(clientPk, sign))
    val r2 = VoteRequest(electionId, message, SignatureData(clientPk, sign))
    val b = BoardData(ts, SignatureData(butlerPk, sign))
    val store = getStore
    val vr1 = store.create(r1, b).await
    val vr2 = store.create(r2, b).await

    val expectedHash = {
      val hash = store.hash(vr1.m, None)
      store.hash(vr2.m, Some(hash))
    }

    store.lastHash(electionId).await shouldBe Some(expectedHash)
  }

}
