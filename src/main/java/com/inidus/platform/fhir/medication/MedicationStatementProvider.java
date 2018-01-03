package com.inidus.platform.fhir.medication;

import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import com.fasterxml.jackson.databind.JsonNode;
import org.hl7.fhir.dstu3.model.MedicationStatement;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

@Component("MedicationStatementProvider")
public class MedicationStatementProvider implements IResourceProvider {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final MedicationStatementConverter openehrConverter = new MedicationStatementConverter();

    @Autowired
    private MedicationStatementConnector openEhrService;

    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return MedicationStatement.class;
    }

    @Read()
    public MedicationStatementCC getResourceById(@IdParam IdType id) throws ParseException, IOException {
        JsonNode ehrJsonList = openEhrService.getResourceById(id.getIdPart());

        if (null != ehrJsonList) {
            return openehrConverter.convertToMedicationStatement(ehrJsonList);
        } else {
            return null;
        }
    }

    @Search()
    public List<MedicationStatementCC> getAllResources() throws ParseException, IOException {
        JsonNode ehrJsonList = openEhrService.getAllResources();

        if (null != ehrJsonList) {
            return openehrConverter.convertToMedicationStatementList(ehrJsonList);
        } else {
            return null;
        }
    }

   /* @Search()
    public List<MedicationStatementCC> getFilteredResources(
       //     @OptionalParam(name="_list") StringParam listParam,
            @OptionalParam(name = "patient.id") StringParam id,
            @OptionalParam(name = "patient.identifier") TokenParam identifier,
            @OptionalParam(name = "category") StringParam category,
            @OptionalParam(name = "status") StringParam clinicalStatus,
            @OptionalParam(name = "asserted-date") DateRangeParam dateRange) throws IOException {

        JsonNode ehrJsonList = openEhrService.getFilteredConditions(id, identifier, category, clinicalStatus,dateRange);

        if (null != ehrJsonList) {
            return openehrConverter.convertToMedicationStatementList(ehrJsonList);
        } else {
            return null;
        }
    }*/

}
