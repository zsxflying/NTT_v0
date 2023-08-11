// Verilated -*- C++ -*-
// DESCRIPTION: Verilator output: Design internal header
// See VPE.h for the primary calling header

#ifndef VERILATED_VPE___024ROOT_H_
#define VERILATED_VPE___024ROOT_H_  // guard

#include "verilated.h"


class VPE__Syms;

class alignas(VL_CACHE_LINE_BYTES) VPE___024root final : public VerilatedModule {
  public:

    // DESIGN SPECIFIC STATE
    VL_IN8(clk,0,0);
    VL_IN8(io_din_payload_data,7,0);
    VL_IN8(io_din_payload_weight,7,0);
    VL_IN8(io_din_ctrl_valid,0,0);
    VL_IN8(io_din_ctrl_lastOrFlush,0,0);
    VL_OUT8(io_dout_payload_data,7,0);
    VL_OUT8(io_dout_payload_weight,7,0);
    VL_OUT8(io_dout_ctrl_valid,0,0);
    VL_OUT8(io_dout_ctrl_lastOrFlush,0,0);
    VL_OUT8(io_mulres_valid,0,0);
    VL_IN8(resetn,0,0);
    CData/*7:0*/ PE__DOT__dinPayloadReg_data;
    CData/*7:0*/ PE__DOT__dinPayloadReg_weight;
    CData/*0:0*/ PE__DOT__dinCtrlReg_valid;
    CData/*0:0*/ PE__DOT__dinCtrlReg_lastOrFlush;
    CData/*0:0*/ PE__DOT__resAccReg_lastOrFlushReg;
    CData/*0:0*/ PE__DOT__resAccReg_validReg;
    CData/*0:0*/ __Vtrigprevexpr___TOP__clk__0;
    CData/*0:0*/ __VactDidInit;
    CData/*0:0*/ __VactContinue;
    VL_OUT16(io_mulres_payload,15,0);
    SData/*15:0*/ PE__DOT__multiplyRes;
    IData/*17:0*/ PE__DOT___zz_resAccReg_value;
    IData/*17:0*/ PE__DOT__resAccReg_value;
    IData/*31:0*/ __VstlIterCount;
    IData/*31:0*/ __VactIterCount;
    VlUnpacked<CData/*0:0*/, 2> __Vm_traceActivity;
    VlTriggerVec<1> __VstlTriggered;
    VlTriggerVec<1> __VactTriggered;
    VlTriggerVec<1> __VnbaTriggered;

    // INTERNAL VARIABLES
    VPE__Syms* const vlSymsp;

    // CONSTRUCTORS
    VPE___024root(VPE__Syms* symsp, const char* v__name);
    ~VPE___024root();
    VL_UNCOPYABLE(VPE___024root);

    // INTERNAL METHODS
    void __Vconfigure(bool first);
};


#endif  // guard
