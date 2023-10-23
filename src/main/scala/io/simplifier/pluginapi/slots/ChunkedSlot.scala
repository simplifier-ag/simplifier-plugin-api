package io.simplifier.pluginapi.slots

import akka.actor.ActorSelection.toScala
import akka.actor.{Actor, ActorRef, ActorSelection, actorRef2Scala}
import akka.pattern.ask
import akka.util.Timeout
import io.simplifier.pluginapi._

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.language.implicitConversions

/**
 * Chunk splitting support trait.
 * @author Christian Simon
 */
trait JsonChunkSupport {

  /** Reference to chunk helper */
  def chunkHelper: ChunkHelper

  /** Cache for chunked JSON messages. */
  protected val chunkedCache: mutable.Map[String, Array[JSONChunked]] = mutable.Map.empty

  /** Cache for chunked redirectToPlugin messages. */
  protected val chunkedRedirectCache: mutable.Map[String, Array[RedirectToPluginChunked]] = mutable.Map.empty

  /** Cache for chunked HttpPost messages. */
  protected val chunkedHttpPostCache: mutable.Map[String, Array[HttpPostChunked]] = mutable.Map.empty

  /** Cache for chunked HttpPostResponse messages. */
  protected val chunkedHttpPostResponseCache: mutable.Map[String, Array[HttpPostResponseChunked]] = mutable.Map.empty

  /**
   * Handle received JSONChunked. If the message is completed with this chunk, delegate the combined message.
   * @param chunked received chunk
   * @param sender sender actor to send AKN messages for the received chunk
   * @param receive handler for the fully combined message
   */
  protected def receiveJSONChunked(chunked: JSONChunked, sender: => ActorRef)(receive: JSON => Unit): Unit = {
    //println(s"---[R] Received Chunk ${chunked.chunk}/${chunked.chunks} from ${chunked.chunkSession}")
    if (chunkedCache contains chunked.chunkSession) {
      val cache = chunkedCache(chunked.chunkSession)
      cache(chunked.chunk - 1) = chunked
      if (chunkHelper chunksFinished cache) {
        //println(s"---[R] Chunks finished: ${chunked.chunkSession}")
        val jsonOpt = chunkHelper joinChunks cache
        if (jsonOpt.isDefined) {
          receive(jsonOpt.get)
        } else {
          println("Error: All chunks transmitted, but message could not be restored")
        }
        chunkedCache.remove(chunked.chunkSession)
      } else {
        sender ! AKN
      }
    } else {
      val cache = new Array[JSONChunked](chunked.chunks)
      cache(chunked.chunk - 1) = chunked
      chunkedCache(chunked.chunkSession) = cache
      sender ! AKN
    }
  }

  /**
   * Handle received JSONChunkedRequest. Answer the sender with the requested chunk.
   * If the requested chunk was the last on for the chunk session, remove the temporary data from the cache.
   * @param request received chunk request
   * @param sender sender actor to answer to
   */
  protected def receiveJSONChunkedRequest(request: JSONChunkedRequest, sender: => ActorRef): Unit = {
    val JSONChunkedRequest(chunkSession, chunk, last) = request
    //println(s"---[R] Received Request for Chunk $chunk from $chunkSession (last: ${last})")
    if (chunkedCache contains chunkSession) {
      val cache = chunkedCache(chunkSession)
      if (chunk >= 1 && chunk <= cache.length) {
        //println(s"---[S] Send Chunk $chunk/${cache.length} from $chunkSession")
        sender ! cache(chunk - 1)
      } else {
        sender ! ErrorMessage(s"invalid chunk $chunk.")
      }
      if (last) {
        chunkedCache.remove(chunkSession)
      }
    } else {
      sender ! ErrorMessage(s"chunk session $chunkSession not found.")
    }
  }

  /**
   * Handle received redirectToPluginChunked. If the message is completed with this chunk, delegate the combined message.
   * @param chunked received chunk
   * @param sender sender actor to send AKN messages for the received chunk
   * @param receive handler for the fully combined message
   */
  protected def receiveRedirectToPluginChunked(chunked: RedirectToPluginChunked, sender: => ActorRef)(receive: RedirectToPlugin => Unit): Unit = {
    //println(s"---[R] Received Chunk ${chunked.chunk}/${chunked.chunks} from ${chunked.chunkSession}")
    if (chunkedRedirectCache contains chunked.chunkSession) {
      val cache = chunkedRedirectCache(chunked.chunkSession)
      cache(chunked.chunk - 1) = chunked
      if (chunkHelper chunksFinished cache) {
        //println(s"---[R] Chunks finished: ${chunked.chunkSession}")
        val redirectOpt = chunkHelper joinChunks cache
        if (redirectOpt.isDefined) {
          receive(redirectOpt.get)
        } else {
          println("Error: All chunks transmitted, but message could not be restored")
        }
        chunkedRedirectCache.remove(chunked.chunkSession)
      } else {
        sender ! AKN
      }
    } else {
      val cache = new Array[RedirectToPluginChunked](chunked.chunks)
      cache(chunked.chunk - 1) = chunked
      chunkedRedirectCache(chunked.chunkSession) = cache
      sender ! AKN
    }
  }

  /**
   * Handle received HttpPostChunked. If the message is completed with this chunk, delegate the combined message.
   * @param chunked received chunk
   * @param sender sender actor to send AKN messages for the received chunk
   * @param receive handler for the fully combined message
   */
  protected def receiveHttpPostChunked(chunked: HttpPostChunked, sender: => ActorRef)(receive: HttpPost => Unit): Unit = {
    //println(s"---[R] Received Chunk ${chunked.chunk}/${chunked.chunks} from ${chunked.chunkSession}")
    if (chunkedHttpPostCache contains chunked.chunkSession) {
      val cache = chunkedHttpPostCache(chunked.chunkSession)
      cache(chunked.chunk - 1) = chunked
      if (chunkHelper chunksFinished cache) {
        //println(s"---[R] Chunks finished: ${chunked.chunkSession}")
        val jsonOpt = chunkHelper joinChunks cache
        if (jsonOpt.isDefined) {
          receive(jsonOpt.get)
        } else {
          println("Error: All chunks transmitted, but message could not be restored")
        }
        chunkedHttpPostCache.remove(chunked.chunkSession)
      } else {
        sender ! AKN
      }
    } else {
      val cache = new Array[HttpPostChunked](chunked.chunks)
      cache(chunked.chunk - 1) = chunked
      chunkedHttpPostCache(chunked.chunkSession) = cache
      sender ! AKN
    }
  }

  /**
   * Handle received HttpPostResponseChunkedRequest. Answer the sender with the requested chunk.
   * If the requested chunk was the last on for the chunk session, remove the temporary data from the cache.
   * @param request received chunk request
   * @param sender sender actor to answer to
   */
  protected def receiveHttpPostResponseChunkedRequest(request: HttpPostResponseChunkedRequest, sender: => ActorRef): Unit = {
    val HttpPostResponseChunkedRequest(chunkSession, chunk, last) = request
    //println(s"---[R] Received Request for Chunk $chunk from $chunkSession (last: ${last})")
    if (chunkedHttpPostResponseCache contains chunkSession) {
      val cache = chunkedHttpPostResponseCache(chunkSession)
      if (chunk >= 1 && chunk <= cache.length) {
        //println(s"---[S] Send Chunk $chunk/${cache.length} from $chunkSession")
        sender ! cache(chunk - 1)
      } else {
        sender ! ErrorMessage(s"invalid chunk $chunk.")
      }
      if (last) {
        chunkedHttpPostResponseCache.remove(chunkSession)
      }
    } else {
      sender ! ErrorMessage(s"chunk session $chunkSession not found.")
    }
  }

  /** Implicit conversion of ActorRef to make ?? and !! operators available. */
  implicit protected def augmentActorRef(actorRef: ActorRef): ChunkedActorRef = new ChunkedActorRef(new ActorRefMessageSupport(actorRef))

  /** Implicit conversion of ActorSelection to make ?? and !! operators available. */
  implicit protected def augmentActorSel(actorSel: ActorSelection): ChunkedActorRef = new ChunkedActorRef(new ActorSelMessageSupport(actorSel))

  /**
   * Abstraction for objects having the tell (!) and ask(?) operator
   * (as ActorRef and ActorSelection have no common interface/trait providing those operators).
   */
  trait AkkaMessageSupport {

    def !(message: Any)(implicit sender: ActorRef = Actor.noSender): Unit

    def ?(message: Any)(implicit timeout: Timeout): Future[Any]

  }

  /**
   * AkkaMessageSupport implementation with ActorRef.
   */
  class ActorRefMessageSupport(delegate: ActorRef) extends AkkaMessageSupport {

    override def !(message: Any)(implicit sender: ActorRef = Actor.noSender): Unit = delegate ! message

    override def ?(message: Any)(implicit timeout: Timeout): Future[Any] = delegate ? message

  }

  /**
   * AkkaMessageSupport implementation with ActorSelection.
   */
  class ActorSelMessageSupport(delegate: ActorSelection) extends AkkaMessageSupport {

    override def !(message: Any)(implicit sender: ActorRef = Actor.noSender): Unit = delegate ! message

    override def ?(message: Any)(implicit timeout: Timeout): Future[Any] = delegate ? message

  }

  /**
   * ActorRef/ActorSelection extension with two new additional operators "!!" and "??".
   */
  protected class ChunkedActorRef(delegate: AkkaMessageSupport) {

    /**
     * Send with support for chunked messages. Same semantics as "!" operator, only with automatic chunk handling.
     */
    def !!(message: Any)(implicit sender: ActorRef = Actor.noSender): Unit = message match {
      case JSON(item, userSession) =>
        chunkHelper.splitJsonMessage(item, userSession) match {
          case Left(json) => delegate ! json
          case Right(chunked) =>
            val first = chunked.head
            chunkedCache += (first.chunkSession -> chunked.toArray)
            //println(s"---[S] Send Initial Chunk ${first.chunk}/${first.chunks} from ${first.chunkSession}")
            delegate ! first
        }
      case httpPostResponse: HttpPostResponse =>
        chunkHelper.splitHttpPostResponseMessage(httpPostResponse) match {
          case Left(response) => delegate ! response
          case Right(chunked) =>
            val first = chunked.head
            chunkedHttpPostResponseCache += (first.chunkSession -> chunked.toArray)
            //println(s"---[S] Send Initial Chunk ${first.chunk}/${first.chunks} from ${first.chunkSession}")
            delegate ! first
        }
      case other => delegate ! other
    }

    /**
     * Ask with support for chunked messages. Same semantics as "?" operator, only with automatic chunk handling.
     */
    def ??(message: Any)(implicit timeout: Timeout): Future[Any] = {
      val mapResult: Any => Future[Any] = {
        case chunked: JSONChunked =>
          val cache = new Array[JSONChunked](chunked.chunks)
          cache(chunked.chunk - 1) = chunked
          chunkedCache(chunked.chunkSession) = cache
          val requestRange = Range(1, cache.length + 1).filter { _ != chunked.chunk }
          val secondChunk = requestRange.head
          var future = {
            //println(s"---[S] Request Chunk ${secondChunk}/${chunked.chunks} from ${chunked.chunkSession}")
            delegate ? JSONChunkedRequest(chunked.chunkSession, secondChunk, secondChunk == requestRange.last)
          }
          val remainingRange = requestRange drop 1
          for (chunk <- remainingRange) {
            future = future flatMap {
              case nextChunked: JSONChunked =>
                cache(nextChunked.chunk - 1) = nextChunked
                //println(s"---[S] Request Chunk ${chunk}/${chunked.chunks} from ${chunked.chunkSession}")
                delegate ? JSONChunkedRequest(chunked.chunkSession, chunk, chunk == requestRange.last)
              case other =>
                throw new IllegalStateException(s"Invalid response: $other")
            }
          }
          future map {
            case lastChunked: JSONChunked =>
              cache(lastChunked.chunk - 1) = lastChunked
              chunkHelper.joinChunks(cache) match {
                case None       => throw new IllegalStateException(s"Chunks could not be reconstructed")
                case Some(json) => json
              }
            case other =>
              throw new IllegalStateException(s"Invalid response: $other")
          }
        case chunked: HttpPostResponseChunked =>
          val cache = new Array[HttpPostResponseChunked](chunked.chunks)
          cache(chunked.chunk - 1) = chunked
          chunkedHttpPostResponseCache(chunked.chunkSession) = cache
          val requestRange = Range(1, cache.length + 1).filter { _ != chunked.chunk }
          val secondChunk = requestRange.head
          var future = {
            //println(s"---[S] Request Chunk ${secondChunk}/${chunked.chunks} from ${chunked.chunkSession}")
            delegate ? HttpPostResponseChunkedRequest(chunked.chunkSession, secondChunk, secondChunk == requestRange.last)
          }
          val remainingRange = requestRange drop 1
          for (chunk <- remainingRange) {
            future = future flatMap {
              case nextChunked: HttpPostResponseChunked =>
                cache(nextChunked.chunk - 1) = nextChunked
                //println(s"---[S] Request Chunk ${chunk}/${chunked.chunks} from ${chunked.chunkSession}")
                delegate ? HttpPostResponseChunkedRequest(chunked.chunkSession, chunk, chunk == requestRange.last)
              case other =>
                throw new IllegalStateException(s"Invalid response: $other")
            }
          }
          future map {
            case lastChunked: HttpPostResponseChunked =>
              cache(lastChunked.chunk - 1) = lastChunked
              chunkHelper.joinChunks(cache) match {
                case None       => throw new IllegalStateException(s"Chunks could not be reconstructed")
                case Some(json) => json
              }
            case other =>
              throw new IllegalStateException(s"Invalid response: $other")
          }
        case other => Future.successful(other)
      }
      message match {
        case JSON(item, userSession) =>
          chunkHelper.splitJsonMessage(item, userSession) match {
            case Left(json) => (delegate ? json) flatMap mapResult
            case Right(chunked) =>
              val first = chunked.head
              var future = delegate ? first
              for (chunk <- Range(2, chunked.length + 1)) {
                future = future flatMap {
                  case AKN => delegate ? chunked(chunk - 1)
                  case other =>
                    throw new IllegalStateException(s"Invalid response: $other")
                }
              }
              future flatMap mapResult
          }
        case httpPost: HttpPost =>
          chunkHelper.splitHttpPostMessage(httpPost) match {
            case Left(post) => (delegate ? post) flatMap mapResult
            case Right(chunked) =>
              val first = chunked.head
              var future = delegate ? first
              for (chunk <- Range(2, chunked.length + 1)) {
                future = future flatMap {
                  case AKN => delegate ? chunked(chunk - 1)
                  case other =>
                    throw new IllegalStateException(s"Invalid response: $other")
                }
              }
              future flatMap mapResult
          }
        case redirect: RedirectToPlugin =>
          chunkHelper.splitRedirectMessage(redirect) match {
            case Left(redirectTo) => (delegate ? redirectTo) flatMap mapResult
            case Right(chunked) =>
              val first = chunked.head
              var future = delegate ? first
              for (chunk <- Range(2, chunked.length + 1)) {
                future = future flatMap {
                  case AKN => delegate ? chunked(chunk - 1)
                  case other =>
                    throw new IllegalStateException(s"Invalid response: $other")
                }
              }
              future flatMap mapResult
          }
        case other => (delegate ? other) flatMap mapResult
      }
    }
  }
}

/**
 * Slot with chunked support.
 * @author Christian Simon
 */
trait ChunkedSlot extends Slot with JsonChunkSupport {

  override def receive: Receive = {
    case chunked: JSONChunked =>
      receiveJSONChunked(chunked, sender) { receive(_) }
    case chunkedRequest: JSONChunkedRequest =>
      receiveJSONChunkedRequest(chunkedRequest, sender)
    case other => super.receive(other)
  }

}

/**
 * HttpSlot with chunked support.
 * @author Christian Simon
 */
trait ChunkedHttpSlot extends HttpSlot with JsonChunkSupport {

  override def receive: Receive = {
    case HttpPost(item, userSession) =>
      sender !! slot(item)(userSession)
    case chunkedHttpPost: HttpPostChunked =>
      receiveHttpPostChunked(chunkedHttpPost, sender) { receive(_) }
    case chunkedRequest: HttpPostResponseChunkedRequest =>
      receiveHttpPostResponseChunkedRequest(chunkedRequest, sender)
    case other => super.receive(other)
  }

}

/**
 * AssetHandler with chunked support.
 * @author Christian Simon
 */
trait ChunkedAssetHandler extends AssetHandler with JsonChunkSupport {

  override def receive: Receive = {
    case AssetRequest(path) =>
      sender !! handleAssetResult(path)
    case chunkedRequest: HttpPostResponseChunkedRequest =>
      receiveHttpPostResponseChunkedRequest(chunkedRequest, sender)
    case other => super.receive(other)
  }

}
