package com.vero.coreprocessor.transactions.utilities;


import com.vero.coreprocessor.destinations.enums.*;
import com.vero.coreprocessor.destinations.model.dto.*;
import com.vero.coreprocessor.destinations.repository.*;
import jakarta.annotation.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProcessorFailoverManager {
    private static final String PROCESSOR_QUEUE_PREFIX = "processor_queue_";
    private static final String FAILURE_COUNT_PREFIX = "processor_failures_";
    private static final String RECOVERY_TIME_PREFIX = "processor_recovery_";
    private static final String INTERNATIONAL_PREFIX = "_international";
    private static final int FAILURE_THRESHOLD = 2;
    private static final long COOLDOWN_TIME_MS = 5 * 60 * 1000;
    private final InstitutionDestinationRepository institutionDestinationRepository;

    @Value("${processor-fail-over-list}")
    private String defaultProcessorList;

    @Value("${processor-fail-over-international-list}")
    private String defaultInternationalProcessorList;

    private final RedisTemplate<String, Object> redisTemplate;

    public ProcessorListWrapper getProcessorList(String institutionId, boolean isInternational) {
        String queueKey = getQueueKey(institutionId, isInternational);
        List<Object> cachedProcessors = redisTemplate.opsForList().range(queueKey, 0, -1);

        if (cachedProcessors != null && !cachedProcessors.isEmpty()) {
            return new ProcessorListWrapper(
                    cachedProcessors.stream().map(Object::toString).toList(),
                    false
            );
        }

        List<String> processors = fetchProcessorsFromDb(institutionId,isInternational);

        if (processors == null || processors.isEmpty()) {
            return new ProcessorListWrapper(
                    List.of(getDefaultProcessorList(isInternational).split(",")),
                    true
            );
        }

        redisTemplate.opsForList().rightPushAll(queueKey, processors.toArray(new String[0]));
        redisTemplate.expire(queueKey, 1, TimeUnit.DAYS);

        return new ProcessorListWrapper(processors, false);
    }

    public String getCurrentProcessor(String institutionId, String ruleEngineProcessor, boolean isInternational) {
        return !isProcessorFailing(institutionId, ruleEngineProcessor, isInternational)
                ? ruleEngineProcessor
                : failover(institutionId, ruleEngineProcessor, isInternational);
    }

    private boolean isProcessorFailing(String institutionId, String processor, boolean isInternational) {
        String failureKey = getFailureKey(institutionId, isInternational);
        Object failureCountObj = redisTemplate.opsForHash().get(failureKey, processor);

        if (failureCountObj == null) {
            failureKey = getFailureKey("default", isInternational);
            failureCountObj = redisTemplate.opsForHash().get(failureKey, processor);
        }

        int failureCount = failureCountObj != null ? Integer.parseInt(failureCountObj.toString()) : 0;
        return failureCount >= FAILURE_THRESHOLD;
    }

    private String failover(String institutionId, String failedProcessor, boolean isInternational) {
        synchronized (this) {
            ProcessorListWrapper wrapper = getProcessorList(institutionId, isInternational);
            List<String> processors = wrapper.getProcessors();

            //get  processor from default failover list
            if (!processors.contains(failedProcessor)) {
                return getFirstProcessorFromRedis(institutionId, isInternational);
            }

            String queueKey = getQueueKey(institutionId, isInternational);
            String recoveryKey = getRecoveryKey(institutionId, isInternational);

            redisTemplate.opsForList().remove(queueKey, 1, failedProcessor);
            redisTemplate.opsForList().rightPush(queueKey, failedProcessor);
            redisTemplate.opsForHash().put(recoveryKey, failedProcessor, String.valueOf(System.currentTimeMillis()));
            //return the new head
            return getFirstProcessorFromRedis(institutionId, isInternational);
        }
    }

    private String getFirstProcessorFromRedis(String institutionId, boolean isInternational) {
        List<?> resultList = redisTemplate.opsForList().range(getQueueKey(institutionId, isInternational), 0, 0);

        if (resultList == null || resultList.isEmpty()) {
            resultList = redisTemplate.opsForList().range(getQueueKey("default", isInternational), 0, 0);
        }

        return (resultList == null || resultList.isEmpty()) ? null : resultList.get(0).toString();
    }

    public void recordFailure(String institutionId, String processor, boolean isInternational) {
        ProcessorListWrapper wrapper = getProcessorList(institutionId, isInternational);
        String failureKey = getFailureKey(wrapper.isDefault() ? "default" : institutionId, isInternational);

        Long failureCount = redisTemplate.opsForHash().increment(failureKey, processor, 1);
        if (failureCount >= FAILURE_THRESHOLD) {
            failover(institutionId, processor, isInternational);
        }
    }

    private String getQueueKey(String institutionId, boolean isInternational) {
        return PROCESSOR_QUEUE_PREFIX + institutionId + (isInternational ? INTERNATIONAL_PREFIX : "");
    }

    private String getFailureKey(String institutionId, boolean isInternational) {
        return FAILURE_COUNT_PREFIX + institutionId + (isInternational ? INTERNATIONAL_PREFIX : "");
    }

    private String getRecoveryKey(String institutionId, boolean isInternational) {
        return RECOVERY_TIME_PREFIX + institutionId + (isInternational ? INTERNATIONAL_PREFIX : "");
    }

    private String getDefaultProcessorList(boolean isInternational) {
        return isInternational ? defaultInternationalProcessorList : defaultProcessorList;
    }

    @PostConstruct
    public void initializeDefaultProcessorList() {
        initializeProcessorList(false);
        initializeProcessorList(true);
    }

    private void initializeProcessorList(boolean isInternational) {
        String queueKey = getQueueKey("default", isInternational);
        List<Object> cachedProcessors = redisTemplate.opsForList().range(queueKey, 0, -1);

        if (cachedProcessors == null || cachedProcessors.isEmpty()) {
            List<String> defaultProcessors = List.of(getDefaultProcessorList(isInternational).split(","));
            redisTemplate.opsForList().rightPushAll(queueKey, defaultProcessors.toArray(new String[0]));
            redisTemplate.expire(queueKey, 1, TimeUnit.DAYS);
        }
    }

    private List<String> fetchProcessorsFromDb(String institutionId, boolean isInternational) {
        return institutionDestinationRepository
                .findByInstitutionId(institutionId)
                .stream()
                .filter(d -> {
                    DestinationDomain domain = d.getDestination().getDomain();
                    return domain == DestinationDomain.HYBRID ||
                            ((isInternational && domain == DestinationDomain.INTERNATIONAL)
                                    || (!isInternational && domain == DestinationDomain.LOCAL));
                })
                .map(d -> d.getDestination().getDestinationName())
                .toList();
    }



    public void attemptRecoveryForAllInstitutions() {
        synchronized (this) {
            List<String> institutionIds = fetchInstitutionsWithProcessors();
            long now = System.currentTimeMillis();

            institutionIds.parallelStream().forEach(institutionId -> processInstitutionRecovery(institutionId, now));

            processInstitutionRecovery("default", now);
        }
    }

    private void processRecoveryForInstitution(String institutionId, long currentTime, boolean isInternational) {
        // Fetch processors for the institution (local or international)
        ProcessorListWrapper processorWrapper = getProcessorList(institutionId, isInternational);
        List<String> processors = processorWrapper.getProcessors();

        // Retrieve the last failure recovery time from Redis for each processor
        processors.parallelStream()
                .map(processor -> new AbstractMap.SimpleEntry<>(
                        processor,
                        redisTemplate.opsForHash().get(getRecoveryKey(institutionId, isInternational), processor)
                ))
                .filter(entry -> entry.getValue() != null)  // Remove processors without recovery timestamps
                .map(entry -> new AbstractMap.SimpleEntry<>(entry.getKey(), Long.parseLong(entry.getValue().toString())))
                .filter(entry -> (currentTime - entry.getValue()) > COOLDOWN_TIME_MS)  // Check cooldown expiry
                .forEach(entry -> recoverProcessor(institutionId, entry.getKey(), processorWrapper.isDefault()));
    }



    private void processInstitutionRecovery(String institutionId, long now) {
        processRecoveryForInstitution(institutionId, now, false);
        processRecoveryForInstitution(institutionId, now, true);
        processRecoveryForInstitution("default", now, false);
        processRecoveryForInstitution("default", now, true);
    }

    private List<String> fetchInstitutionsWithProcessors() {
        Set<String> keys = redisTemplate.keys(PROCESSOR_QUEUE_PREFIX + "*");
        assert keys != null;

        return keys.stream()
                .map(key -> key.replace(PROCESSOR_QUEUE_PREFIX, "")
                        .replace(INTERNATIONAL_PREFIX, ""))
                .distinct()
                .collect(Collectors.toList());
    }



    private void recoverProcessor(String institutionId, String processor, boolean isDefault) {
        String queueKey = isDefault ? PROCESSOR_QUEUE_PREFIX + "default" : PROCESSOR_QUEUE_PREFIX + institutionId;
        String recoveryKey = isDefault ? RECOVERY_TIME_PREFIX + "default" : RECOVERY_TIME_PREFIX + institutionId;
        String failureKey = isDefault ? FAILURE_COUNT_PREFIX + "default" : FAILURE_COUNT_PREFIX + institutionId;

        redisTemplate.opsForList().remove(queueKey, 1, processor);
        redisTemplate.opsForList().leftPush(queueKey, processor);
        redisTemplate.opsForHash().delete(recoveryKey, processor);
        redisTemplate.opsForHash().put(failureKey, processor, "0");
    }

    @PreDestroy
    public void clearAllProcessorCaches() {
        log.info("Clearing all processor-related Redis keys on shutdown...");

        Set<String> keys = redisTemplate.keys(PROCESSOR_QUEUE_PREFIX + "*");
        if (keys != null) redisTemplate.delete(keys);

        keys = redisTemplate.keys(FAILURE_COUNT_PREFIX + "*");
        if (keys != null) redisTemplate.delete(keys);

        keys = redisTemplate.keys(RECOVERY_TIME_PREFIX + "*");
        if (keys != null) redisTemplate.delete(keys);

        log.info("Processor Redis keys cleared.");
    }

}
