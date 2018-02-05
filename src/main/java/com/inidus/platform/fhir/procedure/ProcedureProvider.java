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
import com.inidus.platform.fhir.procedure.ProcedureConverter;
import com.inidus.platform.fhir.procedure.ProcedureConnector;
import com.inidus.platform.fhir.procedure.ProcedureCC;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Procedure;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

@Component("ProcedureProvider")
public class ProcedureProvider implements IResourceProvider {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final ProcedureConverter openehrConverter = new ProcedureConverter();

    @Autowired
    private ProcedureConnector openEhrService;

    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return Procedure.class;
    }

    @Read()
    public ProcedureCC getResourceById(@IdParam IdType id) throws ParseException, IOException {
        JsonNode ehrJsonList = openEhrService.getResourceById(id.getIdPart());

        if (null != ehrJsonList) {
            return openehrConverter.convertToProcedure(ehrJsonList);
        } else {
            return null;
        }
    }

    @Search()
    public List<ProcedureCC> getAllResources() throws ParseException, IOException {
        JsonNode ehrJsonList = openEhrService.getAllResources();

        if (null != ehrJsonList) {
            return openehrConverter.convertToProcedureList(ehrJsonList);
        } else {
            return null;
        }
    }

   @Search()
    public List<ProcedureCC> getFilteredResources(
       //     @OptionalParam(name="_list") StringParam listParam,
            @OptionalParam(name = "patient.id") StringParam id,
            @OptionalParam(name = "patient.identifier") TokenParam identifier,
            @OptionalParam(name = "status") StringParam status,
            @OptionalParam(name = "datePerformed") DateRangeParam dateRange) throws IOException {

        JsonNode ehrJsonList = openEhrService.getFilteredProcedures(id, identifier, status,dateRange);

        if (null != ehrJsonList) {
            return openehrConverter.convertToProcedureList(ehrJsonList);
        } else {
            return null;
        }
    }

}
