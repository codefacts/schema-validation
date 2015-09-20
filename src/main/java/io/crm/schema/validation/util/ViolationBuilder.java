package io.crm.schema.validation.util;

public class ViolationBuilder {
    private Object invalidValue;
    private String message;
    private String messageTemplate;
    private String field;
    private String rootBeanClass;

    public ViolationBuilder setInvalidValue(Object invalidValue) {
        this.invalidValue = invalidValue;
        return this;
    }

    public ViolationBuilder setMessage(String message) {
        this.message = message;
        return this;
    }

    public ViolationBuilder setMessageTemplate(String messageTemplate) {
        this.messageTemplate = messageTemplate;
        return this;
    }

    public ViolationBuilder setField(String field) {
        this.field = field;
        return this;
    }

    public ViolationBuilder setRootBeanClass(String rootBeanClass) {
        this.rootBeanClass = rootBeanClass;
        return this;
    }

    public Violation build() {
        return new Violation(invalidValue, message, messageTemplate, field, rootBeanClass);
    }
}