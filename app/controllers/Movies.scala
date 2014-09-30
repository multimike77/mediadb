package controllers

import javax.inject.{Inject, Singleton}

import models.Movie
import models.MovieFormats._
import org.slf4j.{Logger, LoggerFactory}
import play.api.Configuration
import play.api.Play._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import play.api.mvc._
import play.modules.reactivemongo.MongoController
import play.modules.reactivemongo.json.collection.JSONCollection
import services.{FileService, MovieDBService}

import scala.concurrent.Future

@Singleton
class Movies @Inject()(fileService: FileService, mdb: MovieDBService) extends Controller with MongoController {
  private final val logger: Logger = LoggerFactory.getLogger("movies")

  private def collection: JSONCollection = db.collection[JSONCollection]("movies")

  private val config: Configuration = current.configuration
  private val movieSources: Seq[String] = config.getStringSeq("application.movieSources")
    .getOrElse(throw current.configuration.globalError("movieSources config not set"))


  /*
  def listNewMovies = Action.async {
    findNewMovies.map { movies => Ok(Json.toJson(movies))}
  }

  def listDeletedMovies = Action.async {
    findDeletedMovies.map { movies => Ok(Json.toJson(movies))}
  }


  private def findNewMovies: Future[List[Movie]] = {
    val moviesFromDisk: List[Movie] = allMoviesFromDisk
    val futureMovieList: Future[List[Movie]] = allMoviesFromDB

    futureMovieList.map { fm =>
      moviesFromDisk.diff(fm)
    }
  }

  private def findDeletedMovies: Future[List[Movie]] = {
    val moviesFromDisk: List[Movie] = allMoviesFromDisk
    val futureMovieList: Future[List[Movie]] = allMoviesFromDB

    futureMovieList.map { fm =>
      fm.diff(moviesFromDisk)
    }
  }
*/

  private def allMoviesFromDisk: Seq[Movie] = {
    movieSources.flatMap(dir => fileService.getMovieListFromDisk(dir, ".*\\.(mkv|mp4)$".r))
  }

  private def allMoviesFromDB: Future[Seq[Movie]] = {
    collection.
      find(Json.obj()).
      cursor[Movie].
      collect[Seq]()
  }

  private def moviesWithoutDetails: Future[Seq[Movie]] = {
    collection.
      find(Json.obj("details" -> Json.obj())).
      cursor[Movie].
      collect[Seq]()
  }

  case class MovieSyncResult(addedMovies: Seq[Movie], deletedMovies: Seq[Movie])

  private def findAllNewAndDeleted: Future[MovieSyncResult] = {
    val moviesFromDisk: Seq[Movie] = allMoviesFromDisk
    val futureMovieList: Future[Seq[Movie]] = allMoviesFromDB

    futureMovieList.map { fm =>
      MovieSyncResult(moviesFromDisk.diff(fm), fm.diff(moviesFromDisk))
    }
  }

  /* ### Controller Actions ### */

  def showMovieStatus = Action.async {
    val futureUpdatedMovies: Future[MovieSyncResult] = findAllNewAndDeleted
    futureUpdatedMovies.map { um =>
      val movieData = Json.obj(
        "addedMovies" -> um.addedMovies,
        "deletedMovies" -> um.deletedMovies
      )
      Ok(movieData)
    }
  }


  def syncDbWithFiles = Action.async {
    val futureSync: Future[MovieSyncResult] = findAllNewAndDeleted
    futureSync.map { result =>
      for (movie <- result.addedMovies) {
        collection.insert(movie).map { lastError =>
          val msg = "Success: " + lastError.ok.toString + " added: " + movie.name
          logger.info(msg)
          if (!lastError.ok) {

          }
        }
      }
      for (movie <- result.deletedMovies) {
        collection.remove(Json.obj("name" -> movie.name)).map { lastError =>
          val msg = "Success: " + lastError.ok.toString + " deleted: " + movie.name
          logger.info(msg)
        }
      }
      Ok("syncing movies")
    }
  }

  def loadMissingMovieDetails = Action.async {
    val toBeUpdated: Future[Seq[Movie]] = moviesWithoutDetails
    toBeUpdated.map { movies =>
      for (movie <- movies) {
        mdb.searchMovie(movie.name).map { searchRes =>
          logger.info(movie.name + searchRes.toString())
          //TODO handle selection for multiple results
          val movieId = (searchRes \ "results")(0) \ "id"
          if (movieId.isInstanceOf[JsNumber]) {
            mdb.loadMovieDetails(movieId.as[Int], "de").map { movieDetails =>
              val oid = Json.obj("_id" -> Json.obj("$oid" -> movie.id))
              val details = Json.obj("$set" -> Json.obj("details" -> movieDetails))
              collection.update(oid, details).map { lastError =>
                val msg = "Success: " + lastError.ok.toString + " updated: " + movie.name
                logger.info(msg)
              }
            }
          }
        }
      }
      Ok
    }
  }

  
  def loadMovieDetails(name: String) = Action.async {
    val fm: Future[Option[Movie]] = collection.find(Json.obj("name" -> name)).one

    fm.map {
      case Some(show) => Ok(Json.toJson(show))
      case None => NotFound(Json.obj("error" -> "not found"))
    }
  }

  def updateDetails(id: String) = Action.async { request =>
    request.getQueryString("tmdbid") match {
      case None => Future {BadRequest("tmdbId not specified")}
      case Some(tmdbId) =>
        mdb.loadMovieDetails(tmdbId.toInt, "de").map { md =>
          val oid = Json.obj("_id" -> Json.obj("$oid" -> id))
          val details = Json.obj("$set" -> Json.obj("details" -> md))
          collection.update(oid, details).map { lastError =>
            val msg = "Success: " + lastError.ok.toString + " updated: " + id
            logger.info(msg)
          }
          Ok(details)
        }
    }
  }

  def updateTmdbConfiguration() = Action.async {
    mdb.getConfiguration.flatMap { config =>
      val configCollection = db.collection[JSONCollection]("config")
      val storeConfig = Json.obj("tmdb" -> config)
      configCollection.save(storeConfig).map { lastError =>
        if (lastError.ok) {
          Ok(Json.obj("success" -> true))
        } else {
          InternalServerError(Json.obj("success" -> false, "error" -> lastError.errMsg))
        }
      }
    }
  }

  def listMovies = Action.async {
    allMoviesFromDB.map { movies =>
      Ok(Json.toJson(movies))
    }
  }

}
