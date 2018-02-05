package com.inidus.platform.fhir.procedure;

import com.fasterxml.jackson.databind.JsonNode;
import com.inidus.platform.fhir.procedure.ProcedureCC;
import com.inidus.platform.fhir.openehr.OpenEHRConverter;
import org.hl7.fhir.dstu3.model.*;
import org.hl7.fhir.dstu3.model.Procedure.ProcedureStatus;
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
    public ProcedureCC convertToProcedure(JsonNode ehrJson) {
        List<ProcedureCC> list = convertToProcedureList(ehrJson);
        return list.get(0);
    }

    /**
     * Converts the given json coming from openEHR into a list of {@link Procedure} resources.
     * Duplicates in the json will be merged.
     *
     * @param ehrJson is the array contained inside the AQL "resultSet" section
     */
    public List<ProcedureCC> convertToProcedureList(JsonNode ehrJson) {
        List<ProcedureCC> profiles = new ArrayList<>();
        Iterator<JsonNode> it = ehrJson.elements();
        while (it.hasNext()) {
            ProcedureCC conditionResource = createProcedureResource(it.next());
            profiles.add(conditionResource);
        }
        return profiles;
    }

    private ProcedureCC createProcedureResource(JsonNode ehrJson) {

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


    private ProcedureStatus convertProcedureStatus(JsonNode ehrJson) {
        ProcedureStatus status = null;

    /* openEHR Valueset
        at0021::Active [This is an active medication.]
        at0022::Stopped [This is a medication that has previously been issued, dispensed or administered but has now been discontinued.]
        at0023::Never active [A medication which was ordered or authorised but has been cancelled prior to being issued, dispensed or adiminstered.]
        at0024::Completed [The medication course has been completed.]
        at0025::Obsolete [This medication order has been superseded by another.]
        at0026::Suspended [Actions resulting from the order are to be temporarily halted, but are expected to continue later. May also be called 'on-hold'.]
        at0027::Draft [The medication order has been made but further processes e.g. sign-off or verification are required before it becomes actionable.]
    */

//        String statusCode = getResultsetString(ehrJson, "Status_code");
//        if (statusCode == null) {
//            status = ProcedureStatus.ACTIVE;
//        } else if (statusCode.equals("at0021")) {
//            status = ProcedureStatus.ACTIVE;
//        } else if (statusCode.equals("at0022")) {
//            status = ProcedureStatus.STOPPED;
//        } else if (statusCode.equals("at0023")) {
//            status = ProcedureStatus.INTENDED;
//        } else if (statusCode.equals("at0024")) {
//            status = ProcedureStatus.COMPLETED;
//        } else if (statusCode.equals("at0025")) {
//            status = ProcedureStatus.STOPPED;
//        } else if (statusCode.equals("at0026")) {
//            status = ProcedureStatus.ONHOLD;
//        } else if (statusCode.equals("at0027")) {
//            status = ProcedureStatus.INTENDED;
//        }
//
       return status;
    }

}
