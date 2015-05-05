import views.html.helper.FieldConstructor

/**
 * Created by yerlibilgin on 04/05/15.
 */
object MyHelper {
  implicit val myFields = FieldConstructor(views.html.myInput.f)
}
