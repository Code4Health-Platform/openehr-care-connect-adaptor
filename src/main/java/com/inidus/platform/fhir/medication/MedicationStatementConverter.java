package com.inidus.platform.fhir.medication;

import com.fasterxml.jackson.databind.JsonNode;
import com.inidus.platform.fhir.openehr.OpenEHRConverter;
import org.hl7.fhir.dstu3.model.*;


import org.hl7.fhir.dstu3.model.MedicationStatement.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MedicationStatementConverter extends OpenEHRConverter{
 //   private final Logger logger = LoggerFactory.getLogger(getClass());

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
     * Converts the given json coming from openEHR into a list of {@link MedicationStatement} resources.
     * Duplicates in the json will be merged.
     *
     * @param ehrJson is the array contained inside the AQL "resultSet" section
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
        retVal.setDateAsserted(convertAssertedDate(ehrJson));
        retVal.getInformationSource().setResource(convertAsserter(ehrJson));

        retVal.setMedication(convertMedicationResource(ehrJson));

        retVal.addReasonCode(convertCodeableConcept(ehrJson,"Clinical_indication"));

        //Default 'taken' to Unknown
        retVal.setTaken(MedicationStatementTaken.UNK);

        retVal.setStatus(convertMedicationStatus(ehrJson));

        retVal.setEffective(convertChoiceDate(ehrJson,"Order_start_date_time"));
        retVal.addDosage(convertDosage(ehrJson));

        retVal.addNote(new Annotation().setText(getResultsetString(ehrJson,"Comment")));

        return retVal;
    }

    private Dosage convertDosage(JsonNode ehrJson) {
        Dosage dosage = new Dosage();

        dosage.setText(convertDosageDirections(ehrJson));
        dosage.addAdditionalInstruction(convertCodeableConcept(ehrJson,"Additional_instruction"));
        dosage.setPatientInstruction(getResultsetString(ehrJson, "Patient_instructions"));
        dosage.setRoute(convertCodeableConcept(ehrJson,"Route"));
        dosage.setMethod(convertCodeableConcept(ehrJson,"Method"));
        dosage.setSite(convertCodeableConcept(ehrJson,"Site"));


        return dosage;
    }

    private Reference convertMedicationResource(JsonNode ehrJson){

        // Adding medication to Contained.
        Medication medResource = new MedicationCC();
        Reference medRefDt = new Reference("Medication/1");

        medResource.setCode(convertCodeableConcept(ehrJson,"Medication_item"));
        medResource.setForm(convertCodeableConcept(ehrJson,"Medication_form"));

        // Medication reference. This should point to the contained resource.
        medRefDt.setDisplay(medResource.getCode().getText());
        // Resource reference set, but no ID
        medRefDt.setResource(medResource);

       return medRefDt;

    }


    private String convertDosageDirections(JsonNode ehrJson) {

        String overallDoseDirections = getResultsetString(ehrJson, "Overall_directions_description");

        if (overallDoseDirections == null)
        {
            return convertDoseAmountFrequencyDirections(ehrJson);
        }
        else
            return overallDoseDirections;

    }


    /**
     * Concatenates DoseAmountDescription and DoseFrequencyDescription elements if present
     * @param ehrJson The openEHR Resultset tree
     * @return The concatenated string, if data present
     */
    private String convertDoseAmountFrequencyDirections(JsonNode ehrJson){

        String outText;
        String doseAmountText = getResultsetString(ehrJson,"Dose_amount_description");
        String doseFrequencyText =  getResultsetString(ehrJson,"Dose_frequency_description");;

        if (doseAmountText != null)
            outText = doseAmountText;
        else
            outText = "";

        if (doseFrequencyText != null)
            outText+=  " " + doseFrequencyText;

        return outText;
    }

    private MedicationStatementStatus convertMedicationStatus(JsonNode ehrJson) {
        MedicationStatementStatus status = null;

    /* openEHR Valueset
        at0021::Active [This is an active medication.]
        at0022::Stopped [This is a medication that has previously been issued, dispensed or administered but has now been discontinued.]
        at0023::Never active [A medication which was ordered or authorised but has been cancelled prior to being issued, dispensed or adiminstered.]
        at0024::Completed [The medication course has been completed.]
        at0025::Obsolete [This medication order has been superseded by another.]
        at0026::Suspended [Actions resulting from the order are to be temporarily halted, but are expected to continue later. May also be called 'on-hold'.]
        at0027::Draft [The medication order has been made but further processes e.g. sign-off or verification are required before it becomes actionable.]
    */

        String statusCode = getResultsetString(ehrJson, "Status_code");
        if (statusCode == null) {
            status = MedicationStatementStatus.ACTIVE;
        } else if (statusCode.equals("at0021")) {
            status = MedicationStatementStatus.ACTIVE;
        } else if (statusCode.equals("at0022")) {
            status = MedicationStatementStatus.STOPPED;
        } else if (statusCode.equals("at0023")) {
            status = MedicationStatementStatus.INTENDED;
        } else if (statusCode.equals("at0024")) {
            status = MedicationStatementStatus.COMPLETED;
        } else if (statusCode.equals("at0025")) {
            status = MedicationStatementStatus.STOPPED;
        } else if (statusCode.equals("at0026")) {
            status = MedicationStatementStatus.ONHOLD;
        } else if (statusCode.equals("at0027")) {
            status = MedicationStatementStatus.INTENDED;
        }
        return status;
    }

}
