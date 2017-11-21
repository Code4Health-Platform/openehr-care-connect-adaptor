package com.inidus.platform;

import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.server.IResourceProvider;
import com.fasterxml.jackson.databind.JsonNode;
import org.hl7.fhir.dstu3.model.*;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

@Component("AllergyProvider")
public class AllergyProvider implements IResourceProvider {
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private OpenEhrService openEhrService;

    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return AllergyIntolerance.class;
    }

    @Read()
    public AllergyIntolerance getResourceById(@IdParam IdType id) throws ParseException {
        log.trace("getResourceById: " + id.getIdPart() + " " + id.getValue() + " " + id.getIdPartAsLong());
        AllergyIntolerance retVal = new AllergyIntolerance();

        JsonNode ehrJson = null;
        try {
            ehrJson = openEhrService.getAllergyById(id.getIdPart());
        } catch (IOException e) {
            return null;
        }

        retVal.addIdentifier().setValue(id.getIdPart());
        retVal.setId(ehrJson.get("compositionId").textValue() + "_" + ehrJson.get("entryId").textValue());
        retVal.setClinicalStatus(AllergyIntolerance.AllergyIntoleranceClinicalStatus.ACTIVE);

        retVal.setVerificationStatus("at0065".equals(ehrJson.get("Status_code").textValue())
                ? AllergyIntolerance.AllergyIntoleranceVerificationStatus.CONFIRMED
                : AllergyIntolerance.AllergyIntoleranceVerificationStatus.UNCONFIRMED);

        String mechanism_code = ehrJson.get("Reaction_mechanism_code").textValue();
        if ("at0059".equals(mechanism_code)) {
            retVal.setType(AllergyIntolerance.AllergyIntoleranceType.ALLERGY);
        } else if ("at0060".equals(mechanism_code)) {
            retVal.setType(AllergyIntolerance.AllergyIntoleranceType.INTOLERANCE);
        } else if ("at0121".equals(mechanism_code)) {
            retVal.addCategory(AllergyIntolerance.AllergyIntoleranceCategory.FOOD);
        } else if ("at0122".equals(mechanism_code)) {
            retVal.addCategory(AllergyIntolerance.AllergyIntoleranceCategory.MEDICATION);
        } else if ("at0123".equals(mechanism_code)) {
            retVal.addCategory(AllergyIntolerance.AllergyIntoleranceCategory.ENVIRONMENT);
        } else {
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


        String terminologySystem = ehrJson.get("Causative_agent").get("defining_code").get("terminology_id").get("value").textValue();
        CodeableConcept code = new CodeableConcept();
        code.setText(ehrJson.get("Causative_agent").get("value").textValue());
        retVal.setCode(code);


        Reference patient = new Reference();
        patient.setDisplay("Dummy Patient");
        patient.setReference(ehrJson.get("ehrId").textValue());
        Identifier identifier = new Identifier();
        identifier.setValue(ehrJson.get("subjectId").textValue());
        identifier.setSystem(ehrJson.get("subjectNamespace").textValue());
        patient.setIdentifier(identifier);
        retVal.setPatient(patient);

        SimpleDateFormat ehrDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX");
        retVal.setLastOccurrence(ehrDateFormat.parse(ehrJson.get("Onset_of_last_reaction").textValue()));

        retVal.setAssertedDate(ehrDateFormat.parse(ehrJson.get("Adverse_reaction_risk_Last_updated").asText()));

        JsonNode comment = ehrJson.get("Comment");
        Annotation note = new Annotation();
        if (null != comment) {
            note.setText(comment.textValue());
        } else {
            note.setText("n/a");
        }
        retVal.addNote(note);

        String substance = ehrJson.get("Specific_substance").get("value").textValue();
        AllergyIntolerance.AllergyIntoleranceReactionComponent reaction = new AllergyIntolerance.AllergyIntoleranceReactionComponent();
        CodeableConcept codeableSubstance = new CodeableConcept();
        codeableSubstance.setText(substance);
        reaction.setSubstance(codeableSubstance);
        reaction.addManifestation(new CodeableConcept().setText("Manifestation_value"));
        reaction.setDescription(ehrJson.get("Reaction_description").textValue());
        reaction.setOnset(ehrDateFormat.parse(ehrJson.get("Onset_of_reaction").asText()));

        String severity_code = ehrJson.get("Severity_code").textValue();
        if ("at0093".equals(severity_code)) {
            reaction.setSeverity(AllergyIntolerance.AllergyIntoleranceSeverity.MILD);
        } else if ("at0092".equals(severity_code)) {
            reaction.setSeverity(AllergyIntolerance.AllergyIntoleranceSeverity.MODERATE);
        } else if ("at0090".equals(severity_code)) {
            reaction.setSeverity(AllergyIntolerance.AllergyIntoleranceSeverity.SEVERE);
        }

        reaction.setExposureRoute(new CodeableConcept().setText(ehrJson.get("Route_of_exposure").get("value").textValue()));

        reaction.addNote().setText(ehrJson.get("Adverse_reaction_risk_Comment").textValue());

        retVal.addReaction(reaction);

        return retVal;
    }

    @Search()
    public List<AllergyIntolerance> getAllResources() throws ParseException {
        ArrayList<AllergyIntolerance> all = new ArrayList<>();
        all.add(getResourceById(new IdType(0)));
        return all;
    }
}
