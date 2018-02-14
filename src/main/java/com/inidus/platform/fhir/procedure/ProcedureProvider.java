package com.inidus.platform.fhir.procedure;

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
import org.hl7.fhir.dstu3.model.Procedure;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component("ProcedureProvider")
public class ProcedureProvider implements IResourceProvider {
    private final ProcedureConverter converter = new ProcedureConverter();

    @Autowired
    private ProcedureConnector connector;

    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return Procedure.class;
    }

    @Read()
    public ProcedureCC getResourceById(@IdParam IdType id) throws IOException, FHIRException {
        JsonNode ehrJsonList = connector.getResourceById(id.getIdPart());

        if (null != ehrJsonList) {
            return converter.convertToProcedure(ehrJsonList);
        } else {
            return null;
        }
    }

    @Search()
    public List<ProcedureCC> getAllResources() throws IOException, FHIRException {
        JsonNode ehrJsonList = connector.getAllResources();

        if (null != ehrJsonList) {
            return converter.convertToProcedureList(ehrJsonList);
        } else {
            return null;
        }
    }

    @Search()
    public List<ProcedureCC> getFilteredResources(
            @OptionalParam(name = "patient.id") StringParam id,
            @OptionalParam(name = "patient.identifier") TokenParam identifier,
            @OptionalParam(name = "status") StringParam status,
            @OptionalParam(name = "datePerformed") DateRangeParam dateRange) throws IOException, FHIRException {

        JsonNode ehrJsonList = connector.getFilteredProcedures(id, identifier, status, dateRange);

        if (null != ehrJsonList) {
            return converter.convertToProcedureList(ehrJsonList);
        } else {
            return null;
        }
    }

}
