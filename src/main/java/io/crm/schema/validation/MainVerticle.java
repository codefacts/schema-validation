package io.crm.schema.validation;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.crm.Events;
import io.crm.QC;
import io.crm.intfs.ConsumerInterface;
import io.crm.schema.validation.service.ValidatorService;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static io.crm.util.ExceptionUtil.fail;

/**
 * Created by someone on 10/09/2015.
 */
public class MainVerticle extends AbstractVerticle {
    private final ConfigurableApplicationContext context = new AnnotationConfigApplicationContext(Beans.class);

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        final LocalValidatorFactoryBean validator = context.getBean(LocalValidatorFactoryBean.class);
        final ObjectMapper objectMapper = context.getBean(ObjectMapper.class);
        final MongoClient mongoClient = MongoClient.createShared(vertx,
                new JsonObject()
                        .put(QC.db_name, "phase_0"));
        final App app = new App(vertx, mongoClient, validator, objectMapper);
        registerValidators(app);

        startFuture.complete();
    }

    private void registerValidators(final App app) {
        final ValidatorService validatorService = new ValidatorService(app);

        vertx.eventBus().consumer(Events.VALIDATE_AREA, consumer(validatorService::validateArea));
        vertx.eventBus().consumer(Events.VALIDATE_CAMPAIGN, consumer(validatorService::validateCampaign));
    }

    @Override
    public void stop(Future<Void> stopFuture) throws Exception {
        context.close();
        stopFuture.complete();
    }

    private Handler<Message<Object>> consumer(final ConsumerInterface<Message> consumer) {
        return message -> {
            try {
                consumer.accept(message);
            } catch (final Exception e) {
                fail(message, e);
                throw new RuntimeException(e);
            }
        };
    }
}
