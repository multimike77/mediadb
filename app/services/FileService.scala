package services

import java.io.File
import javax.inject.Singleton

import models.Movie
import org.slf4j.{LoggerFactory, Logger}
import play.api.libs.json.{JsValue, Json, Writes}

import scala.util.matching.Regex


@Singleton
class FileService {
  private final val logger: Logger = LoggerFactory.getLogger("movies")
  val episodePattern = ".*\\.(mkv|mp4|m4v|avi)$".r

  private def recursiveListFiles(dir: File, pattern: Regex): Array[File] = {
    if (dir.exists() && dir.isDirectory) {
      val files = dir.listFiles
      val good = files.filter(f => pattern.findFirstIn(f.getName).isDefined)
      good ++ files.filter(f => f.isDirectory && !f.isHidden).flatMap(recursiveListFiles(_, pattern))
    } else {
      logger.warn(s"Invalid directory: ${dir.getName}")
      Array.empty
    }
  }

  private def filesToMovieList(files: Array[File]): Seq[Movie] = {
    val movies: Array[Movie] = files.map(file =>
      Movie(
        name = stripExtension(file.getName),
        filePath = file.getCanonicalPath,
        creationDate = file.lastModified(),
        size = if (file.isFile) Some(file.length()) else None)
    )
    movies.toSeq
  }

  private def stripExtension(fileName: String) = {
    if (fileName.lastIndexOf('.') > -1) {
      fileName.substring(0, fileName.lastIndexOf('.'))
    } else {
      fileName
    }
  }

  def getMovieListFromDisk(path: String, pattern: Regex): Seq[Movie] = {
    val files: Array[File] = recursiveListFiles(new File(path), pattern)
    filesToMovieList(files)
  }

  private def getAllSubDirectories(parentDir: File): Array[File] = {
    parentDir.listFiles.filter(f => f.isDirectory && !f.isHidden)
  }

  def getTVShowsFromDisk(path: String): Seq[Movie] = {
    val files: Array[File] = getAllSubDirectories(new File(path))
    filesToMovieList(files)
  }

  def getTVShowEpisodes(path: String, seasonNumber: Int): Seq[Movie] = {
    val tvShowDir = new File(path)
    val seasonPattern = ("Season[\\s]?" + seasonNumber.toString).r
    val seasonDir = getAllSubDirectories(tvShowDir).find(dir => seasonPattern.findFirstIn(dir.getName).isDefined)

    val episodes = seasonDir.map(recursiveListFiles(_, episodePattern)).map(filesToMovieList)
    episodes.getOrElse(Seq[Movie]())
  }

  /**
   * Scan through all episodes of a tv show and return the file creation date of the newest episode
   * @param path Path to tv show
   * @return timestamp of the newest episode or <code>None</code>
   */
  def getNewestEpisodeDate(path: String): Option[Long] = {
    val tvShowDir = new File(path)
    val episodes: Array[File] = recursiveListFiles(tvShowDir, episodePattern)
    if (episodes.nonEmpty) {
      val newestEpisode = episodes.reduceLeft((x, y) => if (x.lastModified > y.lastModified) x else y)
      Some(newestEpisode.lastModified())
    } else None
  }

}



object FileJsonFormats {
  implicit val fileWrites = new Writes[File] {
    def writes(file: File): JsValue = {
      Json.obj(
        "path" -> file.getCanonicalPath,
        "name" -> file.getName
      )
    }
  }
}

