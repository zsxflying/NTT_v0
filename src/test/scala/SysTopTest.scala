import MatrixOperation._
import spinal.core.sim._

object SysTopTest extends App {
  implicit val config = new TPUConfig
  val matNum = config.matNum
  val dataWidth = config.DATA_WIDTH
  val weightWidth = config.WEIGHT_WIDTH
  val arraySize = config.ARRAY_SIZE
  val resWidth = config.RESULT_WIDTH
  val initData = Array.fill(matNum)(generateRandomUIntMatrix(weightWidth, arraySize))
  val initWeight = Array.fill(matNum)(generateRandomUIntMatrix(weightWidth, arraySize))
  val initMod = Array(Array(3), Array(8), Array(15), Array(9))
  val resMulRef = initData.zip(initWeight).map { case (l, r) => multiply(l, r, resWidth) }
  val resRef = initData.zip(initWeight).zipWithIndex.map {
    case ((l, r), idx) =>
      multiply(l, r,resWidth).map(_.map {
        case num =>

          num % initMod(idx)(0)
      }
      )
  }
  Config.sim.compile(new SysTop(initData, initWeight, initMod)).doSim { dut =>
    dut.clockDomain.forkStimulus(10)
    SimTimeout(1000 * 10)
    dut.io.start #= false
    dut.clockDomain.waitSampling()
    dut.io.start #= true
    val inputDataMat = dut.dataRamInput
    val inputWeightMat = dut.weightRamInput

    import myUtil.PrintDump._
    printMatrix(initData(0), "data0")
    //    printMatrix(initData(1), "data1")
    //    printMatrix(inputDataMat, "dataRam")
    printMatrix(initWeight(0), "weight0")
    //    printMatrix(initWeight(1),"weight1")
    //    printMatrix(inputWeightMat,"weightRam")
    printMatrix(resMulRef.reduce(Array.concat(_, _)), "resMulRef")
    printMatrix(resRef.reduce(Array.concat(_, _)), "resRef")

    while (!dut.io.done.toBoolean) {
      dut.clockDomain.waitSampling()
    }
    dut.io.start #= false

    printMatrix(dut.resRam.getMemInt().map(_.reverse), "resMemReversed")
  }
}
