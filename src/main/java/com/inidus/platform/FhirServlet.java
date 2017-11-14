package com.inidus.platform;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.RestfulServer;
import ca.uhn.fhir.rest.server.interceptor.ResponseHighlighterInterceptor;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import java.util.ArrayList;
import java.util.List;

@WebServlet(urlPatterns = {"/fhir/**"}, displayName = "FHIR Adaptor")
public class FhirServlet extends RestfulServer {
    public FhirServlet() {
        super(FhirContext.forDstu3());
    }

    @Override
    protected void initialize() throws ServletException {
        super.initialize();

        LoggerFactory.getLogger(getClass()).info("Initialising FHIR Servlet");

        List<IResourceProvider> providers = new ArrayList<>();
        providers.add(new AllergyProvider());
        setResourceProviders(providers);

        registerInterceptor(new ResponseHighlighterInterceptor());
        setDefaultPrettyPrint(true);
    }
}