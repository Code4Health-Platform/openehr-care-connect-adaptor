package com.inidus.platform;

import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import com.fasterxml.jackson.databind.JsonNode;
import com.inidus.platform.openehr.OpenEhrService;
import org.hl7.fhir.dstu3.model.AllergyIntolerance;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

@Component("AllergyProvider")
public class AllergyProvider implements IResourceProvider {
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    @Qualifier("marandConnector")
    private OpenEhrService openEhrService;

    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return AllergyIntolerance.class;
    }

    @Read()
    public AllergyIntolerance getResourceById(@IdParam IdType id) throws ParseException, IOException {
        JsonNode ehrJsonList = openEhrService.getAllergyById(id.getIdPart());

        return new OpenEhrConverter().convertToAllergyIntolerance(ehrJsonList);
    }

    @Search()
    public List<AllergyIntolerance> getAllResources() throws ParseException, IOException {
        JsonNode ehrJsonList = openEhrService.getAllAllergies();

        return new OpenEhrConverter().convertToAllergyIntoleranceList(ehrJsonList);
    }

    @Search()
    public List<AllergyIntolerance> getResourceByPatientIdentifier(@RequiredParam(name = Patient.SP_IDENTIFIER) StringParam id,
                                                                   @OptionalParam(name = "namespace") StringParam namespace) throws IOException {
        if (null == namespace) namespace = new StringParam("uk.nhs.nhs_number");
        JsonNode ehrJsonList = openEhrService.getAllergyByPatientIdentifier(id.getValue(), namespace.getValue());
        return new OpenEhrConverter().convertToAllergyIntoleranceList(ehrJsonList);
    }
}
