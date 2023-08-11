class TPUConfig() {
  // size config
  val DATA_WIDTH = 3
  val WEIGHT_WIDTH = DATA_WIDTH
  val ARRAY_SIZE = 2

  val RESULT_WIDTH = DATA_WIDTH + WEIGHT_WIDTH // PE单元乘法结果的宽度

  // functional config
  var MULTIPLY_CYCLE = 0 // 默认为异步乘法
  var SKEW_INPUT = true // 默认开启输入倾斜
  var SKEW_OUTPUT = true // 默认开启输出倾斜
  var DATA_WEIGHT_INPUT_DELAY = true // 默认输入增加一级delay
  var RES_OUTPUT_DELAY = true // 默认输出增加一级delay

  var debug_noNegative = true

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

