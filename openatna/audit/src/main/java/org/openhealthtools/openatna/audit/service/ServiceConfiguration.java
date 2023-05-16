package org.openhealthtools.openatna.audit.service;

import org.openhealthtools.openatna.audit.persistence.PersistencePolicies;
import org.openhealthtools.openatna.audit.process.ProcessorChain;
import org.openhealthtools.openatna.syslog.LogMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class ServiceConfiguration {

    private final Logger logger = LoggerFactory.getLogger(ServiceConfiguration.class);

    private PersistencePolicies persistencePolicies = new PersistencePolicies();
    private Class<? extends LogMessage> logMessageClass;
    private List<String> preVerifyProcessors = new ArrayList<>();
    private List<String> postVerifyProcessors = new ArrayList<>();
    private List<String> postPersistProcessors = new ArrayList<>();
    private boolean validationProcessor = true;
    private Set<String> codeUrls = new HashSet<>();


    public PersistencePolicies getPersistencePolicies() {
        return persistencePolicies;
    }

    public void setPersistencePolicies(PersistencePolicies persistencePolicies) {
        this.persistencePolicies = persistencePolicies;
    }

    public Class<? extends LogMessage> getLogMessageClass() {
        return logMessageClass;
    }

    public void setLogMessageClass(Class<? extends LogMessage> logMessageClass) {
        this.logMessageClass = logMessageClass;
    }

    public Map<ProcessorChain.PHASE, List<String>> getProcessors() {
        Map<ProcessorChain.PHASE, List<String>> map = new HashMap<>();
        map.put(ProcessorChain.PHASE.PRE_VERIFY, preVerifyProcessors);
        map.put(ProcessorChain.PHASE.POST_VERIFY, postVerifyProcessors);
        map.put(ProcessorChain.PHASE.POST_PERSIST, postPersistProcessors);
        return map;
    }

    public void addPreVerifyProcessor(String processor) {
        preVerifyProcessors.add(processor);
    }

    public void addPostVerifyProcessor(String processor) {
        postVerifyProcessors.add(processor);
    }

    public void addPostPersistProcessor(String processor) {
        postPersistProcessors.add(processor);
    }

    public List<String> getPreVerifyProcessors() {
        return preVerifyProcessors;
    }

    public void setPreVerifyProcessors(List<String> preVerifyProcessors) {
        this.preVerifyProcessors = preVerifyProcessors;
    }

    public List<String> getPostVerifyProcessors() {
        return postVerifyProcessors;
    }

    public void setPostVerifyProcessors(List<String> postVerifyProcessors) {
        this.postVerifyProcessors = postVerifyProcessors;
    }

    public List<String> getPostPersistProcessors() {
        return postPersistProcessors;
    }

    public void setPostPersistProcessors(List<String> postPersistProcessors) {
        this.postPersistProcessors = postPersistProcessors;
    }

    public void addProcessor(String processor, ProcessorChain.PHASE phase) {
        switch (phase) {
            case PRE_VERIFY:
                addPreVerifyProcessor(processor);
                break;
            case POST_VERIFY:
                addPostVerifyProcessor(processor);
                break;
            case POST_PERSIST:
                addPostPersistProcessor(processor);
                break;
            default:
                break;
        }
    }

    public boolean isValidationProcessor() {
        return validationProcessor;
    }

    public void setValidationProcessor(boolean validationProcessor) {
        this.validationProcessor = validationProcessor;
    }

    public void addCodeUrl(String url) {
        codeUrls.add(url);
    }

    public Set<String> getCodeUrls() {
        return codeUrls;
    }

    public void setCodeUrls(Set<String> codeUrls) {
        this.codeUrls = codeUrls;
    }
}
