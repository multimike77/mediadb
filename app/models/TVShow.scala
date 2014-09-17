package models

import play.api.libs.json.{Json, JsValue}


case class TVShow(id: String, name: String, filePath: String, details: JsValue = Json.obj()) {

}
