package com.inidus.platform.fhir.medication;

import ca.uhn.fhir.model.api.annotation.ResourceDef;
import org.hl7.fhir.dstu3.model.Medication;


/**
 * Subclass that represents the Care Connect Medication Profile (currently unconstrained)
 */
@ResourceDef(name = "Medication", profile = "https://fhir.hl7.org.uk/STU3/StructureDefinition/CareConnect-Medication-1")
public class MedicationCC extends Medication {
}


