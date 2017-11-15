package org.daron.board

package object models {

  case class SignatureData(pk: String, data: String)

  case class ElectionId(id: Long) extends AnyVal

  case class Election(id: ElectionId, start: Long, end: Long)

  case class VoteRequest(electionId: ElectionId, m: String, signatureData: SignatureData)

  case class BoardData(voteTimestamp: Long, signatureData: SignatureData)

  case class VoteId(id: Long) extends AnyVal

  case class VoteRecord(voteId: VoteId, voteTimestamp: Long, electionId: ElectionId, m: String, voteHash: String, signatureData: SignatureData)

}
