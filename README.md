# NTT_v0
NTT加速器demo

## 运行
- 在TPUConifg类中可以修改加速器的配置
- 在SysTopTest中进行仿真

## 说明（自用）
- 每次**冷启动**，必须先插入一个周期的lastOrFlush信号来清空累加器。该信号复位时为0。
- 输入的last和输出的last不是同一个last。
  - 输入的last指示累加器结果是否设为有效
  - 输出的last指示输出的是否是当前矩阵的最后元素。