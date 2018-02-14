package com.inidus.platform.fhir.condition;

import com.fasterxml.jackson.databind.JsonNode;
import com.inidus.platform.fhir.openehr.OpenEHRConverter;
import org.hl7.fhir.dstu3.model.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ConditionConverter extends OpenEHRConverter {
    /**
     * Converts the given json coming from openEHR into 1 {@link Condition} resource.
     *
     * @param ehrJson is the array contained inside the "resultSet" section
     */
    public ConditionCC convertToCondition(JsonNode ehrJson) {
        List<ConditionCC> list = convertToConditionList(ehrJson);
        return list.get(0);
    }

    /**
     * Converts the given json coming from openEHR into a list of {@link Condition} resources.
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
        retVal.getAsserter().setResource(convertAsserter(ehrJson));

        retVal.setCode(convertCodeableConcept(ehrJson, "Problem_Diagnosis"));
        retVal.addBodySite(convertCodeableConcept(ehrJson, "Body_site"));

        retVal.addCategory(convertCategory(ehrJson));
        retVal.setSeverity(convertSeverity(ehrJson));
        retVal.setClinicalStatus(convertConditionClinicalStatus(ehrJson));
        retVal.setVerificationStatus(convertConditionVerificationStatus(ehrJson));
        retVal.setEpisodeExtension(convertConditionEpisode(ehrJson));

        JsonNode onsetDate = ehrJson.get("Date_time_of_onset");
        if (null != onsetDate) {
            retVal.setOnset(new DateTimeType(onsetDate.asText(null)));
        }

        JsonNode resolutionDate = ehrJson.get("Date_time_of_resolution");
        if (null != resolutionDate) {
            retVal.setAbatement(new DateTimeType(resolutionDate.textValue()));
        }

        JsonNode comment = ehrJson.get("Comment");
        if (null != comment) {
            retVal.addNote(new Annotation().setText(comment.textValue()));
        }
        return retVal;
    }

    private CodeableConcept convertCategory(JsonNode ehrJson) {
        // Hardwire Category to Problem-List-Item for 5N-CDR use
        Coding categoryCoding = new Coding("http://hl7.org/fhir/condition-category", "problem-list-item", "Problem List Item");
        categoryCoding.setUserSelected(true);
        return new CodeableConcept().addCoding(categoryCoding).setText("Problem List Item");
    }


    private CodeableConcept convertSeverity(JsonNode ehrJson) {

        CodeableConcept severity = new CodeableConcept();
        JsonNode severity_element = ehrJson.get("Severity_code");
        String code = null;
        String display = null;

        String severityCode = severity_element.asText(null);

        if (severityCode != null) {
            if ("at0047".equals(severity_element.textValue())) {
                code = "255604002";
                display = "Mild";
            } else if ("at0048".equals(severity_element)) {
                code = "6736007";
                display = "Moderate";
            } else if ("at0049".equals(severity_element)) {
                code = "24484000";
                display = "Severe";
            }

            if (code != null) {
                Coding severityCoding = new Coding("http://snomed.info/sct ", code, display).setUserSelected(true);
                severity.addCoding(severityCoding).setText(display);
            }
        }

        return severity;
    }

    private Condition.ConditionVerificationStatus convertConditionVerificationStatus(JsonNode ehrJson) {

        Condition.ConditionVerificationStatus verificationStatus = null;
        String diagnostic_certainty_code = null;

        JsonNode diagnosticCertainty = ehrJson.get("Diagnostic_certainty_code");

        if (diagnosticCertainty != null)
            diagnostic_certainty_code = diagnosticCertainty.textValue();

        //`local::at0074::Suspected` | `local::at0075::Probable` => `provisional`
        if (null != diagnostic_certainty_code) {
            if (diagnostic_certainty_code.matches("at0074|at0075")) {
                verificationStatus = Condition.ConditionVerificationStatus.PROVISIONAL;
            }
            //`local::at0076::Confirmed` => `confirmed`
            else if ("at0076".equals(diagnostic_certainty_code)) {
                verificationStatus = Condition.ConditionVerificationStatus.CONFIRMED;
            }
        }

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

        if (resolution_code != null && "at0084".equals(resolution_code)) {
            ClinicalStatus = Condition.ConditionClinicalStatus.RESOLVED;
        }
        // Match against active, resolving and not resolving
        else if (clinical_status_code != null && clinical_status_code.matches("at0026|at0085|at0086")) {
            ClinicalStatus = Condition.ConditionClinicalStatus.ACTIVE;
        } else if (clinical_status_code != null && "at0027".equals(clinical_status_code)) {
            ClinicalStatus = Condition.ConditionClinicalStatus.INACTIVE;
        } else
            ClinicalStatus = null;

        return ClinicalStatus;
    }

    private EpisodeExtension convertConditionEpisode(JsonNode ehrJson) {

        EpisodeExtension episode = new EpisodeExtension();

        CodeType episodeCode = new CodeType();

        String episodicity_code = ehrJson.get("Episodicity_code").asText(null);
        Boolean firstOccurence = ehrJson.get("First_occurrence").asBoolean(false);

        if ("at0034".equals(episodicity_code)) {
            if (firstOccurence)
                episodeCode.setValue("First");
            else
                episodeCode.setValue("New");
        } else if ("at0035".equals(episodicity_code)) {
            episodeCode.setValue("Review");
        } else
            episodeCode = null;

        if (episodeCode != null) {
            episode.setValueCode(episodeCode);
        }

        return episode;
    }
}
