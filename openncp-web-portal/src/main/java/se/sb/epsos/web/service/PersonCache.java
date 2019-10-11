package se.sb.epsos.web.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sb.epsos.web.model.Person;

import java.util.HashMap;
import java.util.Iterator;

/**
 * @author andreas
 */
public class PersonCache {

    private static PersonCache instance;
    public final Logger logger = LoggerFactory.getLogger(PersonCache.class);
    private HashMap<PersonCacheKey, Person> cache;

    private PersonCache() {
        this.cache = new HashMap<>();
    }

    public static PersonCache getInstance() {
        if (instance == null) {
            instance = new PersonCache();
        }
        return instance;
    }

    public boolean contains(PersonCacheKey key) {

        if (logger.isDebugEnabled()) {
            logger.debug(printCompleteCache());
        }
        boolean found = this.cache.containsKey(key);
        if (logger.isDebugEnabled()) {
            logger.debug("Checking cached person by key: '{}', person is '{}'", key.toString(), found ? "cached" : "not cached");
        }
        return found;
    }

    public Person get(PersonCacheKey key) {

        if (logger.isDebugEnabled()) {
            logger.debug(printCompleteCache());
        }
        Person person = this.cache.get(key);
        if (logger.isDebugEnabled()) {
            logger.debug("Retrieveing person with key: '{}', person was '{}' in cache", key.toString(), (person != null ? "found" : "not found"));
        }
        return person;
    }

    public void put(PersonCacheKey key, Person person) {
        if (logger.isDebugEnabled()) {
            logger.debug("Caching person with key: '{}'", key.toString());
        }
        this.cache.put(key, person);
        if (logger.isDebugEnabled()) {
            logger.debug(printCompleteCache());
        }
    }

    public void flush() {

        logger.debug("Flushing entire cache");
        this.cache.clear();
        if (logger.isDebugEnabled()) {
            logger.debug(printCompleteCache());
        }
    }

    public void flush(MetaDocument doc) {

        if (logger.isDebugEnabled()) {
            logger.debug("Flushing person with key: '{}' by MetaDocument", doc.getDtoCacheKey().toString());
        }
        this.cache.remove(doc.getDtoCacheKey());
        if (logger.isDebugEnabled()) {
            logger.debug(printCompleteCache());
        }
    }

    public void flush(PersonCacheKey key) {

        if (logger.isDebugEnabled()) {
            logger.debug("Flushing person with key: '{}' by PersonCacheKey", key.toString());
        }
        this.cache.remove(key);
        if (logger.isDebugEnabled()) {
            logger.debug(printCompleteCache());
        }
    }

    public void flush(String sessionId) {

        if (logger.isDebugEnabled()) {
            logger.debug("Flushing all persons for sessionId: '{}'", sessionId);
        }
        if (sessionId != null) {
            Iterator<PersonCacheKey> it = this.cache.keySet().iterator();
            while (it.hasNext()) {
                PersonCacheKey key = it.next();
                if (key.getSessionId() == null || key.getSessionId().equals(sessionId)) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Flushing person with key: '{}' by SessionID", key.toString());
                    }
                    it.remove();
                }
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug(printCompleteCache());
        }
    }

    private String printCompleteCache() {

        StringBuilder builder = new StringBuilder("\n****\tDocummentCache current status:\n");
        int i = 1;
        for (PersonCacheKey key : this.cache.keySet()) {
            builder.append("\t").append(i).append(" : ").append(key.toString()).append("\n");
            i++;
        }
        builder.append("****\t total size: ").append(this.cache.size()).append("\n");
        return builder.toString();
    }
}
