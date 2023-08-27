package myUtil
import scala.util.Random
import java.io._

object MatrixOperation {
  /**
   * 生成随机矩阵
   *
   * @param dataWidth
   * @param size
   * @return
   */
  def generateRandomSIntMatrix(dataWidth: Int, size: Int, noNegative: Boolean = false): Array[Array[Int]] = {
    val random = new Random()
    val matrix = Array.ofDim[Int](size, size)
    val minValue = -(1 << (dataWidth - 1))
    val maxValue = (1 << (dataWidth - 1)) - 1

    // 随机生成dataWidth宽度的有符号整数所能表示的整数并填充矩阵
    for (i <- 0 until size) {
      for (j <- 0 until size) {
        if (noNegative) {
          matrix(i)(j) = random.nextInt(maxValue)
        } else {
          matrix(i)(j) = random.nextInt(maxValue - minValue + 1) + minValue
        }
      }
    }

    matrix
  }

  def generateRandomUIntMatrix(dataWidth: Int, size: Int): Array[Array[Int]] = {
    val random = new Random()
    val matrix = Array.ofDim[Int](size, size)
    val maxValue = (1 << dataWidth) - 1

    // 随机生成dataWidth宽度的有符号整数所能表示的整数并填充矩阵
    for (i <- 0 until size) {
      for (j <- 0 until size) {
        matrix(i)(j) = random.nextInt(maxValue + 1)
      }
    }

    matrix
  }

  def generateMaxUIntMatrix(dataWidth: Int, size: Int): Array[Array[Int]] = {
    val maxvalue = (1 << dataWidth) - 1
    val matrix = Array.fill(size)(Array.fill(size)(maxvalue))
    matrix
  }

  // TODO: 矩阵乘法算法可以优化

  /**
   * 计算矩阵乘法
   *
   * @param matrix0
   * @param matrix1
   * @return
   */
  def multiply(matrix0: Array[Array[Int]], matrix1: Array[Array[Int]], resWidth:Int) = {
    assert(matrix1.length == matrix0.length)
    val numRows = matrix0.length
    val resultMatrix= Array.fill(numRows)(Array.fill(numRows)(0))
    val maxValue = (1 << resWidth) - 1

    for (i <- 0 until numRows) {
      for (j <- 0 until numRows) {
        for (k <- 0 until numRows) {
          resultMatrix(i)(j) += matrix0(i)(k) * matrix1(k)(j)
        }
        if (resultMatrix(i)(j) > maxValue) resultMatrix(i)(j) = maxValue
      }
    }

    resultMatrix
  }

  /**
   * 生成sram存储数据
   *
   * @param mat0
   * @param mat1
   * @param mat2
   * @return
   */
  def generateSRAMInput(
                         matArray: Array[Array[Array[Int]]],
                         skew: Boolean
                       ): Array[Array[Int]] = {

    val arraySize = matArray.length
    val matSize = matArray(0)(0).length
    val result = if (skew) {
      Array.fill(arraySize * matSize + matSize - 1)(Array.fill(matSize)(0))
    } else {
      Array.fill(arraySize * matSize)(Array.fill(matSize)(0))
    }

    for (i <- 0 until arraySize) {
      for (j <- 0 until matSize) {
        for (k <- 0 until matSize) { // 需要左右翻转
          if (skew) {
            result(i * matSize + j + k)(k) = matArray(i)(j)(matSize - 1 - k)
          } else {
            result(i * matSize + j)(k) = matArray(i)(j)(matSize - 1 - k)
          }

        }
      }
    }

    result
  }

  /**
   * 将结果sram中的数据转换为人类易读的格式
   *
   * @param mat
   * @return
   */
  def transformResultMatrix(sramData: Array[Array[Int]]): Array[Array[Int]] = {
    val matSize = sramData(0).length
    val result = Array.fill(matSize)(Array.fill(matSize)(0))
    val depth = matSize * 2 - 1

    for (i <- 0 until matSize) {
      for (j <- 0 until matSize) {
        val idxSum = i + j
        if (idxSum < matSize) {
          result(i)(j) = sramData(idxSum)(i)
        } else {
          result(i)(j) = sramData(idxSum)(matSize - 1 - j)
        }
      }
    }

    result
  }
}

object MatrixOperationTest extends App {

  import MatrixOperation._
  import myUtil.PrintDump._

  val dataWidth = 8
  val matSize = 3
  val sramResDepth = matSize * 2 - 1
  // 生成矩阵
  val matrixLeftSet, matrixRightSet = Array.fill(3)(generateRandomSIntMatrix(dataWidth, matSize))
  printMatrix(matrixLeftSet(0))
  printMatrix(matrixLeftSet(1))
  printMatrix(generateSRAMInput(matrixLeftSet, false))

}

