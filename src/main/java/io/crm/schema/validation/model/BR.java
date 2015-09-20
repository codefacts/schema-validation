package io.crm.schema.validation.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.Set;

/**
 * Created by someone on 10/09/2015.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class BR {
    private Long id;
    @NotEmpty
    private String name;
    @NotNull
    @Min(1)
    private Long distributionHouse;
    @NotNull
    @Min(1)
    private Long brSupervisor;
    @NotNull
    @Min(1)
    private Long location;
    private Long campaign;
}
