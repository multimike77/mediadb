package services

import java.io.File
import javax.inject.Singleton

import models.Movie
import play.api.libs.json.{JsValue, Json, Writes}

import scala.util.matching.Regex


@Singleton
class FileService {

  private def recursiveListFiles(dir: File, pattern: Regex): Array[File] = {
    val files = dir.listFiles
    val good = files.filter(f => pattern.findFirstIn(f.getName).isDefined)
    good ++ files.filter(f => f.isDirectory && !f.isHidden).flatMap(recursiveListFiles(_, pattern))
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
    val episodePattern = ".*\\.(mkv|mp4|m4v|avi)$".r
    val seasonDir = getAllSubDirectories(tvShowDir).find(dir => seasonPattern.findFirstIn(dir.getName).isDefined)

    val episodes = seasonDir.map(recursiveListFiles(_, episodePattern)).map(filesToMovieList)
    episodes.getOrElse(Seq[Movie]())
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

