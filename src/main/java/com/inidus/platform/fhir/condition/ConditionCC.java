package com.inidus.platform.fhir.condition;

import ca.uhn.fhir.model.api.annotation.Child;
import ca.uhn.fhir.model.api.annotation.Description;
import ca.uhn.fhir.model.api.annotation.Extension;
import ca.uhn.fhir.model.api.annotation.ResourceDef;
import ca.uhn.fhir.util.ElementUtil;
import org.hl7.fhir.dstu3.model.Condition;


/**
 * Subclass that represents the Care Conect Profile
 */
@ResourceDef(name = "Condition", profile = "https://fhir.hl7.org.uk/STU3/StructureDefinition/CareConnect-Condition-1")
public class ConditionCC extends Condition {
    @Child(name = "episode")
    @Extension(url = "https://fhir-test.hl7.org.uk/StructureDefinition/Extension-CareConnect-ConditionEpisode-1", definedLocally = false, isModifier = false)
    @Description(shortDefinition = "The episodicity status of a condition")
    protected EpisodeExtension episodeExtension;

    EpisodeExtension getEpisodeExtension() {
        return episodeExtension;
    }

    void setEpisodeExtension(EpisodeExtension episodeExtension) {
        this.episodeExtension = episodeExtension;
    }

    @Override
    public boolean isEmpty() {
        return super.isEmpty() && ElementUtil.isEmpty(episodeExtension);
    }
}

