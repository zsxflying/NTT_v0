import spinal.core._
import spinal.lib._
import spinal.lib.TraversableOnceAnyPimped


class SystolicArray(implicit config: TPUConfig) extends Component {

  val arraySize = config.ARRAY_SIZE
  val dataWidth = config.DATA_WIDTH
  val weightWidth = config.WEIGHT_WIDTH
  val resWidth = config.RESULT_WIDTH

  val io = new Bundle {
    val dataIn = slave Flow (Fragment(Vec.fill(arraySize)(SInt(dataWidth bits)))) // 数据控制信号沿着data方向传递
    val weightIn = in(Vec.fill(arraySize)(SInt(weightWidth bits)))
    val resOut = master Flow (Fragment(Vec.fill(arraySize)(SInt(resWidth bits))))
  }

  // 生成PE阵列
  val peArray = Array.fill(arraySize)(Array.fill(arraySize)(new PE()))

  val resTemp = Array.fill(arraySize)(Array.fill(arraySize)(SInt(resWidth bits)))
  val resData = Array.fill(arraySize)(SInt(resWidth bits))
  val resLast = peArray.last.last.io.mulres.valid
  val resValid = {
    if(config.SKEW_OUTPUT){
      peArray.head.map(_.io.mulres.valid).toSeq.reduceBalancedTree(_ | _) // 平衡二叉树
    } else {
      peArray.flatten.map(_.io.mulres.valid).toSeq.reduceBalancedTree(_ | _) // 平衡二叉树
    }
  }

  // 端口连接
  io.resOut.last := Delay(resLast, config.RES_OUTPUT_DELAY.toInt, init = False) // last看最后一个元素，delay最少
  if (config.SKEW_OUTPUT) { // valid看第一个元素，delay随第一个元素
    io.resOut.valid := Delay(resValid, arraySize - 1 + config.RES_OUTPUT_DELAY.toInt, init = False)
  } else {
    io.resOut.valid := Delay(resValid, config.RES_OUTPUT_DELAY.toInt, init = False)
  }



  // 确保最后一个输入也能从delay中输出
  val needDelay = io.dataIn.valid | Delay(io.dataIn.valid, arraySize - 1 + config.DATA_WEIGHT_INPUT_DELAY.toInt, init = False)

  for (i <- 0 until arraySize) {
    for (j <- 0 until arraySize) {
      // 阵列边缘连接
      if (i == 0) {
        peArray(i)(j).io.din.payload.weight := {
          if (config.SKEW_INPUT) {
            Delay(io.weightIn(j), j + config.DATA_WEIGHT_INPUT_DELAY.toInt, when = needDelay)
          } else {
            Delay(io.weightIn(j), config.DATA_WEIGHT_INPUT_DELAY.toInt, when = needDelay)
          }
        }
      }

      if (j == 0) {
        peArray(i)(j).io.din.payload.data := {
          if (config.SKEW_INPUT) {
            Delay(io.dataIn.payload(i), i + config.DATA_WEIGHT_INPUT_DELAY.toInt, when = needDelay)
          } else {
            Delay(io.dataIn.payload(i), config.DATA_WEIGHT_INPUT_DELAY.toInt, when = needDelay)
          }
        }
        peArray(i)(j).io.din.ctrl.valid := Delay(io.dataIn.valid, i + config.DATA_WEIGHT_INPUT_DELAY.toInt, init = False)
        peArray(i)(j).io.din.ctrl.lastOrFlush := Delay(io.dataIn.payload.last, i + config.DATA_WEIGHT_INPUT_DELAY.toInt, init = False)
      }

      // 阵列内部连接
      if (i != arraySize - 1) {
        peArray(i + 1)(j).io.din.payload.weight := peArray(i)(j).io.dout.payload.weight
      }
      if (j != arraySize - 1){
        peArray(i)(j + 1).io.din.payload.data := peArray(i)(j).io.dout.payload.data
        peArray(i)(j + 1).io.din.ctrl := peArray(i)(j).io.dout.ctrl
      }

      // 输出结果连接
      resTemp(i)(j) := peArray(i)(j).io.mulres.valid ? peArray(i)(j).io.mulres.payload | S(0).resized // 输出无效时置为0
    }
    resData(i) := resTemp.map(_(i)).toSeq.reduceBalancedTree(_ | _) // 按列进行规约

    io.resOut.payload(i) := {
      if (config.SKEW_OUTPUT) {
        Delay(resData(i), arraySize - 1 - i + config.RES_OUTPUT_DELAY.toInt, when = resValid || io.resOut.valid)
      } else {
        Delay(resData(i), config.RES_OUTPUT_DELAY.toInt, when = resValid)
      }
    }
  }


}

object SystolicArrayGen extends App {
  implicit val config = new TPUConfig
  Config.spinal.generateVerilog(new SystolicArray())
}