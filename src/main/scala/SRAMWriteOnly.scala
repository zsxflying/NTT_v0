import spinal.core.sim._
import spinal.core._
import spinal.lib._

class SRAMWriteOnly(singleDataWidth:Int, dataNumPerLine:Int, depth:Int) extends Component{
  // 同步写
  val io = new Bundle{
    val valid = in Bool()
    val addr = in UInt(log2Up(depth) bits)
    val wdata = in(SRAMData(singleDataWidth, dataNumPerLine))
  }

  // 初始化为0，可后门读写
  val initData = Array.fill(depth)(Array.fill(dataNumPerLine)(BigInt(0)))
  val mem = new Mem(SRAMData(singleDataWidth, dataNumPerLine), depth).init {
    val arr = Array.fill(depth)(SRAMData(singleDataWidth, dataNumPerLine))
    arr.zip(initData).map {
      case (a, d) => a.initValue(d)
    }
  }.simPublic()

  mem.write(
    enable = io.valid,
    address = io.addr,
    data = io.wdata
  )

  def getMemBin():Array[Array[String]] = {
    (0 until depth).map(getBigInt(mem, _).binString(mem.getWidth).grouped(singleDataWidth).toArray).toArray
  }

  import myUtil.DataConvert._
  def getMemInt():Array[Array[BigInt]]={
    (0 until depth).map(getBigInt(mem, _).binString(mem.getWidth).grouped(singleDataWidth).toArray.map(binUIntString2Int(_))).toArray
  }

}

object SRAMWriteOnly{
  def apply = {
    val tpuconfig = new TPUConfig()
//    new SRAMWriteOnly(tpuconfig.outputDataWidth, tpuconfig.ARRAY_SIZE, tpuconfig.sramResDepth)
  }
}