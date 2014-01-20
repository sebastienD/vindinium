package org.jousse
package bot

import scala.util.{ Try, Success, Failure }

case class Game(
    id: String,
    training: Boolean,
    board: Board,
    hero1: Hero,
    hero2: Hero,
    hero3: Hero,
    hero4: Hero,
    turn: Int = 0,
    maxTurns: Int,
    status: Status) {

  val heroes = List(hero1, hero2, hero3, hero4)

  def hero: Option[Hero] = heroes lift (turn % heroes.size)
  def hero(id: Int): Hero = heroes find (_.id == id) getOrElse hero1
  def hero(pos: Pos): Option[Hero] = heroes find (_.pos == pos)
  def heroByToken(token: String): Option[Hero] = heroes find (_.token == token)
  def heroByName(name: String): Option[Hero] = heroes find (_.name == name)

  def step = if (finished) this else {
    val next = copy(turn = turn + 1)
    if (next.turn > maxTurns) next.copy(status = Status.TurnMax) else next
  }

  def crash(c: Crash) = hero match {
    case None => this
    case Some(hero) => withHero(hero setCrash c) match {
      case game if game.heroes.count(_.crashed) == 4 => game.copy(status = Status.AllCrashed)
      case game                                      => game
    }
  }

  def names = heroes.map(_.name)

  def hasManyNames = names.distinct.size > 1

  def withHero(hero: Hero): Game = withHero(hero.id, _ => hero)

  def withHero(id: Int, f: Hero => Hero): Game = copy(
    hero1 = if (id == 1) f(hero1) else hero1,
    hero2 = if (id == 2) f(hero2) else hero2,
    hero3 = if (id == 3) f(hero3) else hero3,
    hero4 = if (id == 4) f(hero4) else hero4)

  def withTraining(v: Boolean) = copy(training = v)

  def withBoard(f: Board => Board) = copy(board = f(board))

  def finished = status.finished

  def arena = !training

  override def toString = s"Game[$id]: $status, turn $turn"

  def render = board.tiles.zipWithIndex map {
    case (xs, x) => xs.zipWithIndex map {
      case (tile, y) => hero(Pos(x, y)).fold(tile.render)(_.render)
    } mkString ""
  } mkString "\n"
}
