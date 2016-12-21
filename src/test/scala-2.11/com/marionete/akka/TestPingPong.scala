package com.marionete.akka

import scala.concurrent.duration._
import akka.actor.{ActorSystem, Props}
import akka.testkit.{DefaultTimeout, ImplicitSender, TestActorRef, TestKit}
import org.scalatest.{Matchers, WordSpecLike}

/**
  * Created by agapito on 19/12/2016.
  */
class TestPingPong extends TestKit(ActorSystem("TestActorSystem"))
  with Matchers
  with DefaultTimeout
  with ImplicitSender
  with WordSpecLike {


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
    "state reaches zero" in {
      ping.underlyingActor.state = 0
      within(1 second) {
        ping ! MsgPing("foo")
        expectMsg(MsgEndGame)
      }
    }
  }

}
