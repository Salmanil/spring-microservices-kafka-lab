package com.example.notification_service.model;

public class FailedKafkaMessage {

    private String key;
    private String payload;
    private String errorClass;
    private String errorMessage;
    private String originalTopic;
    private Integer originalPartition;
    private Long originalOffset;

    public FailedKafkaMessage() {
    }

    public FailedKafkaMessage(String key, String payload, String errorClass, String errorMessage, String originalTopic,
            Integer originalPartition, Long originalOffset) {
        this.key = key;
        this.payload = payload;
        this.errorClass = errorClass;
        this.errorMessage = errorMessage;
        this.originalTopic = originalTopic;
        this.originalPartition = originalPartition;
        this.originalOffset = originalOffset;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public String getErrorClass() {
        return errorClass;
    }

    public void setErrorClass(String errorClass) {
        this.errorClass = errorClass;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getOriginalTopic() {
        return originalTopic;
    }

    public void setOriginalTopic(String originalTopic) {
        this.originalTopic = originalTopic;
    }

    public Integer getOriginalPartition() {
        return originalPartition;
    }

    public void setOriginalPartition(Integer originalPartition) {
        this.originalPartition = originalPartition;
    }

    public Long getOriginalOffset() {
        return originalOffset;
    }

    public void setOriginalOffset(Long originalOffset) {
        this.originalOffset = originalOffset;
    }
}
