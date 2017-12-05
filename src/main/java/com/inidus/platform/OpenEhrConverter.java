package com.inidus.platform;

import com.fasterxml.jackson.databind.JsonNode;
import org.hl7.fhir.dstu3.model.*;
import org.openehr.rm.datatypes.text.DvCodedText;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class OpenEhrConverter {
    Map<String, AllergyIntolerance> items;

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

        JsonNode causative_agent = ehrJson.get("Causative_agent");
        String value = causative_agent.get("value").textValue();
        String terminology = causative_agent.get("defining_code").get("terminology_id").get("value").textValue();
        String code = causative_agent.get("defining_code").get("code_string").textValue();
        DvCodedText causativeAgent = new DvCodedText(value, terminology, code);

        CodeableConcept concept = DfText.convertToCodeableConcept(causativeAgent);
        retVal.setCode(concept);

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

        if (ehrJson.has("Specific_substance") && ehrJson.get("Specific_substance").has("value")) {
            String substance = ehrJson.get("Specific_substance").get("value").textValue();
            reaction.setSubstance(new CodeableConcept().setText(substance));
        }

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
        
        if (ehrJson.has("Route_of_exposure") && ehrJson.get("Route_of_exposure").has("value")) {
            reaction.setExposureRoute(new CodeableConcept().setText(ehrJson.get("Route_of_exposure").get("value").textValue()));
        }

        reaction.addNote().setText(ehrJson.get("Adverse_reaction_risk_Comment").textValue());

        retVal.addReaction(reaction);
    }
}
