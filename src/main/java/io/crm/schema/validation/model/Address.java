package io.crm.schema.validation.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by someone on 10/09/2015.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Address {
    private String house;
    private String street;
    private String city;
    private String division;
    private String country;
    private String policeStation;
    private String postOffice;
    private String postCode;
}
