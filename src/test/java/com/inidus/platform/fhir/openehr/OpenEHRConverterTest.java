package com.inidus.platform.fhir.openehr;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.nio.file.Files;
import java.nio.file.Paths;

@ConfigurationProperties(prefix = "cdr-connector", ignoreUnknownFields = false)

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {OpenEHRConverter.class})
public class OpenEHRConverterTest {
    @Autowired
    @Qualifier("openEHRConverter")
    private OpenEHRConverter testConverter;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    //     configureCdrConnector("http://178.62.71.220:8080", "guest", "guest", true);

    //       configureCdrConnector("https://test.operon.systems", "oprn_hcbox", "XioTAJoO479", true);

    private String resourcesRootPath;


    @Before
    public void setUp() throws Exception {
      SetupResourcesPath();
    }

    private void SetupResourcesPath() throws Exception{

        resourcesRootPath = getClass()
                .getClassLoader()
                .getResource(".")
                .toURI() // to deal with spaces in path
                .getPath();
    }

    private String readFileContent(String filePath) throws Exception{
        byte[] content = Files.readAllBytes(Paths.get( filePath));
        return new String(content);
    }

    private JsonNode getJsonNodeFromResourceFile(String filePath) throws Exception {
     return new ObjectMapper().readTree(readFileContent(resourcesRootPath + "instance/" +filePath));
    }

    @Test
     public void convertCodeableConceptDvCodedText() throws Exception {

        JsonNode resultJson = getJsonNodeFromResourceFile("convertCodeableConceptDvCodedText.json");
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

        JsonNode resultJson = getJsonNodeFromResourceFile("convertCodeableConceptDvText.json");

        CodeableConcept result = testConverter.convertCodeableConcept(resultJson, "Manifestation");
        Assert.assertEquals("Vomiting", result.getText());
        Assert.assertTrue("Coding absent", (result.getCoding().size() == 0));

    }
}