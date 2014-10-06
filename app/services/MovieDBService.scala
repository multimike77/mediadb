package services

import java.io.{File, FileOutputStream}
import javax.inject.Singleton

import com.typesafe.config.{Config, ConfigFactory}
import org.slf4j.{Logger, LoggerFactory}
import play.api.Play.current
import play.api.libs.iteratee.Iteratee
import play.api.libs.json.JsValue
import play.api.libs.ws._
import play.utils.UriEncoding

import scala.concurrent.Future

/**
 * Service class for accessing the API of <a href="http://www.themoviedb.org/">TheMovieDB</a> for retrieving
 * Movie and TV series infos and images
 * API Docs: <a href="http://docs.themoviedb.apiary.io/">http://docs.themoviedb.apiary.io/</a>
 */
@Singleton
class MovieDBService {
  private val logger: Logger = LoggerFactory.getLogger("movies")

  private val config: Config = ConfigFactory.load()

  private val apiKey = config.getString("application.mdbApiKey")
  private val baseUrl = config.getString("application.mdbBaseUrl")
  private val apiKeyParam = "api_key"

  private implicit val context = play.api.libs.concurrent.Execution.Implicits.defaultContext


  //Movies
  def searchMovie(name: String): Future[JsValue] = {
    val url = baseUrl + "search/movie"
    val nameAndYear: (String, Option[String]) = extractNameAndYear(name)
    val encodedName: String = UriEncoding.encodePathSegment(nameAndYear._1, "UTF-8")
    val baseParams: Seq[(String, String)] = Seq(apiKeyParam -> apiKey, "query" -> encodedName)

    val params: Seq[(String, String)] = nameAndYear._2 match {
      case Some(y) => baseParams :+ "year" -> y
      case None => baseParams
    }
    val holder: WSRequestHolder = WS.url(url).withQueryString(params:_*)

    holder.get().map { response => response.json}
  }

  def loadMovieDetails(id: Int, lang: String): Future[JsValue] = {
    val url = baseUrl + "movie/" + id
    val request = WS.url(url).withQueryString(apiKeyParam -> apiKey, "language" -> lang)
    request.get().map { response => response.json}
  }


  //TV Shows
  def searchTVShow(name: String): Future[JsValue] = {
    val url = baseUrl + "search/tv"
    val nameAndYear: (String, Option[String]) = extractNameAndYear(name)
    val encodedName: String = UriEncoding.encodePathSegment(nameAndYear._1, "UTF-8")
    val baseParams: Seq[(String, String)] = Seq(apiKeyParam -> apiKey, "query" -> encodedName)

    val params: Seq[(String, String)] = nameAndYear._2 match {
      case Some(y) => baseParams :+ "first_air_date_year" -> y
      case None => baseParams
    }
    val holder: WSRequestHolder = WS.url(url).withQueryString(params: _*)

    holder.get().map { response => response.json}
  }

  def loadTVShowDetails(id: Int, lang: String): Future[JsValue] = {
    val url = baseUrl + "tv/" + id
    val request = WS.url(url).withQueryString(apiKeyParam -> apiKey, "language" -> lang)
    request.get().map { response => response.json}
  }

  /**
   * Get global configuration settings for MovieDB API
   * See <a href="http://docs.themoviedb.apiary.io/reference/configuration">reference</a>
   * @return Future JSValue containing the configuration
   */
  def getConfiguration: Future[JsValue] = {
    val url = baseUrl + "configuration"
    WS.url(url).withQueryString(apiKeyParam -> apiKey).get().map { response => response.json}
  }

  /**
   * Download an image from the MovieDB API and store it on hard disk
   * @param imageName Name of the image
   * @param size Desired image size (Possible sizes are returned with MovieDB configuration)
   */
  def downloadImage(imageName: String, size: String): Unit = {
    val imagesBaseUrl = "https://image.tmdb.org/t/p/"
    val imageUrl = imagesBaseUrl + size + imageName

    val imageDir = config.getString("application.imageDir")
    val file = new File(imageDir + size + imageName)

    //only download if not already on disk
    if (!file.exists()) {
      val futureResponse = WS.url(imageUrl).getStream()
      futureResponse.flatMap {
        case (headers, body) =>
          val outputStream = new FileOutputStream(file)

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
          }.map(_ => file)
      }
    }

  }

  private def extractNameAndYear(name: String): (String, Option[String]) = {
    val pattern = """(.+)\((\d{4})\)""".r
    pattern.findFirstMatchIn(name) match {
      case Some(m) => (m.group(1).trim.replaceAll("_", " "), Some(m.group(2)))
      case None => (name.replaceAll("_", " "), None)
    }
  }

}
