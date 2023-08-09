class TPUConfig() {
  // size config
  val DATA_WIDTH = 8 //
  val WEIGHT_WIDTH = DATA_WIDTH
  val ARRAY_SIZE = 4

  val RESULT_WIDTH = DATA_WIDTH + WEIGHT_WIDTH // PE单元乘法结果的宽度

  // functional config
  private var MULTIPLY_CYCLE = 0 // 默认为异步乘法
  private var SKEW_INPUT = 1 // 默认开启输入倾斜
  private var SKEW_OUTPUT = 1 // 默认开启输出倾斜
  private var PE_OUTPUT_REG = 0 // 默认

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
    SKEW_INPUT = 0
  }

  /**
   * 取消输出倾斜模块
   */
  def noSkewOutput = {
    SKEW_OUTPUT = 0
  }

  /**
   * 添加PE输出寄存器
   */
  def hasPEOutputReg={
    PE_OUTPUT_REG = 1
  }
}

