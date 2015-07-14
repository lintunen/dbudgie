package controllers

import javafx.beans.value.WritableValue

import models._
import play.Play
import play.api.data.Forms._
import play.api.data._
import play.api.mvc._
import views._
import play.api.Play.current
import play.api.i18n.Messages.Implicits._

/**
 * Manage a database of cats
 */
object Application extends Controller {

  /**
   * This result directly redirect to the application home.
   */
  val Home = Redirect(routes.Application.list(0, 2, ""))

  /**
   * Describe the budgie form (used in both edit and create screens).
   */
  val budgieForm = Form(
    mapping(
      "id" -> ignored(None:Option[Long]),
      "name" -> nonEmptyText,
      "colour" -> nonEmptyText,
      "picture" -> optional(text),
      "race" -> nonEmptyText,
      "gender" -> nonEmptyText()
    )(Budgie.apply)(Budgie.unapply)
  )

  // -- Actions

  /**
   * Handle default path requests, redirect to budgies list
   */
  def index = Action { Home }

  /**
   * Display the paginated list of budgies.
   *
   * @param page Current page number (starts from 0)
   * @param orderBy Column to be sorted
   * @param filter Filter applied on budgie names
   */
  def list(page: Int, orderBy: Int, filter: String) = Action { implicit request =>
    Ok(html.list(
      Budgie.list(page = page, orderBy = orderBy, filter = ("%"+filter+"%")),
      orderBy, filter
    ))
  }

  /**
   * Display the 'edit form' of a existing budgie.
   *
   * @param id Id of the budgie to edit
   */
  def edit(id: Long) = Action {
    Budgie.findById(id).map { budgie =>
      Ok(html.editForm(id, budgieForm.fill(budgie)))
    }.getOrElse(NotFound)
  }

  /**
   * Handle the 'edit form' submission
   *
   * @param id Id of the budgie to edit
   */
  def update(id: Long) = Action { implicit request =>
    budgieForm.bindFromRequest.fold(
      formWithErrors => BadRequest(html.editForm(id, formWithErrors)),
      budgie => {
        Budgie.update(id, budgie)
        Home.flashing("success" -> "Budgie %s has been updated".format(budgie.name))
      }
    )
  }

  /**
   * Display the 'new budgie form'.
   */
  def create = Action {
    Ok(html.createForm(budgieForm))
  }

  /**
   * Handle the 'new budgie form' submission.
   */
  def save = Action(parse.multipartFormData) { implicit request =>
    //def save = Action { implicit request =>
    var budgieName : String = ""

    //todo: handle duplicate files names e.g. appending time stamp to file name or splitting into folders by upload date
    request.body.file("file").map { file =>
      file.ref.moveTo(new java.io.File(
        Play.application().path().getAbsolutePath() + "/public/images/" + file.filename))
        budgieForm.bindFromRequest.fold(
          formWithErrors => BadRequest(html.createForm(formWithErrors)),
          budgie => {
            Budgie.insert(budgie)
            budgieName = budgie.name
            Home.flashing("success" -> "Budgie %s has been created".format(budgieName))
          }
      )
    }.getOrElse {
      budgieForm.bindFromRequest.fold(
        formWithErrors => BadRequest(html.createForm(formWithErrors)),
        budgie => {
          Budgie.insert(budgie)
          budgieName = budgie.name
          Home.flashing("success" -> "Budgie %s has been created".format(budgieName))
        }
      )
    }
  }

  /**
   * Handle budgie deletion.
   */
  def delete(id: Long) = Action {
    Budgie.delete(id)
    Home.flashing("success" -> "Budgie has been deleted")
  }

}
            
