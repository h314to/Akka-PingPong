package com.marionete.akka

import scala.concurrent.duration._
import akka.actor.{Actor, ActorSystem, DeadLetter, Props}
import akka.testkit.{DefaultTimeout, ImplicitSender, TestActorRef, TestKit}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

/**
  * Created by agapito on 19/12/2016.
  */
class TestPingPong extends TestKit(ActorSystem("TestActorSystem"))
  with Matchers
  with DefaultTimeout
  with ImplicitSender
  with WordSpecLike
  with BeforeAndAfterAll {

  override def afterAll(): Unit = {
    println("done")
    //system.terminate()
    TestKit.shutdownActorSystem(system)
  }


  "a simple actor" must {
    val n = 3
    val ping = TestActorRef(new Ping(n), name = "Test")
    "update state" in {
      within(500 millis) {
        ping ! MsgPing("foo")
        expectMsg(MsgPing("Test"))
        ping.underlyingActor.state should be(n - 1)
      }
    }
    "send a message back" in {
      within(500 millis) {
        ping ! MsgPing("foo")
        expectMsg(MsgPing("Test"))
      }
    }
    // "state reaches zero" in {
    //   ping.underlyingActor.state = 0
    //   within(1 second) {
    //     ping ! MsgPing("foo")
    //     expectMsg(MsgEndGame)
    //   }
    // }

    "state reaches zero" in {
      val ping = TestActorRef(new Ping(0))
      var msgFound:Any = 0
      system.eventStream.subscribe(system.actorOf(Props(new Actor {
        def receive = {
          case _ =>
            println("I'm here!")
          //case DeadLetter(msg, from, to) =>
          //  println(s"Dead letter $msg from $from to $to")
          //  msgFound = msg
        }
      })), classOf[DeadLetter])
      within(500 millis) {
        ping ! MsgPing
        Thread.sleep(300)
        assert(msgFound == MsgEndGame)
        //assert(true)
      }
    }
  }

}
