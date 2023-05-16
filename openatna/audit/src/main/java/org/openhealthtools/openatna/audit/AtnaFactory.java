package org.openhealthtools.openatna.audit;

import org.openhealthtools.openatna.audit.persistence.dao.*;
import org.openhealthtools.openatna.audit.service.AuditService;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class AtnaFactory {

    private static AtnaFactory instance;
    private static String openatnaProperties = null;
    private final ApplicationContext context;

    private AtnaFactory(final ApplicationContext context) {
        this.context = context;
    }

    /**
     * If called before any bean getter methods, this allows the factory to be initialized by an arbitrary Application Context.
     * Of course, this context must contain the beans defined by OpenATNA.
     *
     * @param context
     */
    public static synchronized void initialize(ApplicationContext context) {
        if (context == null) {
            try {
                context = new ClassPathXmlApplicationContext("classpath*:/*Context.xml");
            } catch (BeansException e) {
                throw new RuntimeException("FATAL: Could not create Spring Application Context.", e);
            }
        }
        instance = new AtnaFactory(context);
    }

    public static synchronized Object getBean(final String id) {
        if (instance == null) {
            initialize(null);
        }
        return instance.getComponent(id);
    }

    public static String getPropertiesLocation() {
        return openatnaProperties;
    }

    public static void setPropertiesLocation(String location) {
        openatnaProperties = location;
    }

    public static CodeDao codeDao() {
        return (CodeDao) getBean("codeDao");
    }

    public static ParticipantDao participantDao() {
        return (ParticipantDao) getBean("participantDao");
    }

    public static NetworkAccessPointDao networkAccessPointDao() {
        return (NetworkAccessPointDao) getBean("networkAccessPointDao");
    }

    public static MessageDao messageDao() {
        return (MessageDao) getBean("messageDao");
    }

    public static SourceDao sourceDao() {
        return (SourceDao) getBean("sourceDao");
    }

    public static ObjectDao objectDao() {
        return (ObjectDao) getBean("objectDao");
    }

    public static EntityDao entityDao() {
        return (EntityDao) getBean("entityDao");
    }

    public static AuditService auditService() {
        return (AuditService) getBean("auditService");
    }

    public static ErrorDao errorDao() {
        return (ErrorDao) getBean("errorDao");
    }

    public static ProvisionalDao provisionalDao() {
        return (ProvisionalDao) getBean("provisionalDao");
    }

    public static QueryDao queryDao() {
        return (QueryDao) getBean("queryDao");
    }

    private Object getComponent(final String value) {
        return context.getBean(value);
    }
}
