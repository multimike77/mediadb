package controllers

import javax.inject.Singleton

import org.slf4j.{Logger, LoggerFactory}
import play.api.mvc.{Action, Controller}
import play.utils.UriEncoding

/**
 * Controller for handling the downloads of the media files
 * Nginx X-Accel-Redirect is used.
 */
@Singleton
class Downloads extends Controller {

  def downloadFile(diskNr: Int, filePath: String) = Action { request =>

    val fileName = if (filePath.lastIndexOf('/') > -1)
      filePath.substring(filePath.lastIndexOf('/') + 1)
    else
      filePath

    Ok.withHeaders(
      "X-Accel-Redirect" -> s"/disk$diskNr/$filePath",
      CONTENT_DISPOSITION -> s"attachment; filename=$fileName; filename*=UTF-8''$fileName"
    )

  }
}
