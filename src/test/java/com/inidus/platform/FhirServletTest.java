package com.inidus.platform;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletConfig;

public class FhirServletTest {

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private FhirServlet testImpl;

    @Before
    public void setUp() throws Exception {
        testImpl = new FhirServlet();
        testImpl.init(new MockServletConfig());
        testImpl.initialize();

        request = new MockHttpServletRequest();

        response = new MockHttpServletResponse();
    }

    @Test
    public void allergyIntolerance() throws Exception {
        request.setMethod("GET");
        request.addHeader("Content-Type", "application/json");
        request.setRequestURI("/AllergyIntolerance/1");

        testImpl.service(request, response);

        Assert.assertEquals(response.getContentAsString(), HttpStatus.OK.value(), response.getStatus());
        Assert.assertEquals("application/json+fhir", response.getContentType());

    }

    @Test
    public void allergyIntolerance_patientRefPresent() throws Exception {
        request.setMethod("GET");
        request.addHeader("Content-Type", "application/json");
        request.setRequestURI("/AllergyIntolerance/1");

        testImpl.service(request, response);

        JSONObject json = new JSONObject(response.getContentAsString());

        Assert.assertNotNull(json.getJSONObject("patient").getString("display"));
    }

    @Test
    public void allergyIntolerance_codedTextPresent() throws Exception {
        request.setMethod("GET");
        request.addHeader("Content-Type", "application/json");
        request.setRequestURI("/AllergyIntolerance/1");

        testImpl.service(request, response);

        JSONObject json = new JSONObject(response.getContentAsString());

        Assert.assertNotNull(json.getJSONObject("code").getString("text"));
    }
}