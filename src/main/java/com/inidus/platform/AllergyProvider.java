package com.inidus.platform;

import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.server.IResourceProvider;
import org.hl7.fhir.dstu3.model.AllergyIntolerance;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;


public class AllergyProvider implements IResourceProvider {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return AllergyIntolerance.class;
    }

    @Read()
    public AllergyIntolerance getResourceById(@IdParam IdType id) {
        log.trace("getResourceById: " + id.getIdPart() + " " + id.getValue() + " " + id.getIdPartAsLong() );
        AllergyIntolerance allergy = new AllergyIntolerance();
        allergy.addIdentifier().setValue(id.getIdPart());

        Reference patientReference = new Reference();
        patientReference.setDisplay("Dummy Patient");
        allergy.setPatient(patientReference);

        CodeableConcept concept = new CodeableConcept();
        concept.setText("Codeable Concept Text");
        allergy.setCode(concept);

        return allergy;
    }

    @Search()
    public List<AllergyIntolerance> getAllResources() {
        return new ArrayList<>();
    }
}
