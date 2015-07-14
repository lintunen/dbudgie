package models

import java.sql.Blob
import java.util.{Date}

import play.api.db._
import play.api.Play.current

import anorm._
import anorm.SqlParser._

import scala.language.postfixOps

case class Budgie(id: Option[Long] = None, name: String, colour: String, picture: Option[String], race: String, gender: String)


/**
 * Helper for pagination.
 */
case class Page[A](items: Seq[A], page: Int, offset: Long, total: Long) {
  lazy val prev = Option(page - 1).filter(_ >= 0)
  lazy val next = Option(page + 1).filter(_ => (offset + items.size) < total)
}

object Budgie {

  // -- Parsers

  /**
   * Parse a Budgie from a ResultSet
   */
  val simple = {
    get[Option[Long]]("budgie.id") ~
      get[String]("budgie.name") ~
      get[String]("budgie.colour") ~
      get[Option[String]]("budgie.picture") ~
      get[String]("budgie.race") ~
      get[String]("budgie.gender") map {
      case id~name~colour~picture~race~gender => Budgie(id, name, colour, picture, race, gender)
    }
  }

  /**
   * Parse a (Computer,Company) from a ResultSet
   */
/*  val withCompany = Computer.simple ~ (Company.simple ?) map {
    case computer~company => (computer,company)
  }*/

  // -- Queries

  /**
   * Retrieve a budgie from the id.
   */
  def findById(id: Long): Option[Budgie] = {
    DB.withConnection { implicit connection =>
      SQL("select * from budgie where id = {id}").on('id -> id).as(Budgie.simple.singleOpt)
    }
  }

  /**
   * Retrieve a image from the id.
   */
//  def findImageById(id: Long): Stream[Array[Byte]] = {
//    DB.withConnection { implicit connection =>
//      //SQL("select * from Image").on("id" -> id)().map { row => row[Array[Byte]]("picture")
//      SQL("select * from Image").on("id" -> id)().map { row => row[Array[Byte]]("picture") }
//      }
//    }

  /**
   * Return a page of (Budgie).
   *
   * @param page Page to display
   * @param pageSize Number of budgies per page
   * @param orderBy Budgie property used for sorting
   * @param filter Filter applied on the name column
   */
  def list(page: Int = 0, pageSize: Int = 10, orderBy: Int = 1, filter: String = "%"): Page[(Budgie)] = {

    val offest = pageSize * page

    DB.withConnection { implicit connection =>

      val budgies = SQL(
        """
          select * from budgie
          where budgie.name like {filter}
          order by {orderBy} nulls last
          limit {pageSize} offset {offset}
        """
      ).on(
          'pageSize -> pageSize,
          'offset -> offest,
          'filter -> filter,
          'orderBy -> orderBy
        ).as(Budgie.simple *)

      val totalRows = SQL(
        """
          select count(*) from budgie
          where budgie.name like {filter}
        """
      ).on(
          'filter -> filter
        ).as(scalar[Long].single)

      Page(budgies, page, offest, totalRows)

    }



  }

  /**
   * Insert a new budgie.
   *
   * @param budgie The budgie values.
   */
  def insert(budgie: Budgie) = {
    DB.withConnection { implicit connection =>
      SQL(
        """
          insert into budgie values (
            (select next value for budgie_seq),
            {name}, {colour}, {picture}, {race}, {gender}
          )
        """
      ).on(
          'name -> budgie.name,
          'colour -> budgie.colour,
          'picture -> budgie.picture,
          'race -> budgie.race,
          'gender -> budgie.gender
        ).executeUpdate()
    }
  }



  /**
   * Delete a budgie.
   *
   * @param id Id of the budgie to delete.
   */
  def delete(id: Long) = {
    DB.withConnection { implicit connection =>
      SQL("delete from budgie where id = {id}").on('id -> id).executeUpdate()
    }
  }

  /**
   * Update a budgie.
   *
   * @param id The bugdie id
   * @param budgie The budgie values.
   */
  def update(id: Long, budgie: Budgie) = {
    DB.withConnection { implicit connection =>
      SQL(
        """
          update budgie
          set name = {name}, colour = {colour}, picture = {picture}, race = {race}, gender = {gender}
          where id = {id}
        """
      ).on(
          'id -> id,
          'name -> budgie.name,
          'colour -> budgie.colour,
          'picture -> budgie.picture,
          'race -> budgie.race,
          'gender -> budgie.gender
        ).executeUpdate()
    }
  }
}