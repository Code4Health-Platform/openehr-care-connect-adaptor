package com.inidus.platform.fhir.medication;

import ca.uhn.fhir.model.api.annotation.*;
import ca.uhn.fhir.model.api.annotation.Extension;
import ca.uhn.fhir.util.ElementUtil;
import org.hl7.fhir.dstu3.model.*;


/**
 * Subclass that represents the Care Conect Profile
 */
@ResourceDef(name="MedicationStatement", profile="https://fhir.hl7.org.uk/STU3/StructureDefinition/CareConnect-MedicationStatement-1")
public class MedicationStatementCC extends MedicationStatement {

}

