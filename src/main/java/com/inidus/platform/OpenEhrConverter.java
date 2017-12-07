package com.inidus.platform;

import com.fasterxml.jackson.databind.JsonNode;
import org.hl7.fhir.dstu3.model.*;
import org.openehr.rm.datatypes.text.DvCodedText;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class OpenEhrConverter {

    /**
     * Converts the given json coming from openEHR into 1 {@link AllergyIntolerance} resource.
     * Duplicates in the json will be merged.
     *
     * @param ehrJson is the array contained inside the "resultSet" section
     */
    public AllergyIntolerance convertToAllergyIntolerance(JsonNode ehrJson) {
        List<AllergyIntolerance> list = convertToAllergyIntoleranceList(ehrJson);
        return list.get(0);
    }

    /**
     * Converts the given json coming from openEHR into a list of {@link AllergyIntolerance} resources.
     * Duplicates in the json will be merged.
     *
     * @param ehrJson is the array contained inside the "resultSet" section
     */
    public List<AllergyIntolerance> convertToAllergyIntoleranceList(JsonNode ehrJson) {
        List<AllergyIntolerance> profiles = new ArrayList<>();
        Iterator<JsonNode> it = ehrJson.elements();
        while (it.hasNext()) {
            JsonNode jsonNode = it.next();
            AllergyIntolerance allergyIntolerance = new AllergyIntolerance();
            populateAllergyResource(allergyIntolerance, jsonNode);
            profiles.add(allergyIntolerance);
        }

        return profiles;
    }

    private void populateAllergyResource(AllergyIntolerance retVal, JsonNode ehrJson) {
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
        if ("at0121".equals(category_code)) {
            retVal.addCategory(AllergyIntolerance.AllergyIntoleranceCategory.FOOD);
        } else if ("at0122".equals(category_code)) {
            retVal.addCategory(AllergyIntolerance.AllergyIntoleranceCategory.MEDICATION);
        } else if ("at0123".equals(category_code)) {
            retVal.addCategory(AllergyIntolerance.AllergyIntoleranceCategory.ENVIRONMENT);
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
        patient.setDisplay("Dummy Patient");
        patient.setReference(ehrJson.get("ehrId").textValue());
        Identifier identifier = new Identifier();
        identifier.setValue(ehrJson.get("subjectId").textValue());
        identifier.setSystem(ehrJson.get("subjectNamespace").textValue());
        patient.setIdentifier(identifier);
        retVal.setPatient(patient);

        SimpleDateFormat ehrDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX");

        try {
            String onset_of_last_reaction = ehrJson.get("Onset_of_last_reaction").asText();
            retVal.setLastOccurrence(ehrDateFormat.parse(onset_of_last_reaction));
        } catch (ParseException e) {
        }

        try {
            retVal.setAssertedDate(ehrDateFormat.parse(ehrJson.get("Adverse_reaction_risk_Last_updated").asText()));
        } catch (ParseException e) {
        }

        JsonNode comment = ehrJson.get("Comment");
        if (null != comment) {
            retVal.addNote(new Annotation().setText(comment.textValue()));
        }

        AllergyIntolerance.AllergyIntoleranceReactionComponent reaction = new AllergyIntolerance.AllergyIntoleranceReactionComponent();

        reaction.setSubstance(convertSpecificSubstance(ehrJson));

        reaction.addManifestation(new CodeableConcept().setText("Manifestation_value"));
        reaction.setDescription(ehrJson.get("Reaction_description").textValue());

        try {
            reaction.setOnset(ehrDateFormat.parse(ehrJson.get("Onset_of_reaction").asText()));
        } catch (ParseException e) {
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
