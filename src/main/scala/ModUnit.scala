import spinal.core._
import spinal.lib._


class ModUnit(implicit config: TPUConfig) extends Component {
  val arraySize = config.ARRAY_SIZE
  val modWidth = config.MOD_WIDTH

  val io = new Bundle {
    val resIn = slave Flow (Vec.fill(arraySize)(UInt(config.MUL_RES_WIDTH bits)))
    val resOut = master Flow (Vec.fill(arraySize)(UInt(config.MOD_RES_WIDTH bits)))
    val modIn = slave Stream (UInt(modWidth bits))
  }

  val validCond = if (config.SKEW_OUTPUT) {
    io.resIn.valid || Delay(io.resIn.valid, arraySize - 1)
  } else {
    io.resIn.valid
  }

  val resInReg = new Bundle {
    val payload = RegNextWhen(io.resIn.payload, validCond)
    val valid = RegNext(io.resIn.valid, False)
  }

  io.resOut.setAsReg()

  io.modIn.ready := validCond ? True | False // 使用delay之前的valid信号

  //  val shiftReg = History(io.modIn.payload.resize(config.MUL_RES_WIDTH), config.ARRAY_SIZE)
  val shiftReg = History(io.modIn.payload, config.ARRAY_SIZE)
  io.resOut.payload.zipWithIndex.foreach { case (l, r) => l := resInReg.payload(r) % shiftReg(r) }
  io.resOut.valid := resInReg.valid
}

object ModUnitGen extends App {
  implicit val config = new TPUConfig

  Config.spinal.generateVerilog(new ModUnit())
}
