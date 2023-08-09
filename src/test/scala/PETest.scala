import spinal.core.sim._
import spinal.core._

object PETest extends App{
  implicit val config = new TPUConfig
  Config.sim.compile{
    val dut = new PE()
    dut
  }.doSim{ dut =>
    dut.clockDomain.forkStimulus(10)
    dut.io.din.ctrl.valid #= false
    dut.io.din.ctrl.lastOrFlush #= true
    dut.clockDomain.waitSampling(2)
    insert(127,127)
    insert(127,127)
    insert(127,127)
    insert(-128,127)
    insert(-128,127)
    insert(-128,127)
    insert(-128,127)
    for(i<-0 until 10){
      dut.clockDomain.waitSampling()
      println(dut.io.mulres.payload.toBigInt)
    }

    def insert(data:Int, weight:Int)={
      dut.io.din.ctrl.valid #= true
      dut.io.din.ctrl.lastOrFlush #= false
      dut.io.din.payload.data #= data
      dut.io.din.payload.weight #= weight
      dut.clockDomain.waitSampling()
      println(dut.io.mulres.payload.toBigInt)
    }

  }
}
