package com.inidus.platform.fhir.procedure;


import org.hl7.fhir.dstu3.model.Procedure;
import org.hl7.fhir.exceptions.FHIRException;

import java.util.HashMap;
import java.util.Map;

public class ProcedureMappings {

    private static final Map<String, String> openEHRCurrentStateToFHIRProcedureStatusCode = new HashMap<>();
    private static final Map<String, String> FHIRProcedureStatusToOpenEhrCurrentStateCode = new HashMap<>();
    private static final Map<String, String> OpenEhrCurrentStateToFhirProcedureSystem = new HashMap<>();
    private static final Map<String, String> FhirProcedureStatusToOpenEhrCurrentStateSystem = new HashMap<>();

//    private static final Logger log = LoggerFactory.getLogger(DfText.class);

    static {
        openEHRCurrentStateToFHIRProcedureStatusCode.put("524", "preparation"); //initial
        openEHRCurrentStateToFHIRProcedureStatusCode.put("526", "preparation"); //planned
        openEHRCurrentStateToFHIRProcedureStatusCode.put("527", "suspended"); //postponed
        openEHRCurrentStateToFHIRProcedureStatusCode.put("528", "aborted"); //aborted
        openEHRCurrentStateToFHIRProcedureStatusCode.put("529", "preparation"); //scheduled
        openEHRCurrentStateToFHIRProcedureStatusCode.put("245", "in-progress"); //active
        openEHRCurrentStateToFHIRProcedureStatusCode.put("530", "suspended"); //suspended
        openEHRCurrentStateToFHIRProcedureStatusCode.put("531", "aborted"); //aborted
        openEHRCurrentStateToFHIRProcedureStatusCode.put("532", "completed"); //completed
        openEHRCurrentStateToFHIRProcedureStatusCode.put("533", "aborted"); //expired

        FHIRProcedureStatusToOpenEhrCurrentStateCode.put("preparation","526"); //planned
        FHIRProcedureStatusToOpenEhrCurrentStateCode.put("suspended","530"); //suspended
        FHIRProcedureStatusToOpenEhrCurrentStateCode.put("aborted","528"); //aborted
        FHIRProcedureStatusToOpenEhrCurrentStateCode.put( "in-progress","245"); //active
        FHIRProcedureStatusToOpenEhrCurrentStateCode.put("completed","532"); //completed

        //System mappings
        OpenEhrCurrentStateToFhirProcedureSystem.put("openehr","http://hl7.org/fhir/event-status");
        FhirProcedureStatusToOpenEhrCurrentStateSystem.put("http://hl7.org/fhir/event-status","openehr");
    }

    public static Procedure.ProcedureStatus getProcedureStatusEnumFromCode(String currentStatusCode) throws FHIRException {

        return Procedure.ProcedureStatus.fromCode(
                openEHRCurrentStateToFHIRProcedureStatusCode.get(currentStatusCode));
    }
}
