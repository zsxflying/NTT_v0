import spinal.core._
import spinal.lib._

class BarrettModUnitPort(dataWidth: Int, modWidth: Int, l: Int, k: Int, k0: Int) extends Bundle {
  // 变种巴雷特求模的两大约束
  assert(k0 < l, s"BarretModPort: k0:${k0}, l:${l}, which violated k0 < l")
  assert(k > dataWidth, s"BarretModPort: k:${k}, dataWidth:${dataWidth}, which violated k >= dataWidth + 1")
  // 模数约束
  assert(modWidth <= dataWidth, s"BarrettModPort: modWidt:${modWidth}, dataWidth:${dataWidth}, which violated " +
    s"modWidth <= dataWidth")

  val mWidth = k - l

  val din = slave(Flow(new Bundle {
    val data = UInt(dataWidth bits)
    val mod = UInt(modWidth bits)
    val m = UInt(mWidth bits)
  }))
  val dout = master(Flow(UInt(modWidth bits)))
}

// TODO: 只验证了在默认的k和l下的正确性
class BarrettModUnit(dataWidth: Int,
                     modWidth: Int,
                     hasOutputReg: Boolean = false)
                    (k: Int = dataWidth + 1,
                     l:Int = modWidth - 1)
  extends Component {

  // 默认参数
  val k0 = l - 1
  val k1 = k - k0
  val io = new BarrettModUnitPort(dataWidth, modWidth, l, k, k0)

  // TODO: 低功耗改进，根据控制通路的有效信号控制数据通路的寄存
  // stage0: mul0
  val inputRegPayload = RegNext(io.din.payload)
  val inputRegValid = RegNext(io.din.valid) init False

  val s1_valid = inputRegValid
  val s1_data = inputRegPayload.data
  val s1_mod = inputRegPayload.mod
  val s1_m = inputRegPayload.m
  val s1_t0 = UInt(dataWidth - k0 bits)
  val s1_t1 = UInt(dataWidth + k1 - l bits)
  s1_t0 := (s1_data >> k0).resized
  s1_t1 := (s1_t0 * s1_m).resized

  // stage1: mul1
  val s2_valid = RegNext(s1_valid) init False
  val s2_data = RegNext(s1_data(modWidth downto 0))
  val s2_mod = RegNext(s1_mod)
  val s2_t1 = RegNext(s1_t1)

  val s2_q = UInt(dataWidth - l bits)
  val s2_t2 = UInt(modWidth + 1 bits)

  s2_q := (s2_t1 >> k1).resized
  s2_t2 := (s2_q * s2_mod).resized

  // stage2: sub
  val s3_valid = RegNext(s2_valid) init False
  val s3_data = RegNext(s2_data)
  val s3_mod = RegNext(s2_mod)
  val s3_t2 = RegNext(s2_t2)

  val s3_r = UInt(modWidth + 1 bits)
  val s3_res = UInt(modWidth bits)

  s3_r := s3_data - s3_t2
  s3_res := s3_r.resized
  when(s3_r >= s3_mod) {
    s3_res := (s3_r - s3_mod).resized
  }

  io.dout.valid := s3_valid
  io.dout.payload := s3_res

  // stage3(optional): output
  (hasOutputReg).generate {
    io.dout.valid.setAsReg() init False
    io.dout.payload.setAsReg()
  }

}

object BarretModUnitGen extends App {
  val dataWidth = 32
  val modWidth = 16
  Config.spinal.generateVerilog(new BarrettModUnit(dataWidth, modWidth)())

}
