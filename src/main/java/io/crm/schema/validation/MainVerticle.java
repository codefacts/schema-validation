package io.crm.schema.validation;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

/**
 * Created by someone on 10/09/2015.
 */
public class MainVerticle extends AbstractVerticle {
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        startFuture.complete();
    }
}
