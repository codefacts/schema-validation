package io.crm.schema.validation.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.Set;

/**
 * Created by someone on 10/09/2015.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Campaign {
    private Long _id;
    @NotEmpty
    @Size(min = 5)
    private String name;
    @NotNull
    @Min(1)
    private Long brand;
    @NotNull
    private Date salaryStartDate;
    @NotNull
    private Date salaryEndDate;
    @NotNull
    private Date launchDate;
    private Date closeDate;
    private Set<Region> tree;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Region {
        @NotNull
        @Min(1)
        private Long _id;
        private Set<Area> areas;

        public Long get_id() {
            return _id;
        }

        public void set_id(Long _id) {
            this._id = _id;
        }

        public Set<Area> getAreas() {
            return areas;
        }

        public void setAreas(Set<Area> areas) {
            this.areas = areas;
        }

        @Override
        public String toString() {
            return "Region{" +
                    "_id=" + _id +
                    ", areas=" + areas +
                    '}';
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Area {
        @NotNull
        @Min(1)
        private Long _id;
        private Set<AreaCoordinator> areaCoordinators;
        private Set<House> distributionHouses;

        public Long get_id() {
            return _id;
        }

        public void set_id(Long _id) {
            this._id = _id;
        }

        public Set<AreaCoordinator> getAreaCoordinators() {
            return areaCoordinators;
        }

        public void setAreaCoordinators(Set<AreaCoordinator> areaCoordinators) {
            this.areaCoordinators = areaCoordinators;
        }

        public Set<House> getDistributionHouses() {
            return distributionHouses;
        }

        public void setDistributionHouses(Set<House> distributionHouses) {
            this.distributionHouses = distributionHouses;
        }

        @Override
        public String toString() {
            return "Area{" +
                    "_id=" + _id +
                    ", areaCoordinators=" + areaCoordinators +
                    ", distributionHouses=" + distributionHouses +
                    '}';
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AreaCoordinator {
        @NotNull
        @Min(1)
        private Long _id;

        public Long get_id() {
            return _id;
        }

        public void set_id(Long _id) {
            this._id = _id;
        }

        @Override
        public String toString() {
            return "AreaCoordinator{" +
                    "_id=" + _id +
                    '}';
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class House {
        @NotNull
        @Min(1)
        private Long _id;
        private Set<BrSupervisor> brSupervisors;
        private Set<BR> brs;
        private Set<Location> locations;

        public Long get_id() {
            return _id;
        }

        public void set_id(Long _id) {
            this._id = _id;
        }

        public Set<BrSupervisor> getBrSupervisors() {
            return brSupervisors;
        }

        public void setBrSupervisors(Set<BrSupervisor> brSupervisors) {
            this.brSupervisors = brSupervisors;
        }

        public Set<BR> getBrs() {
            return brs;
        }

        public void setBrs(Set<BR> brs) {
            this.brs = brs;
        }

        public Set<Location> getLocations() {
            return locations;
        }

        public void setLocations(Set<Location> locations) {
            this.locations = locations;
        }

        @Override
        public String toString() {
            return "House{" +
                    "_id=" + _id +
                    ", brSupervisors=" + brSupervisors +
                    ", brs=" + brs +
                    ", locations=" + locations +
                    '}';
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BrSupervisor {
        @NotNull
        @Min(1)
        private Long _id;

        public Long get_id() {
            return _id;
        }

        public void set_id(Long _id) {
            this._id = _id;
        }

        @Override
        public String toString() {
            return "BrSupervisor{" +
                    "_id=" + _id +
                    '}';
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BR {
        @NotNull
        @Min(1)
        private Long _id;

        public Long get_id() {
            return _id;
        }

        public void set_id(Long _id) {
            this._id = _id;
        }

        @Override
        public String toString() {
            return "BR{" +
                    "_id=" + _id +
                    '}';
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Location {
        @NotNull
        @Min(1)
        private Long _id;

        public Long get_id() {
            return _id;
        }

        public void set_id(Long _id) {
            this._id = _id;
        }

        @Override
        public String toString() {
            return "Location{" +
                    "_id=" + _id +
                    '}';
        }
    }

    public Long get_id() {
        return _id;
    }

    public void set_id(Long _id) {
        this._id = _id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getBrand() {
        return brand;
    }

    public void setBrand(Long brand) {
        this.brand = brand;
    }

    public Date getSalaryStartDate() {
        return salaryStartDate;
    }

    public void setSalaryStartDate(Date salaryStartDate) {
        this.salaryStartDate = salaryStartDate;
    }

    public Date getSalaryEndDate() {
        return salaryEndDate;
    }

    public void setSalaryEndDate(Date salaryEndDate) {
        this.salaryEndDate = salaryEndDate;
    }

    public Date getLaunchDate() {
        return launchDate;
    }

    public void setLaunchDate(Date launchDate) {
        this.launchDate = launchDate;
    }

    public Date getCloseDate() {
        return closeDate;
    }

    public void setCloseDate(Date closeDate) {
        this.closeDate = closeDate;
    }

    public Set<Region> getTree() {
        return tree;
    }

    public void setTree(Set<Region> tree) {
        this.tree = tree;
    }
}
