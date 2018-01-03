package com.inidus.platform.fhir;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class App {
    @Autowired()
    private FhirServlet servlet;

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

    @Bean
    public ServletRegistrationBean registerServlet() {
        ServletRegistrationBean bean = new ServletRegistrationBean(servlet, "/fhir/*");
        bean.setLoadOnStartup(1);
        return bean;
    }
}
