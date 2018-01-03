package com.inidus.platform;

import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import com.fasterxml.jackson.databind.JsonNode;
import com.inidus.platform.conversion.AllergyConverter;
import com.inidus.platform.openehr.OpenEhrAllergyConnector;
import org.hl7.fhir.dstu3.model.AllergyIntolerance;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

@Component("AllergyProvider")
public class AllergyProvider implements IResourceProvider {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final AllergyConverter openEhrConverter = new AllergyConverter();

    @Autowired
    private OpenEhrAllergyConnector openEhrService;

    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return AllergyIntolerance.class;
    }

    @Read()
    public CCAllergyIntolerance getResourceById(@IdParam IdType id) throws ParseException, IOException {
        JsonNode ehrJsonList = openEhrService.getAllergyById(id.getIdPart());

        if (null != ehrJsonList) {
            return openEhrConverter.convertToAllergyIntolerance(ehrJsonList);
        } else {
            return null;
        }
    }

    @Search()
    public List<CCAllergyIntolerance> getAllResources() throws ParseException, IOException {

        JsonNode ehrJsonList = openEhrService.getAllResources();

        if (null != ehrJsonList) {
            return openEhrConverter.convertToAllergyIntoleranceList(ehrJsonList);
        } else {
            return null;
        }
    }

    @Search()
    public List<CCAllergyIntolerance> getFilteredResources(
            @OptionalParam(name = "patient.identifier") TokenParam id,
            @OptionalParam(name = "category") StringParam category,
            @OptionalParam(name = "date") DateRangeParam dateRange) throws IOException {

        JsonNode ehrJsonList = openEhrService.getFilteredAllergies(id, category, dateRange);

        if (null != ehrJsonList) {
            return openEhrConverter.convertToAllergyIntoleranceList(ehrJsonList);
        } else {
            return null;
        }
    }
}
