package io.crm.schema.validation.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * Created by someone on 10/09/2015.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Contact {
    @NotNull
    private Long br;
    @NotNull
    private Consumer consumer;
    @NotEmpty
    private String brand;
    @NotNull
    private Boolean ptr;
    @NotNull
    private Boolean swp;
    @NotNull
    private Date date;
    private Date receiveDate;
    private Double latitude;
    private Double longitude;
    private Double accuracy;
    private String description;
}
