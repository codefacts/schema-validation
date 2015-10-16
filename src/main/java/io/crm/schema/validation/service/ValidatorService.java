package io.crm.schema.validation.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.crm.FailureCode;
import io.crm.Operation;
import io.crm.QC;
import io.crm.intfs.BiConsumerUnchecked;
import io.crm.intfs.ConsumerUnchecked;
import io.crm.mc;
import io.crm.model.EmployeeType;
import io.crm.model.User;
import io.crm.schema.validation.App;
import io.crm.schema.validation.model.Area;
import io.crm.schema.validation.model.Campaign;
import io.crm.util.*;
import io.vertx.core.MultiMap;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import javax.validation.ConstraintViolation;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import static io.crm.util.ExceptionUtil.withReply;
import static io.crm.util.Util.EMPTY_JSON_ARRAY;
import static io.crm.util.Util.parseMongoDate;
import static io.crm.util.Util.toJsonObject;

/**
 * Created by someone on 14/09/2015.
 */
final public class ValidatorService {
    private final LocalValidatorFactoryBean validator;
    private final ObjectMapper objectMapper;
    private final MongoClient mongoClient;

    public ValidatorService(final App app) {
        validator = app.getValidator();
        objectMapper = app.getObjectMapper();
        mongoClient = app.getMongoClient();
    }

    public ValidatorService(final LocalValidatorFactoryBean validator, final ObjectMapper objectMapper, MongoClient mongoClient) {
        this.validator = validator;
        this.objectMapper = objectMapper;
        this.mongoClient = mongoClient;
    }

    public void validateArea(final Message<String> message) throws Exception {
        final Area area = parseJson(message.body(), Area.class);
        final Set<ConstraintViolation<Area>> violations = validator.validate(area);
        message.reply(toJson(violations));
    }

    public void validateCampaign(final Message<JsonObject> message) throws Exception {
        final MultiMap headers = message.headers();
        if (!headers.contains(QC.operation)) headers.set(QC.operation, Operation.create.name());

        final Operation operation = Operation.valueOf(headers.get(QC.operation));
        final JsonObject campaignJson = message.body();
        final ErrorBuilder errorBuilder = new ErrorBuilder();

        final String launchDate = dateString(campaignJson.getValue(io.crm.model.Campaign.launchDate));
        final String closeDate = dateString(campaignJson.getValue(io.crm.model.Campaign.closeDate));
        final String salaryStartDate = dateString(campaignJson.getValue(io.crm.model.Campaign.salaryStartDate));
        final String salaryEndDate = dateString(campaignJson.getValue(io.crm.model.Campaign.salaryEndDate));

        validateDateFormat(errorBuilder, launchDate, "launchDate", "Launch Date is invalid.");
        validateDateFormat(errorBuilder, closeDate, "closeDate", "Close Date is invalid.");
        validateDateFormat(errorBuilder, salaryStartDate, "salaryStartDate", "Salary Start Date is invalid.");
        validateDateFormat(errorBuilder, salaryEndDate, "salaryEndDate", "Salary End Date is invalid.");

        campaignJson
                .put(QC.launchDate, launchDate)
                .put(QC.closeDate, closeDate)
                .put(QC.salaryStartDate, salaryStartDate)
                .put(QC.salaryEndDate, salaryEndDate)
        ;

        final Campaign campaign = parseJson(campaignJson.encode(), Campaign.class);
        final Set<ConstraintViolation<Campaign>> violations = validator.validate(campaign);
        errorBuilder.putAll(serialize(violations).getList());

        switch (operation) {
            case create:
                ensureIdExists(mc.campaigns, campaign.get_id(), exists -> {
                    if (!exists) {
                        errorBuilder.put(QC.id, "Campaign Id is required.");
                    }
                    ensureNameNotExists(mc.campaigns, campaign.getName(), nameNotExists -> {
                        if (!nameNotExists) {
                            errorBuilder.put(QC.name, "Name already exists. Name must be unique. Please choose a different name.");
                        }
                        campaignValidate(campaign, campaignJson, errorBuilder, message);
                    }, message);
                }, message);
                break;
            case update:
                ensureIdExistsAndNameModified(mc.campaigns, campaign.get_id(), campaign.getName(), (idExists, nameModified) -> {
                    if (!idExists) {
                        errorBuilder.put(QC.id, "Campaign Id is required.");
                    }
                    if (nameModified) {
                        ensureNameNotExists(mc.campaigns, campaign.getName(), nameNotExists -> {
                            if (!nameNotExists) {
                                errorBuilder.put(QC.name, "Name already exists. Name must be unique. Please choose a different name.");
                            }
                            campaignValidate(campaign, campaignJson, errorBuilder, message);
                        }, message);
                    }
                    campaignValidate(campaign, campaignJson, errorBuilder, message);
                }, message);
                break;
            default:
                message.fail(FailureCode.BadRequest.code, "Operation type for validation is missing.");
        }
    }

    private <T> void campaignValidate(Campaign campaign, final JsonObject campaignJson, final ErrorBuilder errorBuilder, final Message message) {

        ensureBrandExists(campaign.getBrand(), brandExists -> {
            if (!brandExists) {
                errorBuilder.put(QC.brandId, "Brand Id does not exists. Please specify a valid brand ID.");
            }

            validateTree(
                    campaignJson
                            .getJsonArray(
                                    QC.tree, EMPTY_JSON_ARRAY),
                    errorBuilder,
                    (tree, eb) -> {

                        message.reply(
                                new JsonObject()
                                        .put(QC.data, campaignJson)
                                        .put(QC.violations, errorBuilder.build()));
                    }, message);
        }, message);
    }

    private void validateTree(final JsonArray tree, final ErrorBuilder errorBuilder, final BiConsumer<JsonArray, ErrorBuilder> biConsumer, final Message message) {
        final List<JsonObject> regionList = tree.stream().map(v -> toJsonObject(v)).collect(Collectors.toList());

        final TaskCoordinator taskCoordinator = new TaskCoordinatorBuilder()
                .count(7)
                .onSuccess(() -> {
                    biConsumer.accept(tree, errorBuilder);
                })
                .message(message)
                .get();

        mongoClient.find(mc.regions.name(),
                new JsonObject()
                        .put(QC.id, new JsonObject()
                                .put(QC.$in, regionList.stream().map(v -> v.getLong(QC.id)).collect(Collectors.toList()))),
                taskCoordinator.add(rList -> {

                    if (rList.size() < regionList.size()) {
                        final Set<Long> rListIdSet = idSet(rList);
                        putInErrorBuilder(errorBuilder, regionList, rListIdSet, QC.regionId, "Regions");
                        taskCoordinator.finish();
                        return;
                    }

                    final Map<Long, JsonObject> areaList = collect(regionList, mc.areas.name(), QC.region);

                    mongoClient.find(mc.areas.name(),
                            new JsonObject()
                                    .put(QC.id, new JsonObject()
                                            .put(QC.$in, new JsonArray(areaList.values().stream().map(v -> v.getLong(QC.id)).collect(Collectors.toList())))),
                            taskCoordinator.add(aList -> {
                                if (aList.size() < areaList.size()) {
                                    final Set<Long> aListIdSet = idSet(aList);
                                    putInErrorBuilder(errorBuilder, areaList.values(), aListIdSet, QC.areaId, "Areas");
                                    taskCoordinator.finish();
                                    return;
                                }

                                checkParents(aList, areaList, errorBuilder, QC.region, QC.area, QC.areaRegionId);

                                Map<Long, JsonObject> houseList = collect(areaList.values(), mc.distributionHouses.name(), QC.area);

                                mongoClient.find(mc.distributionHouses.name(),
                                        new JsonObject()
                                                .put(QC.id, new JsonObject()
                                                        .put(QC.$in, new JsonArray(houseList.values().stream().map(v -> v.getLong(QC.id)).collect(Collectors.toList())))),
                                        taskCoordinator.add(hList -> {

                                            if (hList.size() < houseList.size()) {
                                                final Set<Long> hListIdSet = idSet(hList);
                                                putInErrorBuilder(errorBuilder, houseList.values(), hListIdSet, QC.houseId, "Houses");
                                                taskCoordinator.countdown(3);
                                                return;
                                            }

                                            checkParents(hList, houseList, errorBuilder, QC.area, QC.distributionHouse, QC.distributionHouseAreaId);

                                            final Map<Long, JsonObject> locationList = collect(houseList.values(), mc.locations.name(), QC.distributionHouse);

                                            mongoClient.find(mc.locations.name(), new JsonObject()
                                                            .put(QC.id, new JsonObject()
                                                                    .put(QC.$in, new JsonArray(locationList.values().stream().map(v -> v.getLong(QC.id)).collect(Collectors.toList())))),
                                                    taskCoordinator.add(lList -> {
                                                        if (lList.size() < locationList.size()) {
                                                            final Set<Long> lListIdSet = idSet(lList);
                                                            putInErrorBuilder(errorBuilder, locationList.values(), lListIdSet, QC.locationId, "Locations");
                                                            return;
                                                        }

                                                        checkParents(lList, locationList, errorBuilder, QC.distributionHouse, QC.location, QC.locationDistributionHouseId);
                                                    }));

                                            final Map<String, JsonObject> brList = collectUser(houseList.values(), QC.brs, QC.distributionHouse);

                                            mongoClient.find(mc.employees.name(), new JsonObject()
                                                            .put(QC.userId, new JsonObject()
                                                                    .put(QC.$in, new JsonArray(
                                                                            brList.values().stream()
                                                                                    .map(v -> v.getString(QC.userId))
                                                                                    .collect(Collectors.toList()))))
                                                            .put(QC.userTypeId, EmployeeType.br.id),
                                                    taskCoordinator.add(bList -> {
                                                        if (bList.size() < brList.size()) {
                                                            final Set<String> bListIdSet = userIdSet(bList);
                                                            putInErrorBuilderForUser(errorBuilder, brList.values(), bListIdSet, QC.brId, "BRS");
                                                            return;
                                                        }

                                                        checkParentsForUsers(bList, brList, errorBuilder, QC.distributionHouse, QC.br, QC.brDistributionHouseId);
                                                    }));

                                            final Map<String, JsonObject> brSupervisorList = collectUser(houseList.values(), QC.brSupervisors, QC.distributionHouse);

                                            mongoClient.find(mc.employees.name(),
                                                    new JsonObject()
                                                            .put(QC.userId,
                                                                    new JsonObject().put(QC.$in, new JsonArray(
                                                                            brSupervisorList.values().stream()
                                                                                    .map(v -> v.getString(QC.userId))
                                                                                    .collect(Collectors.toList()))))
                                                            .put(QC.userTypeId, EmployeeType.brSupervisor.id),
                                                    taskCoordinator.add(supList -> {
                                                        if (supList.size() < brSupervisorList.size()) {
                                                            final Set<String> supListIdSet = userIdSet(supList);
                                                            putInErrorBuilderForUser(errorBuilder, brSupervisorList.values(), supListIdSet, QC.brSupervisorId, "BR Supervisors");
                                                            return;
                                                        }

                                                        checkParentsForUsers(supList, brSupervisorList, errorBuilder, QC.distributionHouse, QC.brSupervisor, QC.brSupervisorDistributionHouseId);
                                                    }));
                                        }));


                                final Map<String, JsonObject> areaCoordinatorList = collectUser(areaList.values(), QC.areaCoordinators, QC.area);

                                mongoClient.find(mc.employees.name(), new JsonObject()
                                                .put(QC.userId,
                                                        new JsonObject()
                                                                .put(QC.$in, new JsonArray(
                                                                        areaCoordinatorList.values().stream()
                                                                                .map(v -> v.getString(QC.userId))
                                                                                .collect(Collectors.toList()))))
                                                .put(QC.userTypeId, EmployeeType.areaCoordinator.id),
                                        taskCoordinator.add(acList -> {
                                            if (acList.size() < areaCoordinatorList.size()) {
                                                final Set<String> acListIdSet = userIdSet(acList);
                                                putInErrorBuilderForUser(errorBuilder, areaCoordinatorList.values(), acListIdSet, QC.areaCoordinatorId, "Area Coordinators");
                                                return;
                                            }

                                            checkParentsForUsers(acList, areaCoordinatorList, errorBuilder, QC.area, QC.areaCoordinator, QC.areaCoordinatorAreaId);
                                        }));
                            }));
                }));
    }

    public void ensureBrandExists(final long brandId, final ConsumerUnchecked<Boolean> consumerUnchecked, final Message message) {
        ensureIdExists(mc.brands, brandId, consumerUnchecked, message);
    }

    public void ensureIdExistsAndNameModified(final mc dbName, final long id, final String name, final BiConsumerUnchecked<Boolean, Boolean> biConsumer, final Message message) {
        mongoClient.findOne(dbName.name(),
                new JsonObject()
                        .put(QC.id, id),
                new JsonObject()
                        .put(QC.id, 1)
                        .put(QC.name, 1),
                withReply(obj -> {
                    biConsumer.accept(obj != null, !name.equals(
                                    obj.getString(QC.name))
                    );
                }, message));
    }

    public void ensureIdExists(final mc dbName, final long id, final ConsumerUnchecked<Boolean> consumer, final Message message) {
        mongoClient.count(dbName.name(),
                new JsonObject().put(QC.id, id),
                withReply(count -> {
                    consumer.accept(count > 0);
                }, message));
    }

    public void ensureIdNotExists(final mc dbName, final long id, final ConsumerUnchecked<Boolean> consumer, final Message message) {
        mongoClient.count(dbName.name(),
                new JsonObject().put(QC.id, id),
                withReply(count -> {
                    consumer.accept(count <= 0);
                }, message));
    }

    public void ensureNameNotExists(final mc dbName, final String name, final ConsumerUnchecked<Boolean> consumer, final Message message) {
        mongoClient.count(dbName.name(),
                new JsonObject()
                        .put(QC.name, name),
                withReply(count -> {
                    consumer.accept(count <= 0);
                }, message));
    }

    private void checkParentsForUsers(final List<JsonObject> uListFromDB, final Map<String, JsonObject> userListToCheckAgainst, final ErrorBuilder errorBuilder, final String parent, final String child, final String errorField) {
        try {
            uListFromDB.forEach(user -> {
                if (!user.getJsonObject(parent).getLong(QC.id).equals(
                        userListToCheckAgainst.get(user.getString(QC.userId))
                                .getJsonObject(parent).getLong(QC.id))) {
                    errorBuilder.put(errorField, String.format("The " + parent + " ID %d for " + child + " id %d is incorrect.",
                            userListToCheckAgainst.get(user.getString(QC.userId))
                                    .getJsonObject(parent).getLong(QC.id), user.getString(QC.userId)));
                }
                userListToCheckAgainst.get(user.getString(QC.userId)).put(QC.id, user.getLong(QC.id));
            });
        } catch (NullPointerException ex) {
            ex.printStackTrace();
        }
    }

    private void checkParents(final List<JsonObject> listFromDB, final Map<Long, JsonObject> listToCheckAgainst, final ErrorBuilder errorBuilder, final String parentField, final String errorFieldLabel, final String errorField) {
        try {
            listFromDB.forEach(area -> {
                if (!area.getJsonObject(parentField).getLong(QC.id).equals(
                        listToCheckAgainst.get(area.getLong(QC.id))
                                .getJsonObject(parentField).getLong(QC.id))) {
                    errorBuilder.put(errorField, String.format("The " + parentField + " ID %d for " + errorFieldLabel + " id %d is incorrect.",
                            listToCheckAgainst.get(area.getLong(QC.id)).getJsonObject(parentField).getLong(QC.id), area.getLong(QC.id)));
                }
            });
        } catch (NullPointerException ex) {
            ex.printStackTrace();
        }
    }

    private Map<String, JsonObject> collectUser(final Collection<JsonObject> regionList, final String field, final String parentField) {
        Map<String, JsonObject> map = new HashMap<>();
        regionList.forEach(v -> {
            v.getJsonArray(field, new JsonArray()).forEach(a -> {
                final JsonObject jsonObject = toJsonObject(a);
                map.put(jsonObject.getString(QC.userId), jsonObject.put(parentField,
                        new JsonObject()
                                .put(QC.id, v.getLong(QC.id))));
            });
        });
        return map;
    }

    private Map<Long, JsonObject> collect(final Collection<JsonObject> regionList, final String field, final String parentField) {
        Map<Long, JsonObject> map = new HashMap<>();
        regionList.forEach(v -> {
            v.getJsonArray(field, new JsonArray()).forEach(a -> {
                final JsonObject jsonObject = toJsonObject(a);
                map.put(jsonObject.getLong(QC.id), jsonObject.put(parentField,
                        new JsonObject()
                                .put(QC.id, v.getLong(QC.id))));
            });
        });
        return map;
    }

    private void putInErrorBuilderForUser(final ErrorBuilder errorBuilder, final Collection<JsonObject> regionList, final Set<String> rListIdSet, final String field, final String label) {
        errorBuilder.put(field, String.format(label + " %s are invlid.",
                regionList.stream()
                        .filter(v -> !rListIdSet.contains(v.getString(QC.userId)))
                        .map(v -> String.format("[UserID: %d, Name: %s]",
                                v.getString(QC.userId), v.getString(QC.name)))
                        .collect(Collectors.toList())
        ));
    }

    private void putInErrorBuilder(final ErrorBuilder errorBuilder, final Collection<JsonObject> regionList, final Set<Long> rListIdSet, final String field, final String label) {
        errorBuilder.put(field, String.format(label + " %s are invlid.",
                regionList.stream()
                        .filter(v -> !rListIdSet.contains(v.getLong(QC.id)))
                        .map(v -> String.format("[ID: %d, Name: %s]",
                                v.getLong(QC.id), v.getString(QC.name)))
                        .collect(Collectors.toList())));
    }

    private Set<Long> idSet(List<JsonObject> rList) {
        return rList.stream().map(v -> v.getLong(QC.id)).collect(Collectors.toSet());
    }

    private Set<String> userIdSet(List<JsonObject> supList) {
        return supList.stream().map(v -> v.getString(User.userId)).collect(Collectors.toSet());
    }

    public static boolean validateDateFormat(ErrorBuilder errorBuilder, String date, String fieldName, String errorMessage) {
        final boolean isInvalid = parseMongoDate(date, null) == null;
        if (isInvalid) errorBuilder.put(fieldName, errorMessage);
        return isInvalid;
    }

    public <T> T parseJson(final String data, final Class<T> tClass) throws Exception {
        return objectMapper.readValue(data, tClass);
    }

    public String toJson(final Object o) throws Exception {
        return objectMapper.writeValueAsString(o);
    }

    private String dateString(Object o) {
        if (o instanceof JsonObject) {
            return ((JsonObject) o).getString(QC.$date);
        }
        return o.toString();
    }

    private <T> JsonArray serialize(final Collection<ConstraintViolation<T>> violations) {
        return new JsonArray(violations.stream().map((ConstraintViolation<T> v) -> serialize(v)).collect(Collectors.toList()));
    }

    public static <T> JsonObject serialize(final ConstraintViolation<T> violation) {
        final ArrayList<String> nodes = new ArrayList<>();
        violation.getPropertyPath().forEach(n -> nodes.add(n.getName()));

        return
                new JsonObject()
                        .put(QC.message, violation.getMessage())
                        .put(QC.invalidValue, violation.getInvalidValue())
                        .put(QC.messageTemplate, violation.getMessageTemplate())
                        .put(QC.field, String.join(".", nodes))
                        .put(QC.rootBeanClass, violation.getRootBeanClass().toString())
                ;
    }
}
