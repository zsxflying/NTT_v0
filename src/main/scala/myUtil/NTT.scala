package myUtil

import scala.collection.mutable.ArrayBuffer
import scala.math
import scala.util.Random

class NTT {
  val mod = 65537
  val g = 3
  val polyLen = 4

  def bigNumModPow(a: Int, b: Int, mod: Int): Int = {
    if (b == 0) {
      return 1
    }
    var res:BigInt = 1
    for (i <- 0 until b) {
      res *= a
      res %= mod
    }
    res.toInt
  }

  def twiddleMatrix(polyLen: Int, root: Int, mod: Int):Array[Array[Int]] = {
    val mat = Array.fill(polyLen)(Array.fill(polyLen)(0))
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

  def getRoot(n:Int = polyLen):Int={
    bigNumModPow(g, (mod - 1)/n, mod)
  }

  def getRandomInput(): Array[Int] = {
//    Random.setSeed(2)
    Array.fill(polyLen)(Random.nextInt(mod))
  }

  def getTwiddleMatrix(): Array[Array[Int]] = {
    twiddleMatrix(polyLen, getRoot(), mod)
  }

  def multiply(matrix0: Array[Array[Int]], matrix1: Array[Array[Int]], resWidth: Int) = {
    assert(matrix1.length == matrix0.length)
    val numRows = matrix0.length
    val resultMatrix = Array.fill(numRows)(Array.fill(numRows)(BigInt(0)))
    val maxValue:BigInt = (BigInt(1) << resWidth) - 1
    for (i <- 0 until numRows) {
      for (j <- 0 until numRows) {
        var sum:BigInt = 0
        for (k <- 0 until numRows) {
          val a = BigInt(matrix0(i)(k))
          val b = BigInt(matrix1(k)(j))
          sum += a * b
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