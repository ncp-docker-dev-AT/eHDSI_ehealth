package eu.epsos.pt.eadc.util;

import com.spirit.epsos.cc.adc.EadcEntry;
import com.spirit.epsos.cc.adc.EadcEntryImpl;
import com.spirit.epsos.cc.adc.EadcReceiver;
import com.spirit.epsos.cc.adc.EadcReceiverImpl;
import com.spirit.epsos.cc.adc.db.EadcDbConnect;
import com.spirit.epsos.cc.adc.db.EadcDbConnectImpl;
import com.spirit.epsos.cc.adc.extractor.AutomaticDataCollector;
import com.spirit.epsos.cc.adc.extractor.AutomaticDataCollectorImpl;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import javax.naming.NamingException;
import java.sql.SQLException;

public enum EadcFactory {

    INSTANCE;

    private static final Logger LOGGER = LoggerFactory.getLogger(EadcFactory.class);
    private EadcReceiver receiver = null;
    private EadcEntry entry = null;

    public EadcDbConnect createEadcDbConnect(String dsName) throws NamingException, SQLException {
        return new EadcDbConnectImpl(dsName);
    }

    public AutomaticDataCollector createAutomaticDataCollector() {
        return AutomaticDataCollectorImpl.getInstance();
    }

    /**
     * Returns the EadcReceiver used for processing the EadcEntry
     */
    public EadcReceiver getReceiver() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        return initReceiver(EadcReceiverImpl.class.getName());
    }

    /**
     * Returns the EadcReceiver used for processing the EadcEntry - this method is called by the NCP implementation -
     * if no receiver impl is configured at the NCP the default EadcReceiverImpl is used - only one receiver impl
     * can be used in one classPath!
     */
    public EadcReceiver getReceiver(String implClass) throws InstantiationException, IllegalAccessException,
            ClassNotFoundException {
        return initReceiver(implClass);
    }

    /**
     * Returns the default EadcEntry
     */
    public EadcEntry getEntry(String dsName, Document data, Document soapRqData, Document soapRspData)
            throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        return initEntry(EadcEntryImpl.class.getName(), dsName, data, soapRqData, soapRspData);
    }

    /**
     * Returns the EadcEntry used for processing from the NCP - this method is called by the NCP implementation -
     * if no entry impl is configured at the NCP the default EadcEntryImpl is used.
     */
    public EadcEntry getEntry(String implClass, String dsName, Document data, Document soapRqData, Document soapRspData)
            throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        return initEntry(implClass, dsName, data, soapRqData, soapRspData);
    }

    /**
     * Initializes the EadcReceiver - using a different EadcReceiverImpl would enable you to add additional
     * functionality during the data collection from the EadcEntry to the Database tables.
     */
    private synchronized EadcReceiver initReceiver(String implClass) throws InstantiationException, IllegalAccessException,
            ClassNotFoundException {

        if (receiver == null) {
            LOGGER.debug("initReceiver - instantiate new : '{}'", implClass);
            receiver = (EadcReceiver) Class.forName(implClass).newInstance();
        } else {
            LOGGER.trace("initReceiver - use old singleton instance: '{}'", implClass);
        }
        if (!StringUtils.equals(implClass, receiver.getClass().getName())) {
            throw new IllegalArgumentException("singleton / implClass conflict - receivedImplClass :" + implClass + ", actUsedSingleton :" + receiver.getClass().getName());
        }

        return receiver;
    }

    /**
     * Initializes the EadcEntry - using a different EadcEntryImpl would enable you to extract additional data
     * from the soap request or response data to the data xml before the entry is processed by the EadcReceiverImpl -
     * the default EadcReceiverImpl is only using the data xml and not the soap request and response data
     */
    private synchronized EadcEntry initEntry(String implClass, String dsName, Document data, Document soapRqData,
                                             Document soapRspData)
            throws InstantiationException, IllegalAccessException, ClassNotFoundException {

        if (entry == null) {
            LOGGER.debug("initEntry - instantiate new: '{}'", implClass);
            entry = (EadcEntry) Class.forName(implClass).newInstance();

        } else {
            LOGGER.trace("initEntry - use old singleton instance: '{}'", implClass);
        }
        if (!StringUtils.equals(implClass, entry.getClass().getName())) {
            throw new IllegalArgumentException("singleton / implClass conflict - receivedImplClass :" + implClass + ", actUsedSingleton :" + entry.getClass().getName());
        }

        return entry.newInstance(dsName, data, soapRqData, soapRspData);
    }

    /**
     * Called if the NCP is reloaded
     */
    public void clear() {

        synchronized (INSTANCE) {
            receiver = null;
            entry = null;
        }
    }
}
