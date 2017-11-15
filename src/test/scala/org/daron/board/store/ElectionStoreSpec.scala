package org.daron.board.store

import org.daron.board.RichFuture
import org.daron.board.models.ElectionId
import org.scalatest.{FlatSpec, Matchers}

import scala.util.Random

class ElectionStoreSpec extends FlatSpec with Matchers with RichFuture {

  def getStore = new ElectionSimpleStore

  it should "create election" in {
    val store = getStore
    val start: Long = System.currentTimeMillis()
    val end: Long = start + Random.nextInt(1000000)
    val election = store.create(start, end).await
    election.start shouldEqual start
    election.end shouldEqual end
  }

  it should "find election" in {
    val store = getStore
    val start: Long = System.currentTimeMillis()
    val end: Long = start + Random.nextInt(1000000)
    val election = store.create(start, end).await
    val validId = election.id
    val invalidId = ElectionId(validId.id + 1)

    val res1 =  store.find(validId).await
    res1 shouldBe defined
    res1.get.id shouldEqual validId
    res1.get.start shouldEqual start
    res1.get.end shouldEqual end

    store.find(invalidId).await shouldBe None
  }

}
