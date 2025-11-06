package Segment
import Config._
import spinal.core._
import spinal.lib._

// 定义数码管类型枚举
object SegmentType {
  val COMMON_ANODE = 0  // 共阳极
  val COMMON_CATHODE = 1  // 共阴极
}

// BCD转七段数码管显示码的组合逻辑模块
case class BcdToSegment(segmentType: Int = SegmentType.COMMON_CATHODE) extends Component {
  val io = new Bundle {
    val bcd = in Bits(4 bits)   // BCD输入 (0-15)
    val seg = out Bits(8 bits)  // 七段输出 + 小数点
  }
  
  // 组合逻辑实现BCD到七段显示码的转换
  val decoded = Bits(8 bits)
  decoded := io.bcd.mux( // 默认不亮
    0  -> B"8'b11000000", // 0
    1  -> B"8'b11111001", // 1
    2  -> B"8'b10100100", // 2
    3  -> B"8'b10110000", // 3
    4  -> B"8'b10011001", // 4
    5  -> B"8'b10010010", // 5
    6  -> B"8'b10000010", // 6
    7  -> B"8'b11111000", // 7
    8  -> B"8'b10000000", // 8
    9  -> B"8'b10010000", // 9
    10 -> B"8'b10001000", // A
    11 -> B"8'b10000011", // b
    12 -> B"8'b11000110", // C
    13 -> B"8'b10100001", // d
    14 -> B"8'b10000110", // E
    15 -> B"8'b10001110"  // F
  )
  
  // 根据数码管类型决定是否取反
  if (segmentType == SegmentType.COMMON_ANODE) {
    // 共阳极需要取反
    io.seg := ~decoded
  } else {
    // 共阴极直接输出
    io.seg := decoded
  }
}

// 七段数码管接口定义
case class Segment_interface(count:Int,group:Int) extends Bundle with IMasterSlave {
  // 每个组的8位控制信号（7段+小数点）
  val led = Vec(Bits(8 bits), group)
  // 选择哪个数码管显示
  val select = Vec(Bits(count bits),group)
  
  override def asMaster(): Unit = {
    out(led, select)
  }
}

// 数码管控制器基类
case class Segment(count:Int, group:Int, segmentType:Int = SegmentType.COMMON_CATHODE) extends Component  {

  
  val io = new Bundle {
    // 输入要显示的数字
    val digitData = in(Vec(Vec(Bits(4 bits),count),group))
    // 输入使能信号
    val enable = in Bool()
    // 输出到硬件的接口
    val segmentCtrl = master(Segment_interface(count, group))
  }
  
  // 内部信号定义
  val digitSelect = Reg(Bits(count bits)) init(1)
  val displayData = Vec(Vec(Reg(Bits(4 bits)),count), group)

  // 当使能时更新显示数据
  when(io.enable) {
    for(i<-0 until group){
    displayData(i) := io.digitData(i)
    }
  }
  
  
  
  // 如果每组只有1个数码管，则不需要扫描
  if (count == 1) {
    digitSelect := 1
  }else {
  // 扫描选择下一个数码管
  digitSelect := digitSelect.rotateLeft(1)
  }

  
  // 根据选择的数码管确定显示内容
  for (i <- 0 until group) {

    io.segmentCtrl.select(i) := digitSelect
    // 连接输出信号
    val bcd2d = BcdToSegment(segmentType)
    // 驱动数码管显示
    bcd2d.io.bcd := MuxOH(io.segmentCtrl.select(i),displayData(i))
    io.segmentCtrl.led(i) := bcd2d.io.seg

  }

}

object Segment extends App{
  Config.spinal.generateVerilog(Segment(4,2))
}