package com.inidus.fhir.condition;

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

        retVal.setCode(convertScalarCodableConcept(ehrJson,"ProblemDiagnosis"));
        CodeableConcept bodySite = convertScalarCodableConcept(ehrJson,"BodySite");
        retVal.addBodySite(bodySite);

        retVal.setClinicalStatus(Condition.ConditionClinicalStatus.ACTIVE);

        retVal.setVerificationStatus(Condition.ConditionVerificationStatus.CONFIRMED);

//        retVal.setOnset(Condition.ONSET_DATE);


//        String onset_of_last_reaction = ehrJson.get("Onset_of_last_reaction").textValue();
//        if (null != onset_of_last_reaction) {
//            retVal.setLastOccurrence(DatatypeConverter.parseDateTime(onset_of_last_reaction).getTime());
//        }


        JsonNode comment = ehrJson.get("Comment");
        if (null != comment) {
            retVal.addNote(new Annotation().setText(comment.textValue()));
        }


//        String severity_code = ehrJson.get("Severity_code").textValue();
//        if ("at0093".equals(severity_code)) {
//            reaction.setSeverity(Condition.ConditionSeverity.MILD);
//        } else if ("at0092".equals(severity_code)) {
//            reaction.setSeverity(Condition.ConditionSeverity.MODERATE);
//        } else if ("at0090".equals(severity_code)) {
//            reaction.setSeverity(Condition.ConditionSeverity.SEVERE);
//        }


        return retVal;
    }


    private Date convertAssertedDate(JsonNode ehrJson) {
        String dateString = ehrJson.get("AssertedDate").textValue();
        if (null != dateString) {
            dateString = ehrJson.get("compositionStartTime").textValue();
        }
        return (DatatypeConverter.parseDateTime(dateString).getTime());
    }


    private Reference convertAsserterReference(JsonNode ehrJson) {
        Reference reference = new Reference();
 //       reference.setReference(ehrJson.get("ehrId").textValue());
 //       reference.setIdentifier(convertIdentifier(ehrJson));
        return reference;
//        String composerName = ehrJson.get("composerName").textValue();
//        if (null != composerName) {
//            composerName = ehrJson.get("compositionStartTime").textValue();
//        }
//        return (DatatypeConverter.parseDateTime(dateString).getTime());
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
             return compositionId + "_" + entryId;
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
