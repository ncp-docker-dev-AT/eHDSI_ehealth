package eu.epsos.pt.eadc.helper;

import eu.epsos.pt.eadc.datamodel.ObjectFactory;
import eu.epsos.pt.eadc.datamodel.Transaction;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * This class gathers a set of utilities to manipulate eADC Transactions. For instance, you can convert a Transaction object
 * into a Document (DOM) or insert a CDA Document into a Transaction Document or object.
 *
 * @author Marcelo Fonseca<code> - marcelo.fonseca@iuz.pt</code>
 */
public class TransactionHelper {

    private static JAXBContext jaxbContext;

    static {
        try {
            jaxbContext = JAXBContext.newInstance("eu.epsos.pt.eadc.datamodel");
        } catch (JAXBException e) {
            throw new IllegalStateException(e);
        }
    }

    private TransactionHelper() {
    }

    /**
     * This method will insert a CDA document into a Transaction object, producing a Transaction Document.
     *
     * @param transaction the Transaction object
     * @param cda         the CDA document
     * @return a Document containing the Transaction Information combined with the CDA document
     * @throws JAXBException                thrown by the Transaction conversion process
     * @throws ParserConfigurationException thrown by the Transaction conversion
     *                                      process
     */
    public static Document insertCdaInTransaction(Transaction transaction, Document cda)
            throws JAXBException, ParserConfigurationException {
        return insertCdaInTransaction(convertTransaction(transaction), cda);
    }

    /**
     * This method will insert a CDA document into a Transaction document. It will add it as an extra node, at the same
     * level as Transaction Info element.
     *
     * @param transaction the Transaction document that will receive the CDA document
     * @param cda         the CDA document to be inserted
     * @return a Transaction object filled with a CDA document also
     */
    public static Document insertCdaInTransaction(Document transaction, final Document cda) {

        /* SET-UP */
        Node transactionRoot;

        /* BODY */
        transactionRoot = transaction.getDocumentElement();
        transactionRoot.appendChild(transaction.importNode(cda.getDocumentElement(), true));

        return transaction;
    }

    /**
     * Converts a given transaction object into a Document object (Marshal operation).
     *
     * @param transaction the transaction object to be converted
     * @return a Document object representing the transaction
     * @throws JAXBException                thrown by the conversion of the transaction object
     *                                      to the JAXB element
     * @throws ParserConfigurationException
     */
    public static Document convertTransaction(final Transaction transaction)
            throws JAXBException, ParserConfigurationException {

        /* SETUP */
        DocumentBuilderFactory dbf;
        Document result;
        Marshaller marshaller;
        JAXBElement<Transaction> transElem;

        /* BODY */
        dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        result = dbf.newDocumentBuilder().newDocument(); // Create document to hold Transaction

        transElem = (new ObjectFactory()).createTransaction(transaction); // Create Transaction element
        marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        marshaller.marshal(transElem, result); // Convert transaction element into resulting document

        return result;
    }
}
