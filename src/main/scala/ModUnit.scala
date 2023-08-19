import spinal.core._
import spinal.lib._


class ModUnit(implicit config: TPUConfig) extends Component {
  val arraySize = config.ARRAY_SIZE
  val resWidth = config.RESULT_WIDTH
  val modWidth = config.MOD_WIDTH

  val io = new Bundle{
    val resIn = slave Flow (Vec.fill(arraySize)(UInt(resWidth bits)))
    val resOut = master Flow (Vec.fill(arraySize)(UInt(resWidth bits)))
    val modIn = slave Stream(UInt(modWidth bits))
  }

  val resInReg = new Bundle{
//    private val delay0 = RegNextWhen(io.resIn.payload, io.resIn.valid)
//    private val delay1 = RegNext(io.resIn.valid, False)
//    val payload = RegNextWhen(delay0,delay1)
//    val valid = RegNext(delay1, False)
    val payload = RegNextWhen(io.resIn.payload, io.resIn.valid)
    val valid = RegNext(io.resIn.valid, False)
  }

  io.resOut.setAsReg()

  io.modIn.ready := io.resIn.valid ? True | False // 使用delay之前的valid信号

  val shiftReg = History(io.modIn.payload.resize(resWidth), config.ARRAY_SIZE)
  io.resOut.payload.zipWithIndex.foreach{case (l,r) => l := resInReg.payload(r) % shiftReg(r)}
  io.resOut.valid := resInReg.valid
}

object ModUnitGen extends App{
  implicit val config = new TPUConfig

  Config.spinal.generateVerilog(new ModUnit())
}