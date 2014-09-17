package models

import play.api.libs.json._
import reactivemongo.bson.BSONObjectID


case class Movie(
                  id: String,
                  name: String,
                  filePath: String,
                  details: JsValue = Json.obj()
                  ) {
  def this(name: String, filePath: String) = this(BSONObjectID.generate.stringify, name, filePath)

  override def equals(that: Any): Boolean = {
    that.isInstanceOf[Movie] && (this.hashCode() == that.asInstanceOf[Movie].hashCode())
  }

  override def hashCode = name.hashCode
}

object MovieFormats {

  import play.api.libs.json.Json


  def mongoReads[T](r: Reads[T]) = {
    __.json.update((__ \ 'id).json.copyFrom((__ \ '_id \ '$oid).json.pick[JsString])) andThen r
  }

  def mongoWrites[T](w: Writes[T]) = {
    w.transform(js => js.as[JsObject] - "id" ++ Json.obj("_id" -> Json.obj("$oid" -> js \ "id")))
  }

  implicit val movie: Reads[Movie] = mongoReads[Movie](Json.reads[Movie])
  implicit val movieWrites: Writes[Movie] = mongoWrites[Movie](Json.writes[Movie])


  //implicit val movieFormat = Json.format[Movie]
}
