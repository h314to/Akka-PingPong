package com.marionete.akka

import akka.actor.SupervisorStrategy.Resume
import akka.actor.{Actor, ActorSystem, OneForOneStrategy, Props}

import scala.concurrent.duration._

/**
  * Created by agapito on 19/12/2016.
  */
class Ping(n: Int) extends Actor {
  var state: Int = n

  def receive = {
    case MsgPing(_) if state == 0 =>
      sender ! MsgEndGame
    case MsgPing(name) =>
      state = state - 1
      println(s"got a message from \'$name\', state: $state")
      sender ! MsgPing(self.path.name)
    case MsgEndGame =>
      context.actorSelection("/user/T800Super/T800") ! MsgEndGame
    case MsgStartGame =>
      state = state - 1
      println(s"Game Start, state: $state")
      context.actorSelection("/user/Pong") ! MsgPing("Ping")
  }
}

class GameTerminator extends Actor {
  var state = 1

  override def postRestart(reason: Throwable): Unit = {
    println("Oh no! John Connor is here!")
    super.postRestart(reason)
  }

  def receive = {
    case MsgEndGame =>
      println(s"Game Over")
      throw new Exception("you must be foo")
      println(s"I see dead code... all the time...")
      //context.system.terminate()
  }
}

class SuperGameTerminator extends Actor {

  val t800 = context.actorOf(Props[GameTerminator],name = "T800")

  override val supervisorStrategy =
    OneForOneStrategy(maxNrOfRetries = 5, withinTimeRange = 1 minute) {
      case _ => Resume
    }

  override def receive = {
    case _ => println(s"Terminator is being terminated! Sarah Connor must be close by.")
  }
}

object Main extends App {

  import scala.util.Random

  val maxGame = 10
  game(Random.nextInt(maxGame), Random.nextInt(maxGame))

  def game(nPing: Int, nPong: Int): Unit = {
    val system = ActorSystem("PingPongSystem")
    val t800super = system.actorOf(Props(new SuperGameTerminator), name="T800Super")
    val ping = system.actorOf(Props(new Ping(nPing)), name = "Ping")
    val pong = system.actorOf(Props(new Ping(nPong)), name = "Pong")
    ping ! MsgStartGame
  }

}

sealed trait Msg

case class MsgPing(name: String) extends Msg

case object MsgEndGame extends Msg

case object MsgStartGame extends Msg

