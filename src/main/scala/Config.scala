import spinal.core._
import spinal.core.sim._

object Config {
  val dumpDir = s"dump/" // 打印文件所在目录

  def spinal = SpinalConfig(
    targetDirectory = "gen",
    defaultConfigForClockDomains = ClockDomainConfig(
      resetActiveLevel = LOW, // 复位信号低电平有效
      resetKind = SYNC // 同步复位，要设置，否则会出现先取消复位再产生时钟信号的情况
    ),
    onlyStdLogicVectorAtTopLevelIo = true
  )

  def sim = SimConfig.withConfig(spinal).withWave.withVcdWave.withVerilator

}
