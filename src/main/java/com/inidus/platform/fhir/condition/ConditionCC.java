package com.inidus.platform.fhir.condition;

import ca.uhn.fhir.model.api.annotation.*;
import ca.uhn.fhir.model.api.annotation.Extension;
import ca.uhn.fhir.util.ElementUtil;
import org.hl7.fhir.dstu3.model.*;


/**
 * Subclass that represents the Care Conect Profile
 */
@ResourceDef(name="Condition", profile="https://fhir.hl7.org.uk/STU3/StructureDefinition/CareConnect-Condition-1")
public class ConditionCC extends Condition {

    @Child(name="episode")
    @Extension(url="https://fhir-test.hl7.org.uk/StructureDefinition/Extension-CareConnect-ConditionEpisode-1", definedLocally=false, isModifier=false)
    @Description(shortDefinition="The episodicity status of a condition")
    protected EpisodeExtension episodeExtension;

    public EpisodeExtension getEpisodeExtension() {
        return episodeExtension;
    }

    @Override
    public boolean isEmpty() {
        return super.isEmpty() && ElementUtil.isEmpty(episodeExtension);
    }

    public void setEpisodeExtension(EpisodeExtension theEpisodeExtension) {
        episodeExtension = theEpisodeExtension;
    }

    @Block
    public static class EpisodeExtension extends BackboneElement {

        @Child(name = "valueCode")
        @Extension(url = "https://fhir-test.hl7.org.uk/StructureDefinition/Extension-CareConnect-ConditionEpisode-1/valueCode", definedLocally = false, isModifier = false)
        private CodeType valueCode;

        @Override
        public EpisodeExtension copy() {
            EpisodeExtension copy = new EpisodeExtension();
            copy.valueCode = valueCode;
            return copy;
        }

        @Override
        public boolean isEmpty() {
            return super.isEmpty() && ElementUtil.isEmpty(valueCode);
        }

        public StringType getValueCode() {
            return valueCode;
        }

        public void setValueCode(CodeType theValueCode) {
            valueCode = theValueCode;
        }

    }


}

