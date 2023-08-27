package myUtil

import scala.collection.mutable.ArrayBuffer
import scala.math
import scala.util.Random

class NTT {
  val mod = 65537 // 超过64位可能会出问题
  val g = 3
  val polyLen = 4

  def bigNumModPow(a: BigInt, b: BigInt, mod: BigInt): BigInt = {
    if (b == 0) {
      return 1
    }
    var res:BigInt = 1
    for (i <- BigInt(0) until b) {
      res *= a
      res %= mod
    }
    res
  }

  def twiddleMatrix(polyLen: Int, root: BigInt, mod: BigInt):Array[Array[BigInt]] = {
    val mat = Array.fill(polyLen)(Array.fill(polyLen)(BigInt(0)))
    for (i <- 0 until polyLen){
      val rootI = bigNumModPow(root, i, mod)
      for (j <- 0 until polyLen){
        val rootIJ = bigNumModPow(rootI,j,mod)
//        println(s"${i} ${j} ${rootI} ${rootIJ}")
        mat(i)(j) = rootIJ
      }
    }
    mat
  }

  def getRoot(n:Int = polyLen):BigInt={
    bigNumModPow(g, (mod - 1)/n, mod)
  }

  def getRandomInput(): Array[BigInt] = {
//    Random.setSeed(2)
    Array.fill(polyLen)(Random.nextInt(mod))
  }

  def getTwiddleMatrix(): Array[Array[BigInt]] = {
    twiddleMatrix(polyLen, getRoot(), mod)
  }

  def multiply(matrix0: Array[Array[BigInt]], matrix1: Array[Array[BigInt]], resWidth: Int): Array[Array[BigInt]] = {
    assert(matrix1.length == matrix0.length)
    val numRows = matrix0.length
    val resultMatrix = Array.fill(numRows)(Array.fill(numRows)(BigInt(0)))
    val maxValue: BigInt = (BigInt(1) << resWidth) - 1
    for (i <- 0 until numRows) {
      for (j <- 0 until numRows) {
        var sum: BigInt = 0
        for (k <- 0 until numRows) {
          sum += matrix0(i)(k) * matrix1(k)(j)
        }
        resultMatrix(i)(j) = sum.min(maxValue) % mod
      }
    }
    resultMatrix
  }
}

object NTT extends App {
  val ntt = new NTT
  val root = ntt.getRoot()
  println(root)
  val inputMat = Array.fill(ntt.polyLen)(ntt.getRandomInput()).transpose
  val twiddleMat = ntt.getTwiddleMatrix()
  import PrintDump._
  val res = ntt.multiply(twiddleMat, inputMat, 64)
  printMatrix(inputMat,"inputMatrix")
  printMatrix(twiddleMat,"twiddleMatrix")
  printMatrix(res,"result")

}