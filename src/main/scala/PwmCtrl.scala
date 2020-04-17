package flogics.lib.pwm

import spinal.core._
import spinal.lib._
import spinal.lib.Counter
import spinal.lib.bus.amba3.apb.{Apb3, Apb3Config, Apb3SlaveFactory}

class Pwm(size: Int) extends Component {
  val io = new Bundle {
    val width = in UInt (size bits)
    val output = out Bool
  }

  val ct = Counter(size bits)

  ct.increment()
  io.output := ct.value < io.width
}

class Apb3PwmCtrl(size: Int) extends Component {
  val io = new Bundle {
    val apb = slave(
      Apb3(
        addressWidth = 4,
        dataWidth = 32
      )
    )
    val output = out Bool
  }

  val pwm = new Pwm(size)
  io.output := pwm.io.output

  val busCtrl = Apb3SlaveFactory(io.apb)
  busCtrl.driveAndRead(pwm.io.width, address = 0)
}
