package io.crm.schema.validation.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import javax.validation.constraints.Size;
import java.util.Date;

/**
 * Created by someone on 10/09/2015.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class User {
    private Long id;
    @NotNull
    @Size(min = 3)
    private String name;
    @NotNull
    @Past
    private Date dateOfBirth;
    @Email
    private String mail;
    @NotEmpty
    private String mobile;
    @NotNull
    @Past
    private Date joinDate;
    @Past
    private Date resignDate;

    private String occupation;
    private String designation;
    private String username;
    @NotEmpty
    @Size(min = 3)
    private String password;
    @NotNull
    private Long userType;

    private String userId;
    private Address address;
}
