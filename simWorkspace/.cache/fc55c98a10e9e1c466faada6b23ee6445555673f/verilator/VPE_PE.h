// Verilated -*- C++ -*-
// DESCRIPTION: Verilator output: Design internal header
// See VPE.h for the primary calling header

#ifndef VERILATED_VPE_PE_H_
#define VERILATED_VPE_PE_H_  // guard

#include "verilated.h"


class VPE__Syms;

class alignas(VL_CACHE_LINE_BYTES) VPE_PE final : public VerilatedModule {
  public:

    // DESIGN SPECIFIC STATE
    VL_IN8(clk,0,0);
    VL_IN8(io_din_valid,0,0);
    VL_IN8(io_din_payload_data,7,0);
    VL_IN8(io_din_payload_weight,7,0);
    VL_OUT8(io_dout_valid,0,0);
    VL_OUT8(io_dout_payload_data,7,0);
    VL_OUT8(io_dout_payload_weight,7,0);
    VL_IN8(resetn,0,0);
    CData/*0:0*/ __PVT__inReg_valid;
    CData/*7:0*/ __PVT__inReg_payload_data;
    CData/*7:0*/ __PVT__inReg_payload_weight;
    VL_OUT16(io_mulres,15,0);
    SData/*15:0*/ __PVT__multiplyRes;
    IData/*17:0*/ resAccReg_value;

    // INTERNAL VARIABLES
    VPE__Syms* const vlSymsp;

    // CONSTRUCTORS
    VPE_PE(VPE__Syms* symsp, const char* v__name);
    ~VPE_PE();
    VL_UNCOPYABLE(VPE_PE);

    // INTERNAL METHODS
    void __Vconfigure(bool first);
};


#endif  // guard
