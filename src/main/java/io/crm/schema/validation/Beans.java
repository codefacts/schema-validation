package io.crm.schema.validation;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.crm.util.Util;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

/**
 * Created by someone on 10/09/2015.
 */
@Configuration
public class Beans {
    @Bean
    public LocalValidatorFactoryBean validator() {
        return new LocalValidatorFactoryBean();
    }

    @Bean
    public ObjectMapper objectMapper() {
        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setDateFormat(Util.mongoDateFormat());
        return objectMapper;
    }
}
