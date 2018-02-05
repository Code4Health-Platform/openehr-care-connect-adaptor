package com.inidus.platform.fhir.procedure;

import com.fasterxml.jackson.databind.JsonNode;
import com.inidus.platform.fhir.procedure.ProcedureCC;
import com.inidus.platform.fhir.openehr.OpenEHRConverter;
import com.inidus.platform.fhir.openehr.DfIsmTransition;
import org.hl7.fhir.dstu3.model.*;
import org.hl7.fhir.dstu3.model.Procedure.ProcedureStatus;
import org.hl7.fhir.exceptions.FHIRException;
import org.openehr.rm.datatypes.text.CodePhrase;
import org.openehr.rm.datatypes.text.DvCodedText;
import org.openehr.rm.demographic.Actor;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ProcedureConverter extends OpenEHRConverter{

    /**
     * Converts the given json coming from openEHR into 1 {@link Condition} resource.
     * Duplicates in the json will be merged.
     *
     * @param ehrJson is the array contained inside the "resultSet" section
     */
    public ProcedureCC convertToProcedure(JsonNode ehrJson)  throws FHIRException{
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

    private ProcedureCC createProcedureResource(JsonNode ehrJson) throws FHIRException{

        ProcedureCC retVal = new ProcedureCC();

        retVal.setId(convertResourceId(ehrJson));
        retVal.setSubject(convertPatientReference(ehrJson));

        retVal.addPerformer().setActor(convertOtherParticipations(ehrJson));

        retVal.setCode(convertCodeableConcept(ehrJson,"Procedure_name"));

        retVal.addReasonCode(convertCodeableConcept(ehrJson,"Reason"));

        retVal.addComplication(convertCodeableConcept(ehrJson,"Complication"));

        retVal.addBodySite(convertCodeableConcept(ehrJson,"Body_site"));

        retVal.setOutcome(convertCodeableConcept(ehrJson,"Outcome"));

        retVal.setCategory(convertCodeableConcept(ehrJson,"Procedure_type"));
        retVal.setStatus(convertProcedureStatus(ehrJson));

        retVal.setPerformed(convertChoiceDate(ehrJson,"Procedure_time"));

        retVal.addNote(new Annotation().setText("Description: "+ getResultsetString(ehrJson,"Description")));
        retVal.addNote(new Annotation().setText("Comment: "+ getResultsetString(ehrJson,"Comment")));

        return retVal;
    }

    private Reference convertOtherParticipations(JsonNode ehrJson){

        // Adding Performer to Contained.
        Practitioner PracResource = new Practitioner();
        Reference PerformerRefDt = new Reference("Performer/1");

      //  PracResource.addName().setText();
        // Medication reference. This should point to the contained resource.
        PerformerRefDt.setDisplay(PracResource.getName().toString());
        // Resource reference set, but no ID
        PerformerRefDt.setResource(PracResource);

       return PerformerRefDt;

    }

    private ProcedureStatus convertProcedureStatus(JsonNode ehrJson) throws FHIRException {

        String statusCode = getResultsetString(ehrJson, "Status_code");
        return DfIsmTransition.getProcedureStatusEnumFromCode(statusCode);
    }

}
