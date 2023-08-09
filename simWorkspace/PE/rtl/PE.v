// Generator : SpinalHDL v1.9.0    git head : 7d30dbacbd3aa1be42fb2a3d4da5675703aae2ae
// Component : PE

`timescale 1ns/1ps

module PE (
  input               io_din_valid,
  input      [7:0]    io_din_payload_data,
  input      [7:0]    io_din_payload_weight,
  output              io_dout_valid,
  output     [7:0]    io_dout_payload_data,
  output     [7:0]    io_dout_payload_weight,
  output     [15:0]   io_mulres,
  input               clk,
  input               resetn
);

  wire       [17:0]   _zz_resAccReg_value;
  wire       [2:0]    _zz_cond0;
  wire       [2:0]    _zz_cond1;
  wire       [0:0]    _zz_cond1_1;
  wire       [15:0]   _zz_io_mulres;
  wire       [15:0]   _zz_io_mulres_1;
  reg        [7:0]    dinPayloadReg_data;
  reg        [7:0]    dinPayloadReg_weight;
  reg                 dinValidReg;
  wire       [15:0]   multiplyRes;
  reg        [17:0]   resAccReg_value;
  wire                resAccRegValue0;
  wire       [2:0]    resAccRegValue1;
  wire       [15:0]   MAX_VALUE;
  wire       [15:0]   MIN_VALUE;
  wire                cond0;
  wire                cond1;

  assign _zz_resAccReg_value = {{2{multiplyRes[15]}}, multiplyRes};
  assign _zz_cond0 = 3'b000;
  assign _zz_cond1_1 = 1'b1;
  assign _zz_cond1 = {{2{_zz_cond1_1[0]}}, _zz_cond1_1};
  assign _zz_io_mulres = (cond1 ? MIN_VALUE : _zz_io_mulres_1);
  assign _zz_io_mulres_1 = resAccReg_value[15:0];
  assign multiplyRes = ($signed(dinPayloadReg_data) * $signed(dinPayloadReg_weight));
  assign resAccRegValue0 = resAccReg_value[17];
  assign resAccRegValue1 = resAccReg_value[17 : 15];
  assign MAX_VALUE = 16'h7fff;
  assign MIN_VALUE = 16'h8000;
  assign cond0 = ((resAccRegValue0 == 1'b0) && ($signed(resAccRegValue1) != $signed(_zz_cond0)));
  assign cond1 = ((resAccRegValue0 == 1'b1) && ($signed(resAccRegValue1) != $signed(_zz_cond1)));
  assign io_mulres = (cond0 ? MAX_VALUE : _zz_io_mulres);
  assign io_dout_payload_data = dinPayloadReg_data;
  assign io_dout_payload_weight = dinPayloadReg_weight;
  assign io_dout_valid = dinValidReg;
  always @(posedge clk) begin
    if(io_din_valid) begin
      dinPayloadReg_data <= io_din_payload_data;
      dinPayloadReg_weight <= io_din_payload_weight;
    end
    dinValidReg <= io_din_valid;
  end

  always @(posedge clk) begin
    if(!resetn) begin
      resAccReg_value <= 18'h00000;
    end else begin
      if(dinValidReg) begin
        resAccReg_value <= ($signed(_zz_resAccReg_value) + $signed(resAccReg_value));
      end
    end
  end


endmodule
