package com.inidus.platform.fhir.allergy;

import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import com.fasterxml.jackson.databind.JsonNode;
import org.hl7.fhir.dstu3.model.AllergyIntolerance;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component("AllergyProvider")
public class AllergyProvider implements IResourceProvider {
    private final AllergyConverter converter = new AllergyConverter();

    @Autowired
    private AllergyConnector connector;

    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return AllergyIntolerance.class;
    }

    @Read()
    public AllergyIntoleranceCC getResourceById(@IdParam IdType id) throws IOException {
        JsonNode ehrJsonList = connector.getResourceById(id.getIdPart());

        if (null != ehrJsonList) {
            return converter.convertToAllergyIntolerance(ehrJsonList);
        } else {
            return null;
        }
    }

    @Search()
    public List<AllergyIntoleranceCC> getAllResources() throws IOException {
        JsonNode ehrJsonList = connector.getAllResources();

        if (null != ehrJsonList) {
            return converter.convertToAllergyIntoleranceList(ehrJsonList);
        } else {
            return null;
        }
    }

    @Search()
    public List<AllergyIntoleranceCC> getFilteredResources(
            @OptionalParam(name = "patient.identifier") TokenParam id,
            @OptionalParam(name = "category") StringParam category,
            @OptionalParam(name = "date") DateRangeParam dateRange) throws IOException {

        JsonNode ehrJsonList = connector.getFilteredAllergies(id, category, dateRange);

        if (null != ehrJsonList) {
            return converter.convertToAllergyIntoleranceList(ehrJsonList);
        } else {
            return null;
        }
    }
}
