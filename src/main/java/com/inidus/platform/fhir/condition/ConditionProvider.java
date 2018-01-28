package com.inidus.platform.fhir.condition;

import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import com.fasterxml.jackson.databind.JsonNode;
import org.hl7.fhir.dstu3.model.Condition;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component("ConditionProvider")
public class ConditionProvider implements IResourceProvider {
    private final ConditionConverter converter = new ConditionConverter();

    @Autowired
    private ConditionConnector connector;

    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return Condition.class;
    }

    @Read()
    public ConditionCC getResourceById(@IdParam IdType id) throws IOException {
        JsonNode ehrJsonList = connector.getResourceById(id.getIdPart());

        if (null != ehrJsonList) {
            return converter.convertToCondition(ehrJsonList);
        } else {
            return null;
        }
    }

    @Search()
    public List<ConditionCC> getAllResources() throws IOException {
        JsonNode ehrJsonList = connector.getAllResources();

        if (null != ehrJsonList) {
            return converter.convertToConditionList(ehrJsonList);
        } else {
            return null;
        }
    }

    @Search()
    public List<ConditionCC> getFilteredResources(
            @OptionalParam(name = "patient.id") StringParam id,
            @OptionalParam(name = "patient.identifier") TokenParam identifier,
            @OptionalParam(name = "category") StringParam category,
            @OptionalParam(name = "clinical-status") StringParam clinicalStatus,
            @OptionalParam(name = "asserted-date") DateRangeParam dateRange) throws IOException {

        JsonNode ehrJsonList = connector.getFilteredConditions(id, identifier, category, clinicalStatus, dateRange);

        if (null != ehrJsonList) {
            return converter.convertToConditionList(ehrJsonList);
        } else {
            return null;
        }
    }
}
