package com.stellmangreene.pbprdf.test

import org.scalatest._
import com.stellmangreene.pbprdf._

class GamePeriodInfoSpec extends FlatSpec with Matchers {

  behavior of "GamePeriodInfo"

  it should "convert a clock to seconds left in regulation" in {
    GamePeriodInfo.WNBAPeriodInfo.clockToSecondsLeft(1, "10:00") should be(Some(EventTimes(0, 600)))
    GamePeriodInfo.WNBAPeriodInfo.clockToSecondsLeft(1, "9:59") should be(Some(EventTimes(1, 599)))
    GamePeriodInfo.WNBAPeriodInfo.clockToSecondsLeft(1, "27.3") should be(Some(EventTimes(573, 27)))
    GamePeriodInfo.WNBAPeriodInfo.clockToSecondsLeft(2, "4:02") should be(Some(EventTimes(958, 242)))
    GamePeriodInfo.WNBAPeriodInfo.clockToSecondsLeft(3, "4:22") should be(Some(EventTimes(1538, 262)))
    GamePeriodInfo.WNBAPeriodInfo.clockToSecondsLeft(3, "1:05") should be(Some(EventTimes(1735, 65)))
    GamePeriodInfo.WNBAPeriodInfo.clockToSecondsLeft(4, "8:04") should be(Some(EventTimes(1916, 484)))
    GamePeriodInfo.WNBAPeriodInfo.clockToSecondsLeft(4, "3.6") should be(Some(EventTimes(2397, 3)))
  }

  it should "convert a clock to seconds left in overtime" in {
    GamePeriodInfo.WNBAPeriodInfo.clockToSecondsLeft(5, "5:00") should be(Some(EventTimes(2400, 300)))
    GamePeriodInfo.WNBAPeriodInfo.clockToSecondsLeft(5, "4:59") should be(Some(EventTimes(2401, 299)))
    GamePeriodInfo.WNBAPeriodInfo.clockToSecondsLeft(5, "1:02") should be(Some(EventTimes(2638, 62)))
    GamePeriodInfo.WNBAPeriodInfo.clockToSecondsLeft(7, "5:00") should be(Some(EventTimes(3000, 300)))
    GamePeriodInfo.WNBAPeriodInfo.clockToSecondsLeft(7, "30.4") should be(Some(EventTimes(3270, 30)))
    GamePeriodInfo.WNBAPeriodInfo.clockToSecondsLeft(7, "1.2") should be(Some(EventTimes(3299, 1)))
  }

  it should "handle in invalid clock" in {
    GamePeriodInfo.WNBAPeriodInfo.clockToSecondsLeft(1, "XYZ") should be(None)
    GamePeriodInfo.WNBAPeriodInfo.clockToSecondsLeft(1, "") should be(None)
  }
}