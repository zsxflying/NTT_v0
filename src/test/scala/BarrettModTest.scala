import spinal.core.sim._
import spinal.core._

import scala.collection.mutable._
import scala.util.Random


class BarrettModTest(dataWidth: Int, modWidth: Int, hasOutputReg: Boolean = false,k:Int,l:Int)
  extends BarrettModUnit(dataWidth, modWidth, hasOutputReg)(k,l) {
  class TestData(dataIn: BigInt, modIn: BigInt, k: Int) extends Bundle {
    val data = dataIn
    val mod = modIn
    val m = (BigInt(1) << k) / mod
  }

  private val dataQueue = Queue[TestData]()
  private val resQueue = Queue[BigInt]()
  private val resRefQueue = Queue[(BigInt, BigInt, BigInt)]()



  // 参考结果
  private def reference(data: BigInt, mod: BigInt) = {
    val res = data % mod
    resRefQueue.enqueue((data, mod, res))
  }

  // 产生模块输入
  private def portInDriver() = {
    val processIn = fork {
      while (true) {
        if (dataQueue.nonEmpty) {
          val dataTest = dataQueue.dequeue()
          reference(dataTest.data, dataTest.mod)
          io.din.valid #= true
          io.din.data #= dataTest.data
          io.din.mod #= dataTest.mod
          io.din.m #= dataTest.m
          clockDomain.waitSampling()
          io.din.valid #= false
        } else {
          clockDomain.waitSampling()
        }
      }
    }
  }


  // 获取模块输出
  private def portOutMonitor() = {
    val processOut = fork {
      while (true) {
        if (io.dout.valid.toBoolean) {
          resQueue.enqueue(io.dout.payload.toBigInt)
        }
        clockDomain.waitSampling()
      }
    }
  }

  // 判断正确性
  private def scoreBoard() = {
    val processScore = fork {
      while (true) {
        if (resQueue.nonEmpty && resRefQueue.nonEmpty) {
          val dutRes = resQueue.dequeue()
          val refRes = resRefQueue.dequeue()
          println(s"result: ${refRes}")
          assert(dutRes == refRes._3, s"Error: mismatch ref: ${refRes}; dut result: ${dutRes}")
        }
        clockDomain.waitSampling()
      }
    }
  }

  private def genData(doFullTest:Boolean) = {
    val dataArray = ArrayBuffer[BigInt]()
    val modArray = ArrayBuffer[BigInt]()
    // 确保mod在 (2^l, 2^modWidth) 之间
    val modMin = (BigInt(1) << l) + 1
    val modMax = (BigInt(1) << modWidth) - 1

    clockDomain.waitSampling()
    if (!doFullTest){
      // 随机测试
      // 生成data的测试值
      // 测试随机数
      for (i <- 0 until 10) {
        dataArray += BigInt(dataWidth, Random)
      }

      // 测试边界
      for (i <- 0 to 10) {
        dataArray += BigInt(i) // 下边界
        dataArray += (BigInt(1) << dataWidth) - 1 - i // 上边界
      }

      // 生成mod的测试值
      // 测试随机数
      for (i <- 0 until 10) {
        var mod = BigInt(modWidth, Random)
        while (mod < modMin) {
          mod = BigInt(modWidth, Random)
        }
        modArray += mod
      }
      // 测试边界
      for (i <- 0 to 10) {
        modArray += modMin + i // 下边界
        val mod2 = modMax - i
        assert(mod2 >= modMin,"generate modArray: mod less than modMin")
        modArray += mod2 // 上边界
      }

    } else {
      // 完全覆盖测试
      // 生成data的测试值
      (BigInt(0) to ((BigInt(1) << dataWidth) - 1)).toArray.foreach(dataArray += _)
      (modMin to modMax).toArray.foreach(modArray += _)
    }

    // 取dataArray和modArray的笛卡尔积作为输入
    dataArray.flatMap(x => modArray.map(y => (x,y))).foreach{ case (data, mod) =>
      val testData = new TestData(data, mod, k)
      dataQueue.enqueue(testData)
    }


    while (dataQueue.nonEmpty || resRefQueue.nonEmpty || resQueue.nonEmpty) {
      clockDomain.waitSampling()
    }
  }

}

object BarrettModTest extends App {
  var dataWidth = 16
  var modWidth = 5
  var hasOutputReg = false
  val k = dataWidth + 1
  val l = modWidth - 3

  Config.sim.compile {
    val dut = new BarrettModTest(dataWidth, modWidth, hasOutputReg,k,l)
    dut
  }.doSim { dut =>
    // init
    val din = dut.io.din
    dut.clockDomain.forkStimulus(10)
    din.valid #= false
    dut.clockDomain.waitSampling(2)

    // test
    dut.portInDriver()
    dut.portOutMonitor()
    dut.scoreBoard()

    dut.genData(doFullTest = false)
  }
}
