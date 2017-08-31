package epsos.ccd.posam.tsam.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;


public class DebugUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(DebugUtils.class);

    private static final boolean transactionDebugging = true;
    private static final boolean verboseTransactionDebugging = true;

    private DebugUtils() {
    }

    public static void showTransactionStatus(String message) {
        LOGGER.info(((transactionActive()) ? "[+] " : "[-] ") + message);
    }

    // Some guidance from: http://java.dzone.com/articles/monitoring-declarative-transac?page=0,1
    public static boolean transactionActive() {
        try {
            ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
            Class tsmClass = contextClassLoader.loadClass("org.springframework.transaction.support.TransactionSynchronizationManager");
            return (Boolean) tsmClass.getMethod("isActualTransactionActive", null).invoke(null, null);

        } catch (ClassNotFoundException e) {
            LOGGER.error("ClassNotFoundException: '{}'", e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            LOGGER.error("IllegalArgumentException: '{}'", e.getMessage(), e);
        } catch (SecurityException e) {
            LOGGER.error("SecurityException: '{}'", e.getMessage(), e);
        } catch (IllegalAccessException e) {
            LOGGER.error("IllegalAccessException: '{}'", e.getMessage(), e);
        } catch (InvocationTargetException e) {
            LOGGER.error("InvocationTargetException: '{}'", e.getMessage(), e);
        } catch (NoSuchMethodException e) {
            LOGGER.error("NoSuchMethodException: '{}'", e.getMessage(), e);
        }

        // If we got here it means there was an exception
        throw new IllegalStateException("ServerUtils.transactionActive was unable to complete properly");
    }

    public static void transactionRequired(String message) {
        // Are we debugging transactions?
        if (!transactionDebugging) {
            // No, just return
            return;
        }

        // Are we doing verbose transaction debugging?
        if (verboseTransactionDebugging) {
            // Yes, show the status before we get to the possibility of throwing an exception
            showTransactionStatus(message);
        }

        // Is there a transaction active?
        if (!transactionActive()) {
            // No, throw an exception
            throw new IllegalStateException("Transaction required but not active [" + message + "]");
        }
    }
}
