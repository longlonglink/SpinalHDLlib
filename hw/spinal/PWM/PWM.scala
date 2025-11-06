package PWM

import Config.Config
import spinal.core._
import spinal.lib.slave

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
  when(counter < io.on+io.off) {
     counter := counter + 1
   }otherwise {
     counter := 0
    }
  when(io.no_pwm) {
    io.pwm:= False
  }otherwise {
    io.pwm:= counter < io.on
  }
}

