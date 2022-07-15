package com.spirit.epsos.cc.adc;

import com.spirit.epsos.cc.adc.extractor.AutomaticDataCollector;
import eu.epsos.pt.eadc.util.EadcFactory;
import eu.europa.ec.sante.ehdsi.eadc.EADCException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * EadcReceiverImpl is instantiated from the EADCFactory as a singleton instance.
 */
public class EadcReceiverImpl implements EadcReceiver {

    private final Logger logger = LoggerFactory.getLogger(EadcReceiverImpl.class);
    private final AutomaticDataCollector automaticDataCollectorInstance = EadcFactory.INSTANCE.createAutomaticDataCollector();

    /**
     * This method is called from the NCP - called from multiple threads in parallel.
     */
    @Override
    public void process(EadcEntry entry) throws Exception {

        logger.debug("[EADC] Process entry start");
        if (entry == null) {
            throw new EADCException("EADCEntry == null");
        }
        if (entry.getData() == null) {
            throw new EADCException("EADCEntry.data xml == null");
        }
        if (StringUtils.isBlank(entry.getDsName())) {
            throw new EADCException("Null or Empty dsName");
        }
        automaticDataCollectorInstance.processTransaction(entry.getDsName(), entry.getData());

        logger.debug("[EADC] Process entry stop");
    }

    /**
     * This method is called from the NCP - called from multiple threads in parallel.
     */
    @Override
    public void processFailure(EadcEntry entry, String errorDescription) throws Exception {

        logger.debug("[EADC] Process Failure entry start");
        if (entry == null) {
            throw new EADCException("EADCEntry == null");
        }
        if (entry.getData() == null) {
            throw new EADCException("EADCEntry.data xml == null");
        }
        if (StringUtils.isBlank(entry.getDsName())) {
            throw new EADCException("Null or Empty dsName");
        }
        automaticDataCollectorInstance.processTransactionFailure(entry.getDsName(), entry.getData(), errorDescription);

        logger.debug("[EADC] Process Failure entry stop");
    }

}
