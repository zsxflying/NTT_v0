// Generator : SpinalHDL v1.9.0    git head : 7d30dbacbd3aa1be42fb2a3d4da5675703aae2ae
// Component : PE
// Git hash  : 3da9f98179e2b3db086cdb734670c0513080b900

`timescale 1ns/1ps

module PE (
  input      [7:0]    io_din_payload_data,
  input      [7:0]    io_din_payload_weight,
  input               io_din_ctrl_valid,
  input               io_din_ctrl_lastOrFlush,
  output     [7:0]    io_dout_payload_data,
  output     [7:0]    io_dout_payload_weight,
  output              io_dout_ctrl_valid,
  output              io_dout_ctrl_lastOrFlush,
  output              io_mulres_valid,
  output     [15:0]   io_mulres_payload,
  input               clk,
  input               resetn
);

  wire       [2:0]    _zz_cond0;
  wire       [2:0]    _zz_cond1;
  wire       [0:0]    _zz_cond1_1;
  wire       [15:0]   _zz_io_mulres_payload;
  wire       [15:0]   _zz_io_mulres_payload_1;
  reg        [7:0]    dinPayloadReg_data;
  reg        [7:0]    dinPayloadReg_weight;
  reg                 dinCtrlReg_valid;
  reg                 dinCtrlReg_lastOrFlush;
  wire       [15:0]   multiplyRes;
  wire       [17:0]   _zz_resAccReg_value;
  reg                 resAccReg_lastOrFlushReg;
  reg        [17:0]   resAccReg_value;
  wire                resAccRegValue0;
  wire       [2:0]    resAccRegValue1;
  wire       [15:0]   MAX_VALUE;
  wire       [15:0]   MIN_VALUE;
  wire                cond0;
  wire                cond1;

  assign _zz_cond0 = 3'b000;
  assign _zz_cond1_1 = 1'b1;
  assign _zz_cond1 = {{2{_zz_cond1_1[0]}}, _zz_cond1_1};
  assign _zz_io_mulres_payload = (cond1 ? MIN_VALUE : _zz_io_mulres_payload_1);
  assign _zz_io_mulres_payload_1 = resAccReg_value[15:0];
  assign io_dout_payload_data = dinPayloadReg_data;
  assign io_dout_payload_weight = dinPayloadReg_weight;
  assign io_dout_ctrl_valid = dinCtrlReg_valid;
  assign io_dout_ctrl_lastOrFlush = dinCtrlReg_lastOrFlush;
  assign multiplyRes = ($signed(dinPayloadReg_data) * $signed(dinPayloadReg_weight));
  assign _zz_resAccReg_value = {{2{multiplyRes[15]}}, multiplyRes};
  assign resAccRegValue0 = resAccReg_value[17];
  assign resAccRegValue1 = resAccReg_value[17 : 15];
  assign MAX_VALUE = 16'h7fff;
  assign MIN_VALUE = 16'h8000;
  assign cond0 = ((resAccRegValue0 == 1'b0) && ($signed(resAccRegValue1) != $signed(_zz_cond0)));
  assign cond1 = ((resAccRegValue0 == 1'b1) && ($signed(resAccRegValue1) != $signed(_zz_cond1)));
  assign io_mulres_payload = (cond0 ? MAX_VALUE : _zz_io_mulres_payload);
  assign io_mulres_valid = resAccReg_lastOrFlushReg;
  always @(posedge clk) begin
    if(io_din_ctrl_valid) begin
      dinPayloadReg_data <= io_din_payload_data;
      dinPayloadReg_weight <= io_din_payload_weight;
    end
    dinCtrlReg_valid <= io_din_ctrl_valid;
    dinCtrlReg_lastOrFlush <= io_din_ctrl_lastOrFlush;
    resAccReg_lastOrFlushReg <= dinCtrlReg_lastOrFlush;
    if(resAccReg_lastOrFlushReg) begin
      resAccReg_value <= _zz_resAccReg_value;
    end else begin
      if(dinCtrlReg_valid) begin
        resAccReg_value <= ($signed(_zz_resAccReg_value) + $signed(resAccReg_value));
      end
    end
  end


endmodule
