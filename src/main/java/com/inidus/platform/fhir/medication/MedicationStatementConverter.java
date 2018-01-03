package com.inidus.platform.fhir.medication;

import com.fasterxml.jackson.databind.JsonNode;
import com.inidus.platform.fhir.openehr.OpenEHRConverter;
import org.hl7.fhir.dstu3.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MedicationStatementConverter extends OpenEHRConverter{
    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Converts the given json coming from openEHR into 1 {@link Condition} resource.
     * Duplicates in the json will be merged.
     *
     * @param ehrJson is the array contained inside the "resultSet" section
     */
    public MedicationStatementCC convertToMedicationStatement(JsonNode ehrJson) {
        List<MedicationStatementCC> list = convertToMedicationStatementList(ehrJson);
        return list.get(0);
    }

    /**
     * Converts the given json coming from openEHR into a list of {@link Condition} resources.
     * Duplicates in the json will be merged.
     *
     * @param ehrJson is the array contained inside the "resultSet" section
     */
    public List<MedicationStatementCC> convertToMedicationStatementList(JsonNode ehrJson) {
        List<MedicationStatementCC> profiles = new ArrayList<>();
        Iterator<JsonNode> it = ehrJson.elements();
        while (it.hasNext()) {
            MedicationStatementCC conditionResource = createMedicationStatementResource(it.next());
            profiles.add(conditionResource);
        }
        return profiles;
    }

    private MedicationStatementCC createMedicationStatementResource(JsonNode ehrJson) {

        MedicationStatementCC retVal = new MedicationStatementCC();

        retVal.setId(convertResourceId(ehrJson));
        retVal.setSubject(convertPatientReference(ehrJson));
//        retVal.setDateAsserted(convertAssertedDate(ehrJson));
        retVal.getInformationSource().setResource(convertAsserter(ehrJson));

        retVal.setMedication(convertScalarCodableConcept(ehrJson,"Medication_item"));
        retVal.addReasonCode(convertScalarCodableConcept(ehrJson,"Clinical_indication"));

        //
//        JsonNode onsetDate =  ehrJson.get("Date_time_of_onset");
//        if (null != onsetDate) {
//            retVal.setOnset(new DateTimeType(onsetDate.asText(null)));
//        }
//
//        JsonNode resolutionDate =  ehrJson.get("Date_time_of_resolution");
//        if (null != resolutionDate){
//            retVal.setAbatement(new DateTimeType(resolutionDate.textValue()));
//        }

        JsonNode comment = ehrJson.get("Comment");
        if (null != comment) {
            retVal.addNote(new Annotation().setText(comment.textValue()));
        }
        return retVal;
    }
    
}
