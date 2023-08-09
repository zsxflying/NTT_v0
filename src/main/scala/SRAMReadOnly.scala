import spinal.core._
import spinal.core.sim._

class SRAMReadOnly(singleDataWidth:Int, dataNumPerLine:Int, depth:Int, initData:Array[Array[Int]]) extends Component{
  // 同步读
  val io = new Bundle{
    val addr = in UInt(log2Up(depth) bits)
    val rdata = out Bits(singleDataWidth * dataNumPerLine bits)
  }

  // 初始化为0，可后门读写
  val mem = new Mem(SRAMData(singleDataWidth, dataNumPerLine), depth).init {
    val arr = Array.fill(depth)(SRAMData(singleDataWidth, dataNumPerLine))
    arr.zip(initData).map {
      case (a, d) => a.initValue(d)
    }
  }.simPublic()

  io.rdata := mem.readSync(
    enable = True,
    address = io.addr
  ).asBits

}

object SRAMReadOnly {
  def apply(arr: Array[Array[Int]]) = {
    val tpuconfig = new TPUConfig()
//    new SRAMReadOnly(tpuconfig.inputDataWidth, tpuconfig.peSize, tpuconfig.sramDDepth, arr)
  }
}