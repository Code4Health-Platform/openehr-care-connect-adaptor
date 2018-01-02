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
import com.inidus.platform.openehr.OpenEhrConditionConnector;
import com.inidus.platform.openehr.OpenEhrConnector;
import org.hl7.fhir.dstu3.model.Condition;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

@Component("ConditionProvider")
public class ConditionProvider implements IResourceProvider {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final ConditionConverter openehrConverter = new ConditionConverter();

    @Autowired
    private OpenEhrConditionConnector openEhrService;

    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return Condition.class;
    }

    @Read()
    public ConditionCC getResourceById(@IdParam IdType id) throws ParseException, IOException {
        JsonNode ehrJsonList = openEhrService.getConditionById(id.getIdPart());

        if (null != ehrJsonList) {
            return openehrConverter.convertToCondition(ehrJsonList);
        } else {
            return null;
        }
    }

    @Search()
    public List<ConditionCC> getAllResources() throws ParseException, IOException {
        JsonNode ehrJsonList = openEhrService.getAllResources();

        if (null != ehrJsonList) {
            return openehrConverter.convertToConditionList(ehrJsonList);
        } else {
            return null;
        }
    }

    @Search()
    public List<ConditionCC> getFilteredResources(
            @OptionalParam(name = "patient.identifier") TokenParam id,
            @OptionalParam(name = "category") StringParam category,
            @OptionalParam(name = "date") DateRangeParam dateRange) throws IOException {

        JsonNode ehrJsonList = openEhrService.getFilteredConditions(id, category, dateRange);

        if (null != ehrJsonList) {
            return openehrConverter.convertToConditionList(ehrJsonList);
        } else {
            return null;
        }
    }
}
