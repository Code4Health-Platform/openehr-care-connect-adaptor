package com.inidus.platform.conversion;

import com.fasterxml.jackson.databind.JsonNode;
import com.inidus.platform.CCAllergyIntolerance;
import org.hl7.fhir.dstu3.model.*;
import org.openehr.rm.datatypes.text.DvCodedText;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.DatatypeConverter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class OpenEhrConverter {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Converts the given json coming from openEHR into 1 {@link AllergyIntolerance} resource.
     * Duplicates in the json will be merged.
     *
     * @param ehrJson is the array contained inside the "resultSet" section
     */
    public CCAllergyIntolerance convertToAllergyIntolerance(JsonNode ehrJson) {
        List<CCAllergyIntolerance> list = convertToAllergyIntoleranceList(ehrJson);
        return list.get(0);
    }

    /**
     * Converts the given json coming from openEHR into a list of {@link AllergyIntolerance} resources.
     * Duplicates in the json will be merged.
     *
     * @param ehrJson is the array contained inside the "resultSet" section
     */
    public List<CCAllergyIntolerance> convertToAllergyIntoleranceList(JsonNode ehrJson) {
        List<CCAllergyIntolerance> profiles = new ArrayList<>();
        Iterator<JsonNode> it = ehrJson.elements();
        while (it.hasNext()) {
            CCAllergyIntolerance allergyResource = createAllergyResource(it.next());
            profiles.add(allergyResource);
        }
        return profiles;
    }

    private CCAllergyIntolerance createAllergyResource(JsonNode ehrJson) {
        CCAllergyIntolerance retVal = new CCAllergyIntolerance();

        retVal.setId(ehrJson.get("compositionId").textValue() + "_" + ehrJson.get("entryId").textValue());
        retVal.setClinicalStatus(AllergyIntolerance.AllergyIntoleranceClinicalStatus.ACTIVE);

        String statusCode = ehrJson.get("Status_code").textValue();
        if ("at0065".equals(statusCode)) {
            retVal.setVerificationStatus(AllergyIntolerance.AllergyIntoleranceVerificationStatus.CONFIRMED);
        } else if ("at0127".equals(statusCode)) {
            retVal.setVerificationStatus(AllergyIntolerance.AllergyIntoleranceVerificationStatus.UNCONFIRMED);
        } else {
            retVal.setVerificationStatus(AllergyIntolerance.AllergyIntoleranceVerificationStatus.UNCONFIRMED);
        }


        String mechanism_code = ehrJson.get("Reaction_mechanism_code").textValue();
        if ("at0059".equals(mechanism_code)) {
            retVal.setType(AllergyIntolerance.AllergyIntoleranceType.ALLERGY);
        } else if ("at0060".equals(mechanism_code)) {
            retVal.setType(AllergyIntolerance.AllergyIntoleranceType.INTOLERANCE);
        }

        String category_code = ehrJson.get("Category_code").textValue();
        AllergyIntolerance.AllergyIntoleranceCategory category = AllergyIntoleranceCategory.convertToFhir(category_code);
        if (null != category) {
            retVal.addCategory(category);
        }

        String criticality_code = ehrJson.get("Criticality_code").textValue();
        if ("at0102".equals(criticality_code)) {
            retVal.setCriticality(AllergyIntolerance.AllergyIntoleranceCriticality.LOW);
        } else if ("at0103".equals(criticality_code)) {
            retVal.setCriticality(AllergyIntolerance.AllergyIntoleranceCriticality.HIGH);
        } else if ("at0124".equals(criticality_code)) {
            retVal.setCriticality(AllergyIntolerance.AllergyIntoleranceCriticality.UNABLETOASSESS);
        }

        retVal.setCode(convertCausativeAgent(ehrJson));

        Reference patient = new Reference();
        patient.setReference("Patient/" + ehrJson.get("ehrId").textValue());
        patient.setIdentifier(convertPatientIdentifier(ehrJson));
        retVal.setPatient(patient);


        String onset_of_last_reaction = ehrJson.get("Onset_of_last_reaction").textValue();
        if (null != onset_of_last_reaction) {
            retVal.setLastOccurrence(DatatypeConverter.parseDateTime(onset_of_last_reaction).getTime());
        }

        String dateString = ehrJson.get("Adverse_reaction_risk_Last_updated").textValue();
        if (null != dateString) {
            retVal.setAssertedDate(DatatypeConverter.parseDateTime(dateString).getTime());
        } else {
            dateString = ehrJson.get("compositionStartTime").textValue();
            if (null != dateString) {
                retVal.setAssertedDate(DatatypeConverter.parseDateTime(dateString).getTime());
            }
        }

        JsonNode comment = ehrJson.get("Comment");
        if (null != comment) {
            retVal.addNote(new Annotation().setText(comment.textValue()));
        }

        AllergyIntolerance.AllergyIntoleranceReactionComponent reaction = new AllergyIntolerance.AllergyIntoleranceReactionComponent();

        reaction.setSubstance(convertSpecificSubstance(ehrJson));

        reaction.addManifestation(convertManifestation(ehrJson));

        reaction.setDescription(ehrJson.get("Reaction_description").textValue());

        String onset_of_reaction = ehrJson.get("Onset_of_reaction").textValue();
        if (null != onset_of_last_reaction) {
            reaction.setOnset(DatatypeConverter.parseDateTime(onset_of_reaction).getTime());
        }

        String severity_code = ehrJson.get("Severity_code").textValue();
        if ("at0093".equals(severity_code)) {
            reaction.setSeverity(AllergyIntolerance.AllergyIntoleranceSeverity.MILD);
        } else if ("at0092".equals(severity_code)) {
            reaction.setSeverity(AllergyIntolerance.AllergyIntoleranceSeverity.MODERATE);
        } else if ("at0090".equals(severity_code)) {
            reaction.setSeverity(AllergyIntolerance.AllergyIntoleranceSeverity.SEVERE);
        }

        reaction.setExposureRoute(convertExposureRoute(ehrJson));

        reaction.addNote().setText(ehrJson.get("Adverse_reaction_risk_Comment").textValue());

        retVal.addReaction(reaction);

        return retVal;
    }

    private Identifier convertPatientIdentifier(JsonNode ehrJson) {
        Identifier identifier = new Identifier();
        identifier.setValue(ehrJson.get("subjectId").textValue());
        identifier.setSystem(convertPatientIdentifierSystem(ehrJson.get("subjectNamespace").textValue()));
        return identifier;
    }

    private String convertPatientIdentifierSystem(String subjectIdNamespace) {
        if ("uk.nhs.nhs_number".equals(subjectIdNamespace)) {
            return "https://fhir.nhs.uk/Id/nhs-number";
        } else {
            return subjectIdNamespace;
        }
    }

    private CodeableConcept convertCausativeAgent(JsonNode ehrJson) {
        String value = ehrJson.get("Causative_agent_value").textValue();
        String terminology = ehrJson.get("Causative_agent_terminology").textValue();
        String code = ehrJson.get("Causative_agent_code").textValue();

        if (null != terminology && null != code) {
            DvCodedText causativeAgent = new DvCodedText(value, terminology, code);
            return DfText.convertToCodeableConcept(causativeAgent);
        } else {
            return new CodeableConcept().setText(value);
        }
    }

    private CodeableConcept convertManifestation(JsonNode ehrJson) {
        String value = ehrJson.get("Manifestation_value").textValue();
        String terminology = ehrJson.get("Manifestation_terminology").textValue();
        String code = ehrJson.get("Manifestation_code").textValue();

        if (null != terminology && null != code) {
            DvCodedText causativeAgent = new DvCodedText(value, terminology, code);
            return DfText.convertToCodeableConcept(causativeAgent);
        } else {
            return new CodeableConcept().setText(value);
        }
    }

    private CodeableConcept convertSpecificSubstance(JsonNode ehrJson) {
        String value = ehrJson.get("Specific_substance_value").textValue();
        String terminology = ehrJson.get("Specific_substance_terminology").textValue();
        String code = ehrJson.get("Specific_substance_code").textValue();

        if (null != terminology && null != code) {
            DvCodedText substance = new DvCodedText(value, terminology, code);
            return DfText.convertToCodeableConcept(substance);
        } else {
            return new CodeableConcept().setText(value);
        }
    }

    private CodeableConcept convertExposureRoute(JsonNode ehrJson) {
        String value = ehrJson.get("Route_of_exposure_value").textValue();
        String terminology = ehrJson.get("Route_of_exposure_terminology").textValue();
        String code = ehrJson.get("Route_of_exposure_code").textValue();

        if (null != terminology && null != code) {
            DvCodedText routeOfExposure = new DvCodedText(value, terminology, code);
            return DfText.convertToCodeableConcept(routeOfExposure);
        } else {
            return new CodeableConcept().setText(value);
        }
    }
}
