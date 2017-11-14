package com.inidus.platform;

import org.hl7.fhir.dstu3.model.AllergyIntolerance;
import org.hl7.fhir.dstu3.model.IdType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MockMvcBuilder;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

public class AllergyProviderTest {
    @Test
    public void getResourceById_VerifyPatientDataPresent() throws Exception {
        AllergyIntolerance returnedValue = new AllergyProvider().getResourceById(new IdType("1"));

        if (null != returnedValue) {
            Assert.assertNotNull(returnedValue.getPatient());
            Assert.assertNotNull(returnedValue.getPatient().getDisplay());
        }
    }

    @Test
    public void getResourceById_VerifyCodeTextPresent() throws Exception {
        AllergyIntolerance returnedValue = new AllergyProvider().getResourceById(new IdType("1"));

        if (null != returnedValue) {
            Assert.assertNotNull(returnedValue.getCode());
            Assert.assertNotNull(returnedValue.getCode().getText());
        }
    }
}