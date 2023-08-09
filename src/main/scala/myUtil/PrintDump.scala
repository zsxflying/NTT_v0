package myUtil
import java.io._

object PrintDump{
  /**
   * 打印二维矩阵到标准输出
   * @param mat
   * @param str
   * @param sep 分隔符
   * @tparam T
   */
  def printMatrix[T](mat: Array[Array[T]], str:String = "", sep: String = ", ") = {
    if (str.nonEmpty) println(s"--------${str}--------")
    for (row <- mat) {
      println(row.mkString(sep))
    }
    println()
  }

  /**
   * 打印二维矩阵到文件
   * @param mat
   * @param fileName
   * @param path 目录路劲
   * @param sep 分隔符
   * @tparam T
   */
  def dumpMatrix2File[T](mat: Array[Array[T]], fileName: String, path: String = "dump/", sep: String = ", ") = {
    val dumpDir = path
    val dir = new File(dumpDir)
    if (!dir.exists()) {
      dir.mkdir()
    }

    val file = new File(dumpDir + fileName)
    val writer = new PrintWriter(file)

    for (row <- mat) {
      writer.println(row.mkString(sep))
    }

    writer.close()
  }

}