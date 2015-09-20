package io.crm.schema.validation.util;

import io.crm.QC;
import io.vertx.core.json.JsonObject;

/**
 * Created by someone on 17/09/2015.
 */
final public class Violation {
    private final Object invalidValue;
    private final String message;
    private final String messageTemplate;
    private final String field;
    private final String rootBeanClass;


    public Violation(Object invalidValue, String message, String messageTemplate, String field, String rootBeanClass) {
        this.invalidValue = invalidValue;
        this.message = message;
        this.messageTemplate = messageTemplate;
        this.field = field;
        this.rootBeanClass = rootBeanClass;
    }

    public JsonObject toJson() {
        return
                new JsonObject()
                        .put(QC.invalidValue, invalidValue)
                        .put(QC.message, message)
                        .put(QC.messageTemplate, messageTemplate)
                        .put(QC.field, field)
                        .put(QC.rootBeanClass, rootBeanClass)
                ;
    }
}
