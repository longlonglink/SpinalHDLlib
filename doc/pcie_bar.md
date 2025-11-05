# PCIE 枚举流程
## ID
BIOS重启后，初始化PCIE总线，搜索PCIE设备  
BIOS在搜索过程中会根据CPU发现PCIE设备的顺序分配ID  
PCIE ID  [15:0] 包括 Bus ID [7:0]  Device ID[4:0]  Function ID[2:0]  
总线最多256条，一条总线最多32个设备，PCIE是点对点，一条总线一般对应一个设备，一个设备最多8个功能。  
不同的PCIE插槽对应不同的总线ID。 
PCIE枚举时会去读取厂商ID确认总线上设备是否存在。一旦确认后会将分配的ID写入对应的配置寄存器中。  
驱动可以通过厂商ID和器件ID来进行匹配。也可以通过Class ID 来匹配通用的免驱ID。

## RC,SWITCH  
RC和SWITCH中的Device ID一般硬件已经固定，一个Virtual P2P 桥接Device的 ID对应一个端口。RC有多个下游端口，一个端口一般有自己独立的Bus ID。SWITCH一般有一个上游接口和多个下游接口，用于扩展RC的接口。
SWITCH上游Bus ID一般由连接的RC对应下游端口决定。  
## Type 0  
RC和EP通过Type 0交换配置信息，一个Type 0配置空间对应一个Function，也就是说有几个Funtion就会有几个Type 0配置空间。只有RC有权限去读写EP的配置空间。  
Type 1 配置空间一般由RC,SWITCH使用。  
Type 0兼容配置空间的大小为256字节，前64字节为配置头，后192字节放置的是Capability Structure。PCIE扩展配置空间是3840字节，和兼容配置空间加起来为4096字节。  
## BAR
一个配置空间有6个32bit的BAR寄存器。 
BAR[31:0]  BAR[0] 指示BAR映射到IO（1）还是Mem空间（0）， BAR[2：1] 为00指示32bit地址译码，10指示64bit地址译码。        BAR[3] 指示是否支持cache预取   
PCIE规定最小申请空间为128字节。 
BIOS扫描到Function后会从BAR0顺序读取到BAR5，根据BAR寄存器中遇到0的数量N申请（过大可能会申请失败）2^（N+4）空间的内存（不是识别非1的bit），并且将申请到的基地址（地址对齐）传给RC写入EP的对应BAR寄存器。
## VT-d ATS ACS
