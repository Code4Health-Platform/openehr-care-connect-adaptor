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
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.MedicationStatement;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component("MedicationStatementProvider")
public class MedicationStatementProvider implements IResourceProvider {
    private final MedicationStatementConverter converter = new MedicationStatementConverter();

    @Autowired
    private MedicationStatementConnector connector;

    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return MedicationStatement.class;
    }

    @Read()
    public MedicationStatementCC getResourceById(@IdParam IdType id) throws IOException {
        JsonNode ehrJsonList = connector.getResourceById(id.getIdPart());

        if (null != ehrJsonList) {
            return converter.convertToMedicationStatement(ehrJsonList);
        } else {
            return null;
        }
    }

    @Search()
    public List<MedicationStatementCC> getAllResources() throws IOException {
        JsonNode ehrJsonList = connector.getAllResources();

        if (null != ehrJsonList) {
            return converter.convertToMedicationStatementList(ehrJsonList);
        } else {
            return null;
        }
    }

    @Search()
    public List<MedicationStatementCC> getFilteredResources(
            @OptionalParam(name = "patient.id") StringParam id,
            @OptionalParam(name = "patient.identifier") TokenParam identifier,
            @OptionalParam(name = "status") StringParam status,
            @OptionalParam(name = "effective") DateRangeParam dateRange) throws IOException {

        JsonNode ehrJsonList = connector.getFilteredMedicationStatements(id, identifier, status, dateRange);

        if (null != ehrJsonList) {
            return converter.convertToMedicationStatementList(ehrJsonList);
        } else {
            return null;
        }
    }

}
