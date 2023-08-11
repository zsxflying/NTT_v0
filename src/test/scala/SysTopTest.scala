import spinal.core.sim._

object SysTopTest extends App {
  implicit val config = new TPUConfig
  Config.sim.compile(new SysTop()).doSim{ dut =>
    dut.clockDomain.forkStimulus(10)
    SimTimeout(60 * 10)
    dut.io.start #= false
    dut.clockDomain.waitSampling()
    dut.io.start #= true
    val inputDataMat = dut.dataRamInput
    val inputWeightMat = dut.weightRamInput
    val resRef = dut.resRef

    import myUtil.PrintDump._
//    printMatrix(dut.initData(0), "data0")
//    printMatrix(dut.initData(1), "data1")
    printMatrix(inputDataMat, "dataRam")
//    printMatrix(dut.initWeight(0),"weight0")
//    printMatrix(dut.initWeight(1),"weight1")
    printMatrix(inputWeightMat,"weightRam")
    printMatrix(resRef(0),"res0")
    printMatrix(resRef(1),"res1")

    while(!dut.io.done.toBoolean){
      dut.clockDomain.waitSampling()
    }
    dut.io.start #= false
    printMatrix(dut.resRam.getMemInt())
  }
}
