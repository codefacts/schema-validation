package io.crm.schema.validation.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Created by someone on 10/09/2015.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Consumer {
    @NotEmpty
    @Size(min = 2)
    private String name;
    @NotEmpty
    private String mobile;
    @NotEmpty
    private String occupation;
    @NotNull
    @Min(18)
    private Integer age;

    private String fatherName;
}
