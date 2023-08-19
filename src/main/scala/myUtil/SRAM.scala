import jdk.nashorn.internal.ir.debug.ASTWriter
import spinal.core._
import spinal.core.sim._
import spinal.lib._

case class SRAMData(dataWidth:Int, num:Int) extends Bundle{
  val data = Vec.fill(num)(UInt(dataWidth bits))

  def initValue(arr:Array[Int]): this.type = {
//    data.reverse.zip(arr).foreach{case (d, a) => d := a}
    data.reverse.zip(arr).foreach{case (d, a) => d := a}
    this
  }
}

class SRAM(singleDataWidth:Int, dataNumPerLine:Int, depth:Int, initData:Array[Array[Int]]) extends Component{
  // 同步读写
  val io = new Bundle{
    val valid = in Bool()
    val wr = in Bool()
    val addr = in UInt(log2Up(depth) bits)
    val wdata = in (SRAMData(singleDataWidth,dataNumPerLine))
    val rdata = out Bits(singleDataWidth * dataNumPerLine bits)
  }

  // 初始化为0，可后门读写
  val mem = new Mem(SRAMData(singleDataWidth,dataNumPerLine), depth).init{
    val arr = Array.fill(depth)(SRAMData(singleDataWidth,dataNumPerLine))
    arr.zip(initData).map{
      case (a, d) => a.initValue(d)
    }
  }.simPublic()

  mem.write(
    enable = io.valid && io.wr,
    address = io.addr,
    data = io.wdata
  )
  io.rdata := mem.readSync(
    enable = io.valid && !io.wr,
    address = io.addr
  ).asBits

}


object SRAM extends App{
  val singleDataWidth = 8
  val dataNumPerLine = 3
  val depth = 2
  val initData = Array.fill(depth)(Array(-1,2,3))
  println(initData)
  Config.spinal.generateVerilog(new SRAM(singleDataWidth, dataNumPerLine, depth, initData))
  Config.spinal.generateVerilog(new SRAMReadOnly(singleDataWidth, dataNumPerLine, depth, initData))
  Config.spinal.generateVerilog(new SRAMWriteOnly(singleDataWidth, dataNumPerLine, depth))

}
