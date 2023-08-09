package myUtil
import spinal.lib._
import spinal.core.sim._
import spinal.core._

/**
 * 各种数据类型的转换
 */
object DataConvert{
  /**
   * 二进制字符串表示的有符号数转换为Int
   * @param str 二进制有符号数字符串
   * @return
   */
  def binString2Int(str: String): Int = {
    val s = str.toCharArray
    if (s(0) == '1') {
      for (i <- 1 until s.length) {
        if (s(i) == '1') {
          s(i) = '0'
        } else {
          s(i) = '1'
        }
      }
      -(s.tail.mkString.asBin + 1).toInt
    } else {
      s.mkString.asBin.toInt
    }
  }

  // Vec转
  // Vec转Array[Int]

}