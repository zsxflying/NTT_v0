import myUtil.Accumulator
import spinal.core._
import spinal.lib.{MS, _}
import scala.math._

case class PEPayload(implicit config:TPUConfig) extends Bundle{
  val data = UInt(config.DATA_WIDTH bits)
  val weight = UInt(config.WEIGHT_WIDTH bits)
}

case class PECtrlSinals(implicit config:TPUConfig) extends Bundle{
  val valid = Bool()
  val lastOrFlush = Bool()

  def setFalse() = {
    valid := False
    lastOrFlush := False
    this
  }
}
case class PEData(implicit config:TPUConfig) extends Bundle with IMasterSlave {
  val payload = PEPayload()
  val ctrl = PECtrlSinals()

  override def asMaster(): Unit = {
    out(payload.data, payload.weight, ctrl.valid, ctrl.lastOrFlush)
  }
}

class PEIO(implicit config:TPUConfig) extends Bundle {
  // TODO: 添加有效信号是否可以降低功耗？ --待验证
  val din = slave (PEData())
  val dout = master (PEData())
  val mulres = master Flow(UInt(config.RESULT_WIDTH bits))
}


class PE(implicit config:TPUConfig) extends Component {
  val io = new PEIO()

  val dinPayloadReg = RegNextWhen(io.din.payload, io.din.ctrl.valid) // TODO: 目标是减少翻转降低功耗，但未验证有效性。
  val dinCtrlReg = RegNext(io.din.ctrl, init = PECtrlSinals().setFalse())

  io.dout.payload := dinPayloadReg
  io.dout.ctrl := dinCtrlReg

  val multiplyRes = dinPayloadReg.data * dinPayloadReg.weight

  val resAccWidth = config.DATA_WIDTH + config.WEIGHT_WIDTH + log2Up(config.ARRAY_SIZE)
  val resAccReg = Accumulator(multiplyRes.resize(resAccWidth bits), dinCtrlReg.valid, dinCtrlReg.lastOrFlush)
  val resAccRegValue0 = resAccReg.value(resAccWidth - 1 downto config.RESULT_WIDTH)
  val MAX_VALUE = U((pow(2, config.RESULT_WIDTH) - 1).toInt, config.RESULT_WIDTH bits) // 输出结果的最大值

  io.mulres.payload := Mux(resAccRegValue0 =/= U(0), MAX_VALUE, resAccReg.value.resized)// 输出数据被压缩到指定范围
  io.mulres.valid := resAccReg.validOut
}

object PEGen extends App {
  implicit val config = new TPUConfig
  Config.spinal.generateVerilog(new PE())
}