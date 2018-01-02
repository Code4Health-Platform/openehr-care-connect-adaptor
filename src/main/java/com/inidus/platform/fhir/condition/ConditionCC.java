package com.inidus.fhir.condition;

import ca.uhn.fhir.model.api.annotation.ResourceDef;
import org.hl7.fhir.dstu3.model.Condition;


/**
 * Subclass that represents the Care Conect Profile
 */
@ResourceDef(name="Condition", profile="https://fhir.hl7.org.uk/STU3/StructureDefinition/CareConnect-Condition-1")
public class ConditionCC extends Condition {
    }
}
