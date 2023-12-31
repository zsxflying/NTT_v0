import spinal.core.log2Up

class TPUConfig() {
  // size config
  val DATA_WIDTH = 16  // 不能小于3
  val WEIGHT_WIDTH = DATA_WIDTH
  val ARRAY_SIZE = 8
  val MOD_WIDTH = DATA_WIDTH

  val MUL_RES_WIDTH = DATA_WIDTH + WEIGHT_WIDTH + log2Up(ARRAY_SIZE) // PE单元乘法结果宽度
  val MOD_RES_WIDTH = MOD_WIDTH // 求模后结果的宽度

  // functional config, 仅测试了默认的配置
  var MULTIPLY_CYCLE = 0 // 默认为异步乘法
  var SKEW_INPUT = true // 默认开启输入倾斜
  var SKEW_OUTPUT = true // 默认开启输出倾斜
  var DATA_WEIGHT_INPUT_DELAY = true // 默认输入增加一级delay
  var RES_OUTPUT_DELAY = true // 默认输出增加一级delay


  var matNum = 4 // 初始随机矩阵的个数
  var debug_noNegative = true // 初始随机数组是否包含负数


  /**
   * 设置乘法器的运算周期
   * @param cycle 运算周期，0为异步
   */
  def setMultiplyCycle(cycle:Int): Unit = {
    MULTIPLY_CYCLE = cycle
  }

  /**
   * 取消输入倾斜模块
   */
  def noSkewInput = {
    SKEW_INPUT = false
  }

  /**
   * 取消输出倾斜模块
   */
  def noSkewOutput = {
    SKEW_OUTPUT = false
  }

  /**
   * 取消data、weight倾斜阵列的一级delay
   */
  def noDataWeightInputDelay={
    DATA_WEIGHT_INPUT_DELAY = false
  }

  /**
   * 取消result倾斜阵列的一级delay
   */
  def noResOutputDelay = {
    RES_OUTPUT_DELAY = false
  }
}

