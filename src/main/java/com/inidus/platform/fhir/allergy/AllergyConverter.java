package com.inidus.platform.fhir.allergy;

import com.fasterxml.jackson.databind.JsonNode;
import com.inidus.platform.fhir.openehr.OpenEHRConverter;
import org.hl7.fhir.dstu3.model.AllergyIntolerance;
import org.hl7.fhir.dstu3.model.Annotation;

import javax.xml.bind.DatatypeConverter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AllergyConverter extends OpenEHRConverter {
    /**
     * Converts the given json coming from openEHR into 1 {@link AllergyIntolerance} resource.
     *
     * @param ehrJson is the array contained inside the "resultSet" section
     */
    public AllergyIntoleranceCC convertToAllergyIntolerance(JsonNode ehrJson) {
        List<AllergyIntoleranceCC> list = convertToAllergyIntoleranceList(ehrJson);
        return list.get(0);
    }

    /**
     * Converts the given json coming from openEHR into a list of {@link AllergyIntolerance} resources.
     *
     * @param ehrJson is the array contained inside the "resultSet" section
     */
    public List<AllergyIntoleranceCC> convertToAllergyIntoleranceList(JsonNode ehrJson) {
        List<AllergyIntoleranceCC> profiles = new ArrayList<>();
        Iterator<JsonNode> it = ehrJson.elements();
        while (it.hasNext()) {
            AllergyIntoleranceCC allergyResource = createAllergyResource(it.next());
            profiles.add(allergyResource);
        }
        return profiles;
    }

    private AllergyIntoleranceCC createAllergyResource(JsonNode ehrJson) {
        AllergyIntoleranceCC retVal = new AllergyIntoleranceCC();

        retVal.setId(convertResourceId(ehrJson));
        retVal.setPatient(convertPatientReference(ehrJson));
        retVal.setAssertedDate(convertAssertedDate(ehrJson));
        retVal.getAsserter().setResource(convertAsserter(ehrJson));


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

        retVal.setCode(convertScalarCodableConcept(ehrJson, "Causative_agent"));

        String onset_of_last_reaction = ehrJson.get("Onset_of_last_reaction").textValue();
        if (null != onset_of_last_reaction) {
            retVal.setLastOccurrence(DatatypeConverter.parseDateTime(onset_of_last_reaction).getTime());
        }

        JsonNode comment = ehrJson.get("Comment");
        if (null != comment) {
            retVal.addNote(new Annotation().setText(comment.textValue()));
        }

        AllergyIntolerance.AllergyIntoleranceReactionComponent reaction = new AllergyIntolerance.AllergyIntoleranceReactionComponent();

        reaction.setSubstance(convertScalarCodableConcept(ehrJson, "Specific_substance"));

        reaction.addManifestation(convertScalarCodableConcept(ehrJson, "Manifestation"));

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

        reaction.setExposureRoute(convertScalarCodableConcept(ehrJson, "Route_of_exposure"));

        reaction.addNote().setText(ehrJson.get("Adverse_reaction_risk_Comment").textValue());

        retVal.addReaction(reaction);

        return retVal;
    }
}
