import scala.util.Random
import java.io._

object MatrixOperation {
  /**
   * 生成随机矩阵
   * @param dataWidth
   * @param size
   * @return
   */
  def generateRandomMatrix(dataWidth: Int, size: Int): Array[Array[Int]] = {
    val random = new Random()
    val matrix = Array.ofDim[Int](size, size)
    val minValue = -(1 << (dataWidth - 1))
    val maxValue = (1 << (dataWidth - 1)) - 1

    // 随机生成dataWidth宽度的有符号整数所能表示的整数并填充矩阵
    for (i <- 0 until size) {
      for (j <- 0 until size) {
        matrix(i)(j) = random.nextInt(maxValue - minValue + 1) + minValue
      }
    }

    matrix
  }

  // TODO: 矩阵乘法算法可以优化
  /**
   * 计算矩阵乘法
   * @param matrix0
   * @param matrix1
   * @return
   */
  def multiply(matrix0: Array[Array[Int]], matrix1: Array[Array[Int]]) = {
    assert(matrix1.length == matrix0.length)
    val numRows = matrix0.length
    val resultMatrix = Array.fill(numRows)(Array.fill(numRows)(0))

    for (i <- 0 until numRows) {
      for (j <- 0 until numRows) {
        for (k <- 0 until numRows) {
          resultMatrix(i)(j) += matrix0(i)(k) * matrix1(k)(j)
        }
      }
    }

    resultMatrix
  }

  /**
   * 生成sram存储数据
   * @param mat0
   * @param mat1
   * @param mat2
   * @return
   */
  def generateSRAMInput(
                         mat0: Array[Array[Int]],
                         mat1: Array[Array[Int]],
                         mat2: Array[Array[Int]]
                       ): Array[Array[Int]] = {
    val n = mat0.length
    val result = Array.fill(4 * n - 1)(Array.fill(n)(0))

    for (i <- 0 until 3 * n) {
      for (j <- 0 until n) {
        if (j < n/2){
          result(i + j)(j) = {
            if (i < n) {
              mat0(i)(j)
            } else if (i < 2 * n) {
              mat1(i - n)(j)
            } else {
              mat2(i - 2 * n)(j)
            }
          }
        } else {
          result(i+j-n/2)(j) = {
            if (i < n) {
              mat0(i)(j)
            } else if (i < 2 * n) {
              mat1(i - n)(j)
            } else {
              mat2(i - 2 * n)(j)
            }
          }
        }
      }
    }

    result
  }

  /**
   * 将结果sram中的数据转换为人类易读的格式
   * @param mat
   * @return
   */
  def transformResultMatrix(sramData: Array[Array[Int]]): Array[Array[Int]] = {
    val matSize = sramData(0).length
    val result = Array.fill(matSize)(Array.fill(matSize)(0))
    val depth = matSize * 2 - 1

    for (i <- 0 until matSize){
      for (j <- 0 until matSize){
        val idxSum = i + j
          if (idxSum < matSize){
            result(i)(j) = sramData(idxSum)(i)
          }else{
            result(i)(j) = sramData(idxSum)(matSize - 1 - j)
          }
      }
    }

    result
  }
}

object MatrixOperationTest extends App{
  import MatrixOperation._
  import myUtil.PrintDump._

  val dataWidth = 8
  val matSize = 3
  val sramResDepth = matSize * 2 - 1
  // 生成矩阵
  val matrixLeftSet, matrixRightSet = Array.fill(3)(generateRandomMatrix(dataWidth, matSize))

  // 参考结果
  val refResult = matrixLeftSet.zip(matrixRightSet).map { element =>
    multiply(element._1, element._2)
  }

  // 输入sram
  val sramD = generateSRAMInput(matrixLeftSet(0), matrixLeftSet(1), matrixLeftSet(2))
  val sramW = generateSRAMInput(matrixRightSet(0), matrixRightSet(1), matrixRightSet(2))

  println("matLeft:")
  matrixLeftSet.foreach(printMatrix(_))
  println("sramD:")
  printMatrix(sramD)
  println("matRight:")
  matrixRightSet.foreach(printMatrix(_))
  println("sramW:")
  printMatrix(sramW)

  // 结果sram
  val sramRes0 = Array.fill(sramResDepth)(Array.fill(matSize)(Random.nextInt(10)))

  // 结果sram转换后数据
  val transRes0 = transformResultMatrix(sramRes0)
  println("sramRes0:")
  printMatrix(sramRes0)
  println("transRes0")
  printMatrix(transRes0)
}

