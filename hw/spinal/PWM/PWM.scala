package PWM

import Config.Config
import spinal.core._
import spinal.lib._

/**
 * PWM信号生成模块
 * 可生成指定周期和占空比的PWM信号
 * 应用场景：
 * - 电机速度控制
 * - 音频信号生成
 * - 通信协议实现
 */
case class PWMGenerate(pwmWidth: Int ) extends Component {
  val io = slave(PWMIO(pwmWidth))
  val counter = Reg(UInt(pwmWidth bits)) init(0)
  when(counter < io.param.on+io.param.off) {
     counter := counter + 1
   }otherwise {
     counter := 0
    }
  when(io.param.no_pwm) {
    io.pwm:= False
  }otherwise {
    io.pwm:= counter < io.param.on
  }
}

object PWMGenerate {
  def apply (pwmWidth:Int,port:param,pwm:Bool ):Unit ={
    val PWMGenerate_inst=new PWMGenerate(pwmWidth)
    PWMGenerate_inst.io.param<>port
    PWMGenerate_inst.io.pwm<>pwm
  }
}



case class PWM() extends Component{
  val io =new Bundle{
    val pwm_in=in(Bool())
    val pwm_out=out(Bool())
    val sel=Bits(4 bits)
  }
  
  val PWMIN=new param(32)
  val PWMOUT=new param(32)
  PWMOUT.on:=io.sel.mux(
    1->PWMIN.on/5,
    4->PWMIN.on/10,
    8->PWMIN.on/20,
    16->PWMIN.on/50,
    default ->PWMIN.on
  )
  PWMOUT.off:=io.sel.mux(
    1->PWMIN.off/5,
    4->PWMIN.off/10,
    8->PWMIN.off/20,
    16->PWMIN.off/50,
    default ->PWMIN.off
  )
  PWMGenerate(32,PWMOUT,  io.pwm_out  )
  PWMMeasure(32,20000,PWMIN,  io.pwm_in)
}



object PWM extends App{
  Config.spinal.generateVerilog(PWM())
}