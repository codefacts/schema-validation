package io.crm.schema.validation;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.ext.mongo.MongoClient;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.io.File;
import java.nio.file.Paths;

/**
 * Created by someone on 10/09/2015.
 */
public class App {
    private final Vertx vertx;
    private final EventBus bus;
    private final MongoClient mongoClient;
    private final LocalValidatorFactoryBean validator;
    private final ObjectMapper objectMapper;

    public App(Vertx vertx, MongoClient mongoClient, LocalValidatorFactoryBean validator, ObjectMapper objectMapper) {
        this.vertx = vertx;
        this.bus = vertx.eventBus();
        this.mongoClient = mongoClient;
        this.validator = validator;
        this.objectMapper = objectMapper;
    }

    public Vertx getVertx() {
        return vertx;
    }

    public EventBus getBus() {
        return bus;
    }

    public MongoClient getMongoClient() {
        return mongoClient;
    }

    public LocalValidatorFactoryBean getValidator() {
        return validator;
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public static void main(String... args) throws Exception {
        Vertx.vertx().deployVerticle(MainVerticle.class.getName());
        System.in.read();
    }
}
