import MatrixOperation.generateRandomSIntMatrix
import spinal.core._
import spinal.lib.fsm._
import spinal.lib._

class SysTop(
              initData: Array[Array[Array[Int]]],
              initWeight: Array[Array[Array[Int]]],
              initMod: Array[Array[Int]]
            )(implicit config: TPUConfig) extends Component {
  val io = new Bundle {
    val start = in Bool()
    val done = out Bool()
  }

  private val arraySize = config.ARRAY_SIZE
  private val dataWidth = config.DATA_WIDTH
  private val weightWidth = config.WEIGHT_WIDTH
  private val resWidth = config.RESULT_WIDTH
  private val modWidth = config.MOD_WIDTH

  import MatrixOperation._

  private val matNum = config.matNum
  private val inputRamDepth = if (config.SKEW_INPUT) matNum * arraySize else matNum * arraySize + arraySize - 1
  private val outputRamDepth = if (config.SKEW_OUTPUT) matNum * arraySize else matNum * arraySize + arraySize - 1

  val dataRamInput = generateSRAMInput(initData.map(_.transpose), skew = !config.SKEW_INPUT) // data需要转置
  val weightRamInput = generateSRAMInput(initWeight, skew = !config.SKEW_INPUT)

  val systolic = new SystolicArray()
  val dataRam = new SRAMReadOnly(dataWidth, arraySize, inputRamDepth, dataRamInput)
  val weightRam = new SRAMReadOnly(weightWidth, arraySize, inputRamDepth, weightRamInput)
  val resRam = new SRAMWriteOnly(resWidth, arraySize, outputRamDepth)
  val modRam = new SRAMReadOnly(modWidth, 1, matNum, initMod)


  systolic.io.dataIn.payload.fragment := dataRam.io.rdata.data
  systolic.io.weightIn := weightRam.io.rdata.data
  systolic.io.modIn.payload.assignFrom(modRam.io.rdata.data.asBits.asUInt)

  val inputCnt = Counter(0, (matNum + 3) * arraySize)
  dataRam.io.addr := inputCnt.resized
  weightRam.io.addr := inputCnt.resized

  val lastCnt = Counter(0, arraySize - 1)

  val outputCnt = Counter(0, (matNum + 3) * arraySize)
  resRam.io.addr := outputCnt.resized
  resRam.io.wdata.data := systolic.io.resOut.payload.fragment
  resRam.io.valid := systolic.io.resOut.valid

  when(systolic.io.resOut.valid) {
    outputCnt.increment()
  }

  val modCnt = Counter(0, arraySize - 1)
  val modAddrCnt = Counter(0, matNum - 1)
  modRam.io.addr := modAddrCnt


  // TODO: 为什么这段代码就提示找不到idel类型？？？(内部逻辑非最新）

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
    systolic.io.modIn.valid := False

    idle.whenIsActive {
      inputCnt.clear()
      outputCnt.clear()
      lastCnt.clear()
      modCnt.clear()
      modAddrCnt.clear()
      io.done := False
      systolic.io.dataIn.valid := False
      systolic.io.dataIn.last := False
      systolic.io.modIn.valid := False
      when(io.start) {
        goto(loadAndFlush)
      }
    }

    loadAndFlush.whenIsActive {
      inputCnt.increment()
      lastCnt.increment()
      io.done := False
      systolic.io.dataIn.valid := False
      systolic.io.dataIn.last := True
      systolic.io.modIn.valid := True
      goto(computing)
    }

    computing.whenIsActive {
      inputCnt.increment()
      lastCnt.increment()
      io.done := False
      systolic.io.modIn.valid := True
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

      when(systolic.io.modIn.ready){
        modCnt.increment()
      }
      when(modCnt === U(arraySize - 1)){
        modAddrCnt.increment()
      }
    }
  }

}

object SysTopGen extends App {
  implicit val config = new TPUConfig
  val matNum = config.matNum
  val dataWidth = config.DATA_WIDTH
  val weightWidth = config.WEIGHT_WIDTH
  val arraySize = config.ARRAY_SIZE
  val initData = Array.fill(matNum)(generateRandomSIntMatrix(dataWidth, arraySize, config.debug_noNegative))
  val initWeight = Array.fill(matNum)(generateRandomSIntMatrix(weightWidth, arraySize, config.debug_noNegative))
  val initMod = Array.fill(matNum)(Array(13))
  Config.spinal.generateVerilog(new SysTop(initData, initWeight,initMod))
}