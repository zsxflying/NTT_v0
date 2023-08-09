package myUtil

import spinal.core._
import spinal.core.sim._

// TODO: 是否可以利用泛型简化？
object Accumulator{
  def apply(data:SInt, valid:Bool) = {
    new Area{
      val value = Reg(data, S(0))
      when(valid) {
        value := data + value
      }
    }
  }

  def apply(data: SInt, valid: Bool, lastOrFlush: Bool) = {
    new Area {
      val lastOrFlushReg = RegNext(lastOrFlush)
      val value = Reg(data)
      when(lastOrFlushReg){
        value := data
      }.elsewhen(valid){
        value := data + value
      }
    }
  }

  def apply(data:UInt, valid:Bool)={
    new Area {
      val value = Reg(data, U(0))
      when(valid) {
        value := data + value
      }
    }
  }
}
