import spinal.core._
import spinal.lib._
import spinal.lib.com.jtag.{Jtag, JtagTap}
import flogics.lib.pwm._

class JtagPwm_TinyFPGA_BX extends Component {
  val io = new Bundle {
    val CLK = in Bool
    val PWM = out Bool
    val TCK = in Bool
    val TDI = in Bool
    val TDO = out Bool
    val TMS = in Bool
    val USBPU = out Bool
  }

  val pwm_size = 8

  val core_clock_domain = ClockDomain(
    clock = io.CLK,
    frequency = FixedFrequency(16 MHz),
    config = ClockDomainConfig(
      resetKind = BOOT
    )
  )

  class MyJtagTap extends Component {
    val io = new Bundle {
      val jtag = slave(Jtag())
      val width = out UInt(pwm_size bits)
      val pwm = in Bool
    }

    val pwm_reg = RegNext(io.pwm).addTag(crossClockDomain)

    val tap = new JtagTap(io.jtag, 8)
    val idcodeArea = tap.idcode(B"x87654321")(instructionId = 4)
    val widthArea = tap.write(io.width)(instructionId = 5)
    val pwmArea = tap.read(pwm_reg)(instructionId = 6)
  }

  val jtag_area = new ClockingArea(ClockDomain(io.TCK)) {
    val jtag = new MyJtagTap()
    jtag.io.jtag.tms <> io.TMS
    jtag.io.jtag.tdi <> io.TDI
    jtag.io.jtag.tdo <> io.TDO
  }

  val core_area = new ClockingArea(core_clock_domain) {
    io.USBPU := False

    val pwm = new Pwm(
      size = pwm_size
    )
    pwm.io.width := jtag_area.jtag.io.width
    jtag_area.jtag.io.pwm := pwm.io.output
    io.PWM := pwm.io.output
  }
}

object JtagPwm_TinyFPGA_BX {
  def main(args: Array[String]): Unit = {
    SpinalVerilog(new JtagPwm_TinyFPGA_BX)
  }
}
