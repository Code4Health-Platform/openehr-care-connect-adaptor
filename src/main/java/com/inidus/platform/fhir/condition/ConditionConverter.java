package com.inidus.platform.fhir.condition;

import com.fasterxml.jackson.databind.JsonNode;
import com.inidus.platform.conversion.DfText;
import org.hl7.fhir.dstu3.model.*;
import org.openehr.rm.datatypes.text.DvCodedText;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.DatatypeConverter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Date;

public class ConditionConverter {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Converts the given json coming from openEHR into 1 {@link Condition} resource.
     * Duplicates in the json will be merged.
     *
     * @param ehrJson is the array contained inside the "resultSet" section
     */
    public ConditionCC convertToCondition(JsonNode ehrJson) {
        List<ConditionCC> list = convertToConditionList(ehrJson);
        return list.get(0);
    }

    /**
     * Converts the given json coming from openEHR into a list of {@link Condition} resources.
     * Duplicates in the json will be merged.
     *
     * @param ehrJson is the array contained inside the "resultSet" section
     */
    public List<ConditionCC> convertToConditionList(JsonNode ehrJson) {
        List<ConditionCC> profiles = new ArrayList<>();
        Iterator<JsonNode> it = ehrJson.elements();
        while (it.hasNext()) {
            ConditionCC conditionResource = createConditionResource(it.next());
            profiles.add(conditionResource);
        }
        return profiles;
    }

    private ConditionCC createConditionResource(JsonNode ehrJson) {

        ConditionCC retVal = new ConditionCC();

        retVal.setId(convertResourceId(ehrJson));
        retVal.setSubject(convertPatientReference(ehrJson));
        retVal.setAssertedDate(convertAssertedDate(ehrJson));
        retVal.setAsserter(convertAsserterReference(ehrJson));

        retVal.setCode(convertScalarCodableConcept(ehrJson,"Problem_Diagnosis"));

        retVal.addBodySite(convertScalarCodableConcept(ehrJson,"Body_site"));

        //retVal.setOnset(Condition.ONSET_DATE);

//        CodeableConcept category = new CodeableConcept();
//        category.addCoding();

//        retVal.addCategory(new CodeableConcept());

        //retVal.setAbatement();


        //retVal.setSeverity();

        retVal.setClinicalStatus(convertConditionClinicalStatus(ehrJson));

        retVal.setVerificationStatus(convertConditionVerificationStatus(ehrJson));

//      retVal.setOnset(Condition.ONSET_DATE);


        JsonNode comment = ehrJson.get("Comment");
        if (null != comment) {
            retVal.addNote(new Annotation().setText(comment.textValue()));
        }
        return retVal;
    }

    private Condition.ConditionVerificationStatus convertConditionVerificationStatus(JsonNode ehrJson) {

        Condition.ConditionVerificationStatus verificationStatus;
        String diagnostic_certainty_code = null;

        JsonNode diagnosticCertainty = ehrJson.get("Diagnostic_certainty_code");
        if (diagnosticCertainty != null)
            diagnostic_certainty_code = diagnosticCertainty.textValue();

       if ("at0026".equals(diagnostic_certainty_code)) {
            verificationStatus = Condition.ConditionVerificationStatus.PROVISIONAL;
        }
        else if ("at0026".equals(diagnostic_certainty_code)) {
            verificationStatus = Condition.ConditionVerificationStatus.CONFIRMED;
        }
        else if ("at0026".equals(diagnostic_certainty_code)) {
                verificationStatus = Condition.ConditionVerificationStatus.REFUTED;
            }
        else
            verificationStatus = null;

        return verificationStatus;
    }

    private Condition.ConditionClinicalStatus convertConditionClinicalStatus(JsonNode ehrJson) {

        Condition.ConditionClinicalStatus ClinicalStatus;
        String clinical_status_code = null;
        String resolution_code = null;

        JsonNode active_inactive = ehrJson.get("Active_Inactive_code");
        if (active_inactive != null)
            clinical_status_code = active_inactive.textValue();

        JsonNode resolution_node = ehrJson.get("Resolution_status_code");
        if (resolution_node != null)
            resolution_code = resolution_node.textValue();

        if (resolution_code != null && "at0084".equals(resolution_code)){
            ClinicalStatus = Condition.ConditionClinicalStatus.RESOLVED;
        }else if (clinical_status_code != null && "at0026".equals(clinical_status_code)) {
            ClinicalStatus = Condition.ConditionClinicalStatus.ACTIVE;
        }
        else if (clinical_status_code != null && "at0027".equals(clinical_status_code)) {
            ClinicalStatus = Condition.ConditionClinicalStatus.INACTIVE;
        }
        else
            ClinicalStatus = null;

        return ClinicalStatus;
    }


    private Date convertAssertedDate(JsonNode ehrJson) {
        String dateString = ehrJson.get("AssertedDate").textValue();
        if (null == dateString) {
            dateString = ehrJson.get("compositionStartTime").textValue();
        }
        return (DatatypeConverter.parseDateTime(dateString).getTime());
    }

    private Reference convertAsserterReference(JsonNode ehrJson) {
        Reference reference = new Reference();
        String composerName = ehrJson.get("composerName").textValue();
        String composerId = ehrJson.get("composerId").textValue();
        String composerNamespace = ehrJson.get("composerNamespace").textValue();
        String displayString = "";

        //Convert Composer name and ID.

        Practitioner asserter = new Practitioner();
        asserter.setId("#composer");

        String asserterName = ehrJson.get("composerName").textValue();
        if (null != asserterName) {
            asserter.addName().setText(asserterName);
            displayString.concat(asserterName);
        }

        String asserterID = ehrJson.get("composerId").textValue();
        if (null != asserterID) {

            Identifier id = asserter.addIdentifier();
            id.setValue(asserterID);
            displayString.concat(" : " + asserterName);

            String asserterNamespace = ehrJson.get("composerNamespace").textValue();
            if (null != asserterNamespace) {
                id.setSystem(asserterNamespace);
                displayString.concat(" : " + asserterNamespace);

            }
        }

        reference.setDisplay(displayString);

        return reference;
    }

    private Reference convertPatientReference(JsonNode ehrJson) {
        Reference reference = new Reference();
        reference.setReference(ehrJson.get("ehrId").textValue());
        reference.setIdentifier(convertPatientIdentifier(ehrJson));
        return reference;
    }

    private String convertResourceId(JsonNode ehrJson) {
        String entryId = ehrJson.get("entryId").textValue();
        String compositionId = ehrJson.get("compositionId").textValue();
        if (entryId == null)
            return compositionId;
        else {
             return compositionId + "|" + entryId;
        }
    }

    private Identifier convertPatientIdentifier(JsonNode ehrJson) {
        Identifier identifier = new Identifier();
        identifier.setValue(ehrJson.get("subjectId").textValue());
        identifier.setSystem(convertPatientIdentifierSystem(ehrJson));
        return identifier;
    }

    private String convertPatientIdentifierSystem(JsonNode ehrJson) {
        String subjectIdNamespace = ehrJson.get("subjectNamespace").textValue();
        if ("uk.nhs.nhs_number".equals(subjectIdNamespace)) {
            return "https://fhir.nhs.uk/Id/nhs-number";
        } else {
            return subjectIdNamespace;
        }
    }

    private CodeableConcept convertScalarCodableConcept(JsonNode ehrJson, String scalarElementName) {

  //      logger.debug("Scalar Name" + scalarElementName);

        String value = ehrJson.get(scalarElementName+"_value").textValue();
        String terminology = ehrJson.get(scalarElementName+"_terminology").textValue();
        String code = ehrJson.get(scalarElementName+"_code").textValue();

        if (null != terminology && null != code) {
            DvCodedText openEHRCodeable = new DvCodedText(value, terminology, code);
            return DfText.convertToCodeableConcept(openEHRCodeable);
        } else {
            return new CodeableConcept().setText(value);
        }
    }
}
