import spinal.core._
import spinal.lib.fsm._
import spinal.lib._

class SysTop(implicit config: TPUConfig) extends Component {
  val io = new Bundle {
    val start = in Bool()
    val done = out Bool()
  }

  private val arraySize = config.ARRAY_SIZE
  private val dataWidth = config.DATA_WIDTH
  private val weightWidth = config.WEIGHT_WIDTH
  private val resWidth = config.RESULT_WIDTH


  import MatrixOperation._

  private val matNum = 2
  private val inputRamDepth = if (config.SKEW_INPUT) matNum * arraySize else matNum * arraySize + arraySize - 1
  private val outputRamDepth = if (config.SKEW_OUTPUT) matNum * arraySize else matNum * arraySize + arraySize - 1
  val initData = Array.fill(matNum)(generateRandomMatrix(dataWidth, arraySize, config.debug_noNegative))
  val initWeight = Array.fill(matNum)(generateRandomMatrix(weightWidth, arraySize, config.debug_noNegative))
  val resRef = initData.zip(initWeight).map { case (l, r) => multiply(l, r) }
  val dataRamInput = generateSRAMInput(initData, skew = !config.SKEW_INPUT)
  val weightRamInput = generateSRAMInput(initWeight, skew = !config.SKEW_INPUT)

  val systolic = new SystolicArray()
  val dataRam = new SRAMReadOnly(dataWidth, arraySize, inputRamDepth, dataRamInput)
  val weightRam = new SRAMReadOnly(weightWidth, arraySize, inputRamDepth, weightRamInput)
  val resRam = new SRAMWriteOnly(resWidth, arraySize, outputRamDepth)


  systolic.io.dataIn.payload.fragment := dataRam.io.rdata.data
  systolic.io.weightIn := weightRam.io.rdata.data

  val inputCnt = Counter(0, 20000)
  dataRam.io.addr := inputCnt.resized
  weightRam.io.addr := inputCnt.resized

  val lastCnt = Counter(0, arraySize - 1)

  val outputCnt = Counter(0, 20000)
  resRam.io.addr := outputCnt.resized
  resRam.io.wdata.data := systolic.io.resOut.payload.fragment
  resRam.io.valid := systolic.io.resOut.valid

  when(systolic.io.resOut.valid) {
    outputCnt.increment()
  }

  // TODO: 为什么这段代码就提示找不到idel类型？？？
//  val ctrlFsm = new StateMachine {
//    io.done := False
//    systolic.io.dataIn.valid := False
//    systolic.io.dataIn.last := False
//
//    val idle = new State with EntryPoint {
//      whenIsActive {
//        inputCnt.clear()
//        outputCnt.clear()
//        lastCnt.clear()
//        io.done := False
//        systolic.io.dataIn.valid := False
//        systolic.io.dataIn.last := False
//        when(io.start) {
//          goto(loadAndFlush)
//        }
//      }
//    }
//
//    val loadAndFlush = new State { // 从ram取数据，并发起flush信号清空PE内部的累加器
//      whenIsActive {
//        inputCnt.increment()
//        outputCnt.increment()
//        lastCnt.increment()
//        io.done := False
//        systolic.io.dataIn.valid := False
//        systolic.io.dataIn.last := True
//        goto(computing)
//      }
//    }
//
//    val computing = new State {
//      whenIsActive {
//        inputCnt.increment()
//        outputCnt.increment()
//        lastCnt.increment()
//        io.done := False
//        when(lastCnt === 0) {
//          systolic.io.dataIn.last := True
//        } otherwise {
//          systolic.io.dataIn.last := False
//        }
//
//        when(inputCnt <= inputRamDepth) { // TODO：边界有可能出错
//          systolic.io.dataIn.valid := True
//        } otherwise {
//          systolic.io.dataIn.valid := False
//        }
//
//        when(systolic.io.resOut.valid.fall) {
//          io.done := True
//          goto(idle)
//        }
//      }
//    }
//  }

  val fsm = new StateMachine {
    val idle = new State with EntryPoint
    val loadAndFlush = new State
    val computing = new State
    io.done := False
    systolic.io.dataIn.valid := False
    systolic.io.dataIn.last := False

    idle.whenIsActive {
      inputCnt.clear()
      outputCnt.clear()
      lastCnt.clear()
      io.done := False
      systolic.io.dataIn.valid := False
      systolic.io.dataIn.last := False
      when(io.start) {
        goto(loadAndFlush)
      }
    }

    loadAndFlush.whenIsActive {
      inputCnt.increment()
      outputCnt.increment()
      lastCnt.increment()
      io.done := False
      systolic.io.dataIn.valid := False
      systolic.io.dataIn.last := True
      goto(computing)
    }

    computing.whenIsActive{
      inputCnt.increment()
      outputCnt.increment()
      lastCnt.increment()
      io.done := False

      when(lastCnt === 0) {
        systolic.io.dataIn.last := True
      } otherwise {
        systolic.io.dataIn.last := False
      }

      when(inputCnt <= inputRamDepth) { // TODO：边界有可能出错
        systolic.io.dataIn.valid := True
      } otherwise {
        systolic.io.dataIn.valid := False
      }

      when(systolic.io.resOut.valid.fall) {
        io.done := True
        goto(idle)
      }
    }
  }

}

object SysTopGen extends App {
  implicit val config = new TPUConfig
  Config.spinal.generateVerilog(new SysTop())
}