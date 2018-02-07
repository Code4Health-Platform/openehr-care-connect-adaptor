package com.inidus.platform.fhir.procedure;

import com.fasterxml.jackson.databind.JsonNode;
import com.inidus.platform.fhir.openehr.OpenEHRConverter;
import org.hl7.fhir.dstu3.model.*;
import org.hl7.fhir.dstu3.model.Procedure.ProcedureStatus;
import org.hl7.fhir.exceptions.FHIRException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.inidus.platform.fhir.procedure.ProcedureMappings.getProcedureStatusEnumFromCode;

public class ProcedureConverter extends OpenEHRConverter {

    /**
     * Converts the given json coming from openEHR into 1 {@link Condition} resource.
     * Duplicates in the json will be merged.
     *
     * @param ehrJson is the array contained inside the "resultSet" section
     */
    public ProcedureCC convertToProcedure(JsonNode ehrJson) throws FHIRException {
        List<ProcedureCC> list = convertToProcedureList(ehrJson);
        return list.get(0);
    }

    /**
     * Converts the given json coming from openEHR into a list of {@link Procedure} resources.
     * Duplicates in the json will be merged.
     *
     * @param ehrJson is the array contained inside the AQL "resultSet" section
     */
    public List<ProcedureCC> convertToProcedureList(JsonNode ehrJson) throws FHIRException {
        List<ProcedureCC> profiles = new ArrayList<>();
        Iterator<JsonNode> it = ehrJson.elements();
        while (it.hasNext()) {
            ProcedureCC conditionResource = createProcedureResource(it.next());
            profiles.add(conditionResource);
        }
        return profiles;
    }

    private ProcedureCC createProcedureResource(JsonNode ehrJson) throws FHIRException {

        ProcedureCC retVal = new ProcedureCC();

        retVal.setId(convertResourceId(ehrJson));
        retVal.setSubject(convertPatientReference(ehrJson));

        retVal.addPerformer(convertPerformer(ehrJson));

        retVal.setCode(convertCodeableConcept(ehrJson, "Procedure_name"));

        retVal.addReasonCode(convertCodeableConcept(ehrJson, "Reason"));

        retVal.addComplication(convertCodeableConcept(ehrJson, "Complication"));

        retVal.addBodySite(convertCodeableConcept(ehrJson, "Body_site"));

        retVal.setOutcome(convertCodeableConcept(ehrJson, "Outcome"));

        retVal.setCategory(convertCodeableConcept(ehrJson, "Procedure_type"));

        retVal.setStatus(convertProcedureStatus(ehrJson));

        retVal.setPerformed(convertChoiceDate(ehrJson, "Procedure_time"));

        String description = getResultsetString(ehrJson, "Description");
        if (description != null)
            retVal.addNote(new Annotation().setText("Description: " + description));

        String comment = getResultsetString(ehrJson, "comment");
        if (comment != null)
            retVal.addNote(new Annotation().setText("Comment: " + comment));

        return retVal;
    }

    private Procedure.ProcedurePerformerComponent convertPerformer(JsonNode ehrJson) {

        Procedure.ProcedurePerformerComponent performer = null;

        //The openEHR PARTICIPATION class maps well to Performer.
        JsonNode participationsNode = ehrJson.get("OtherParticipations");

        if (participationsNode == null) {
            return performer;
        }

        Practitioner practitioner = new Practitioner();
        performer = new Procedure.ProcedurePerformerComponent();
        Reference PerformerRefDt = new Reference("Practitioner/1");

        String name = participationsNode.path("performer").path("name").textValue();
        practitioner.addName().setText(name);

        String role = participationsNode.path("function").path("value").textValue();
        performer.setRole(new CodeableConcept().setText(role));

        PerformerRefDt.setDisplay(name + " : " + role);
        PerformerRefDt.setResource(practitioner);

        performer.setActor(PerformerRefDt);

        return performer;
    }

    private ProcedureStatus convertProcedureStatus(JsonNode ehrJson) throws FHIRException {

        String statusCode = getResultsetString(ehrJson, "Status_code");
        return getProcedureStatusEnumFromCode(statusCode);
    }

}
