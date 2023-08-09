import myUtil.Accumulator
import spinal.core._
import spinal.lib.{MS, _}
import scala.math._

case class PEFlow[T<:Data](data:HardType[T]) extends Flow(data){
  val lastOrFlush = Bool()
  override def asMaster()={
    super.asMaster()
    out(lastOrFlush)
  }
}

case class PEData(implicit config:TPUConfig) extends Bundle{
  val data = SInt(config.DATA_WIDTH bits)
  val weight = SInt(config.WEIGHT_WIDTH bits)
}

class PEIO(implicit config:TPUConfig) extends Bundle {
  // TODO: 添加有效信号是否可以降低功耗？ --待验证
  val din = slave Flow(PEData())
  val dout = master Flow(PEData())
  val mulres = master Flow(SInt(config.RESULT_WIDTH bits))
}


class PE(implicit config:TPUConfig) extends Component {
  val io = new PEIO()

  val dinPayloadReg = RegNextWhen(io.din.payload, io.din.valid) // TODO: 目标是减少翻转降低功耗，但未验证有效性。
  val dinValidReg = RegNext(io.din.valid)

  val multiplyRes = dinPayloadReg.data * dinPayloadReg.weight
  val resAccWidth = config.DATA_WIDTH + config.WEIGHT_WIDTH + log2Up(config.ARRAY_SIZE)
  val resAccReg = Accumulator(multiplyRes.resize(resAccWidth bits), dinValidReg, dinPayloadReg.lastOrFlushAcc)
  val resAccRegValue0 = resAccReg.value(resAccWidth - 1)
  val resAccRegValue1 = resAccReg.value(resAccWidth - 1 downto (resAccWidth - log2Up(config.ARRAY_SIZE) - 1))
  val MAX_VALUE = S((pow(2, config.RESULT_WIDTH - 1) - 1).toInt, config.RESULT_WIDTH bits) // 输出结果的最大值
  val MIN_VALUE = S(-(pow(2, config.RESULT_WIDTH - 1)).toInt, config.RESULT_WIDTH bits) // 输出结果的最小值
  val cond0 = (resAccRegValue0 === False) && (resAccRegValue1 =/= S(0))
  val cond1 = (resAccRegValue0 === True) && (resAccRegValue1 =/= S(-1).resized)

  io.mulres.payload := Mux(cond0, MAX_VALUE, Mux(cond1, MIN_VALUE, resAccReg.value.resized))
  io.mulres.valid := RegNext(dinPayloadReg.lastOrFlushAcc)

  io.dout.payload := dinPayloadReg
  io.dout.valid := dinValidReg
}

object PEGen extends App {
  implicit val config = new TPUConfig
  Config.spinal.generateVerilog(new PE())
}