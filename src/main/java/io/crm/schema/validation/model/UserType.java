package io.crm.schema.validation.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Min;
import javax.validation.constraints.Size;

/**
 * Created by someone on 10/09/2015.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserType {
    private Long id;
    @NotEmpty
    private String prefix;
    @NotEmpty
    @Size(min = 3)
    private String name;
    @NotEmpty
    @Size(min = 3)
    private String label;
}
