package com.inidus.platform.fhir.procedure;

import ca.uhn.fhir.model.api.annotation.ResourceDef;
import org.hl7.fhir.dstu3.model.Procedure;


/**
 * Subclass that represents the Care Connect Procedure Profile (currently unconstrained)
 */
@ResourceDef(name="Procedure", profile="https://fhir.hl7.org.uk/STU3/StructureDefinition/CareConnect-Procedure-1")
public class ProcedureCC extends Procedure {
    }


