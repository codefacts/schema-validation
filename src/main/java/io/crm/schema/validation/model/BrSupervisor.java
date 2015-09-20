package io.crm.schema.validation.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Set;

/**
 * Created by someone on 10/09/2015.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class BrSupervisor {
    @NotNull
    @Size(min = 1)
    private Set<Long> distributionHouses;
    @NotNull
    @Min(1)
    private Long areaCoordinator;
    private Long campaign;
}
