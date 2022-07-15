package com.spirit.epsos.cc.adc;

public interface EadcReceiver {

    void process(EadcEntry entry) throws Exception;

    void processFailure(EadcEntry entry, String errorDescription) throws Exception;
}
