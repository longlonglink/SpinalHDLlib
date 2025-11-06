package PWM

import spinal.core._
import spinal.lib._

/**
 * PWM测量模块IO定义
 * 用于测量外部PWM信号的on和off时间
 */
case class PWMIO(pwmWidth: Int ) extends Bundle with IMasterSlave {
  // 输入PWM信号
  val pwm = in Bool()

  // 输出测量结果
  val param=new param(pwmWidth)
  override def asMaster(): Unit = {
    in(pwm)
    out(param.on, param.off,param.no_pwm)
  }
}
case class param(pwmWidth: Int) extends Bundle{
  val on=  UInt(pwmWidth bits)       // 频率 (Hz)
  val off = UInt(pwmWidth bits)
  val no_pwm =  Bool()
}
/**
 * PWM测量模块
 * 测量输入PWM信号的频率和占空比
 * 
 * 工作原理：
 * 1. 检测PWM信号的上升沿和下降沿
 * 2. 测量高电平和低电平持续时间
 * 3. 计算周期、频率和占空比
 * 
 * @param pwmWidth 计数器位宽
 * @param period 周期
 */
case class PWMMeasure(pwmWidth: Int , period:Int) extends Component {
  val io = master(PWMIO(pwmWidth))
    // 高电平和低电平计数器
  val highCounter = Reg(UInt(pwmWidth bits)) init(0)
  val lowCounter = Reg(UInt(pwmWidth bits)) init(0)
  val highMAX = Reg(UInt(pwmWidth bits)) init(0)
  val lowMAX = Reg(UInt(pwmWidth bits)) init(0)
when(io.pwm) {
    lowCounter := 0
    highCounter := highCounter + 1
    when(highCounter > highMAX) {
      highMAX :=  highCounter
    }
  } otherwise {
    highCounter := 0
    lowCounter := lowCounter + 1
    when(lowCounter > lowMAX) {
      lowMAX := lowCounter
    }
  }

when (highMAX<period|| lowMAX<period)
{ 
  io.param.on := highMAX
  io.param.off := lowMAX
  io.param.no_pwm := False

} otherwise {
    io.param.on := 0
    io.param.off := 0
    io.param.no_pwm := True
}
}
object PWMMeasure extends App {
  import Config.Config
  Config.spinal.generateVerilog(PWMMeasure(32, 1000))
  def apply (pwmWidth:Int,period:Int,port:param,pwm:Bool): Unit = {
    val PWMMeasure_inst=new PWMMeasure(pwmWidth,period)
    PWMMeasure_inst.io.param<>port
    PWMMeasure_inst.io.pwm<>pwm
  }
}