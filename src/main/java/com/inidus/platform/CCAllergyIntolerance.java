package com.inidus.platform;

import ca.uhn.fhir.model.api.annotation.ResourceDef;
import org.hl7.fhir.dstu3.model.AllergyIntolerance;


/**
 * Subclass that represents the Care Conect Profile
 */
@ResourceDef(name="AllergyIntolerance", profile="https://fhir.hl7.org.uk/STU3/StructureDefinition/CareConnect-AllergyIntolerance-1")
public class CCAllergyIntolerance extends AllergyIntolerance {
}
