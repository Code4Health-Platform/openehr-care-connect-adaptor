package com.inidus.platform.fhir.condition;

import ca.uhn.fhir.model.api.annotation.Block;
import ca.uhn.fhir.model.api.annotation.Child;
import ca.uhn.fhir.model.api.annotation.Extension;
import ca.uhn.fhir.util.ElementUtil;
import org.hl7.fhir.dstu3.model.BackboneElement;
import org.hl7.fhir.dstu3.model.CodeType;
import org.hl7.fhir.dstu3.model.StringType;

@Block
public class EpisodeExtension extends BackboneElement {
    @Child(name = "valueCode")
    @Extension(url = "https://fhir-test.hl7.org.uk/StructureDefinition/Extension-CareConnect-ConditionEpisode-1/valueCode", definedLocally = false, isModifier = false)
    private CodeType valueCode;

    @Override
    public EpisodeExtension copy() {
        EpisodeExtension copy = new EpisodeExtension();
        copy.setValueCode(valueCode);
        return copy;
    }

    @Override
    public boolean isEmpty() {
        return super.isEmpty() && ElementUtil.isEmpty(valueCode);
    }

    public StringType getValueCode() {

        return valueCode;
    }

    public void setValueCode(CodeType valueCode) {

        this.valueCode = valueCode;
    }

}
