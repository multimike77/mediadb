package controllers

import java.io.{FileOutputStream, File}
import javax.inject.Singleton

import com.typesafe.config.{ConfigFactory, Config}
import org.slf4j.{LoggerFactory, Logger}
import play.api.libs.iteratee.{Enumeratee, Iteratee}
import play.api.libs.ws.WS
import play.api.mvc.{Result, Action, Controller}
import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent.Future

/**
 * Simple controller for serving images for the Movies/TV Shows
 */
@Singleton
class Images extends Controller {
  private final val logger: Logger = LoggerFactory.getLogger("movies")
  private val config: Config = ConfigFactory.load()
  private val imageDir = config.getString("application.imageDir")

  def loadImage(path: String) = Action.async {
    val imagePath = imageDir + path
    val image = new File(imagePath)

    if (image.exists() && image.length() > 0) {
      Future {
        Ok.sendFile(content = image, inline = true).withHeaders(CACHE_CONTROL -> "public, max-age=31536000")
      }
    } else {
      val futureImage: Future[File] = downloadImage(image, path)
      futureImage.map { img =>
        Ok.sendFile(content = img, inline = true).withHeaders(CACHE_CONTROL -> "public, max-age=31536000")
      }
    }
  }


  def downloadImage(image: File, imagePath: String): Future[File] = {
    val imagesBaseUrl = config.getString("application.imageBaseUrl")
    val imageUrl = imagesBaseUrl + imagePath


    val futureResponse = WS.url(imageUrl).getStream()
    val downloadedFile: Future[File] = futureResponse.flatMap {
      case (headers, body) =>
        val outputStream = new FileOutputStream(image)

        // The iteratee that writes to the output stream
        val iteratee = Iteratee.foreach[Array[Byte]] { bytes =>
          outputStream.write(bytes)
        }

        // Feed the body into the iteratee
        (body |>>> iteratee).andThen {
          case result =>
            // Close the output stream whether there was an error or not
            outputStream.close()
            // Get the result or rethrow the error
            result.get
        }.map(_ => image)
    }
    downloadedFile
  }


  def streamImage(imagePath: String) = Action.async {
    val imagesBaseUrl = config.getString("application.imageBaseUrl")
    val imageUrl = imagesBaseUrl + imagePath
    val file = new File(imageDir + imagePath)
    val futureResponse = WS.url(imageUrl).getStream()
    futureResponse map {
      case (response, body) =>
        // Check that the response was successful
        if (response.status == 200) {


          // Get the content type
          val contentType = response.headers.get("Content-Type").flatMap(_.headOption)
            .getOrElse("application/octet-stream")

          // If there's a content length, send that, otherwise return the body chunked
          response.headers.get("Content-Length") match {
            case Some(Seq(length)) =>
              Ok.feed(body).as(contentType).withHeaders("Content-Length" -> length)
            case _ =>
              Ok.chunked(body).as(contentType)
          }
        } else {
          BadGateway
        }
    }

  }

}
