package com.inidus.platform.fhir.openehr;


import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

@ConfigurationProperties(prefix = "cdr-connector", ignoreUnknownFields = false)

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {OpenEHRConverter.class})
public class OpenEHRConverterTest {
    @Autowired
    @Qualifier("openEHRConverter")
    private OpenEHRConverter testConverter;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Before
    public void setUp() throws Exception {
        //     configureCdrConnector("http://178.62.71.220:8080", "guest", "guest", true);
 //       configureCdrConnector("https://test.operon.systems", "oprn_hcbox", "XioTAJoO479", true);
    }

    private JsonNode getJsonfromFile(String filePath) throws IOException {
        Path path = Paths.get("./src/test/resources/instance/" +  filePath);
        return new ObjectMapper().readTree(Files.lines(path).collect(Collectors.joining()));
    }

    @Test
     public void convertCodeableConceptDvCodedText() throws Exception {

        JsonNode resultJson = getJsonfromFile("convertCodeableConceptDvCodedText.json");
        CodeableConcept result = testConverter.convertCodeableConcept(resultJson, "Manifestation");
        Assert.assertEquals("Vomiting", result.getText());
        Assert.assertEquals("422400008", result.getCodingFirstRep().getCode());
        Assert.assertEquals("Vomiting", result.getCodingFirstRep().getDisplay());
        Assert.assertEquals("http://snomed.info/sct", result.getCodingFirstRep().getSystem());
        Assert.assertTrue("User selected = true", result.getCodingFirstRep().getUserSelected());

    }

//    @Test
//    public void convertCodeableConceptList() throws Exception {
//
//        JsonNode resultJson = getJsonfromFile("convertCodeableConceptList.json");
//        List<CodeableConcept> result = testConverter.convertCodeableConceptList(resultJson, "Manifestation");
//        Assert.assertEquals("Vomiting", result.get(0).getText());
//        Assert.assertEquals("422400008", result.get(0).getCodingFirstRep().getCode());
//        Assert.assertEquals("Vomiting", result.get(0).getCodingFirstRep().getDisplay());
//        Assert.assertEquals("http://snomed.info/sct", result.get(0).getCodingFirstRep().getSystem());
//        Assert.assertTrue("User selected = true", result.get(0).getCodingFirstRep().getUserSelected());
//
//        Assert.assertEquals("Vomiting", result.get(1).getText());
//        Assert.assertTrue("Coding absent", (result.get(1).getCoding().size() == 0));
//    }

    @Test
    public void convertCodeableConceptDvText() throws Exception {

        JsonNode resultJson = getJsonfromFile("convertCodeableConceptDvText.json");

        CodeableConcept result = testConverter.convertCodeableConcept(resultJson, "Manifestation");
        Assert.assertEquals("Vomiting", result.getText());
        Assert.assertTrue("Coding absent", (result.getCoding().size() == 0));

    }
}