
# openEHR-FHIR INTEROpen Care Connect STU3 Adaptor

This is a proof-of-concept Web service that exposes HL7 FHIR operations (read, search, conformance) from a choice of openEHR CDR (Ehrscape compliant e.g. Marand Think!Ehr or EtherCis) for a small range of INTEROpen Care-Connect FHIR profiles, using the [HAPI FHIR](http://hapifhir.io) stack.

## Build the project
`./mvnw clean package` (inside the project folder)

## Deploy
Copy `fhir-adaptor.jar` from `target` folder to your server

### Run with Marand backend
`java -jar fhir-adaptor.jar --spring.profiles.active=marand` (will run on port 8082)

### Run with Ethercis backend
`java -jar fhir-adaptor.jar --spring.profiles.active=ethercis` (will run on port 8083)

## Background

The intent was to create an extensible framework  to support the creation of a FHIR API for any openEHR CDR that supports the EHRscape API - This includes the Marand and etherCIS CDRs used by Code4Health, with other CDR providers likely to follow suit.

The API was built using HAPI-FHIR, an open-source implementation of the FHIR specification in Java. HAPI-FHIR simplifies the building of a FHIR API on any data source and its use simplifies conformance with the FHIR standard as it has already be tested in a number of implementations. It was originally intended to base the project on FHIR DSTU2, but during development it became clear that STU3 was becoming preferred for current INTEROpen Care-connect and GP-Connect profile curation. This curation work is well advanced but not yet stable, so it is ecpected that further revisions ot the mappings will be required as stable, published versions emerge.

The following FHIR Interactions are supported

- `Read`
- `Search`
- `Conformance`

However, the framework will allow the addition of support for other FHIR API interactions should use cases emerge that require these.

### openEHR Mappings to support INTEROPen Care-Connect Profiles

In order to make use of the framework. It was necessary to ensure that the underlying openEHR archetypes support the data-points in the INTEROPen profiles and provide mappings between these.

Currently of the 16 profiles proposed by INTEROPen, most if not all of the data points they contain are supported by existing openEHR archetypes, but should additional archetypes be required these will be created and made available as open-source artefacts via the UK Apperta CKM repository.

The [Apperta 5-Nation CKM group](http://ckm.apperta.org/ckm/#showProject_1051.61.18) is currently reviewing the related openEHR templates as fit-for-purpose for UK + Ireland use, including alignment with Care-Connect profiles.

So the scope of this project was limited to the essential data points in the profiles stable enough to allow clinically useful mappings and for which clear Search specifications are available:

* Care-Connect FHIR Profiles

[AllergyIntolerance](https://nhsconnect.github.io/CareConnectAPI/api_clinical_allergyintolerance.html)
[Condition](https://nhsconnect.github.io/CareConnectAPI/api_clinical_condition.html)
[Procedure](https://nhsconnect.github.io/CareConnectAPI/api_clinical_procedure.html)
[Medication Statement](https://nhsconnect.github.io/CareConnectAPI/api_medication_medicationstatement.html)
[Medication](https://nhsconnect.github.io/CareConnectAPI/api_medication_medication.html)

* openEHR Apperta CKM Templates

[IDCR -  Adverse Reaction List.v1| Adverse reaction list](http://ckm.apperta.org/ckm/#showTemplate_1051.57.7)
[IDCR -  Medication Statement List.v1| Medication Statement list] (In preparation)
[IDCR -  Problem List.v1| Problem list](http://ckm.apperta.org/ckm/#showTemplate_1051.57.134)
[IDCR - Procedure list .v1| Procedure list](http://ckm.apperta.org/ckm/#showTemplate_1051.57.140)

### Limitations

1. Multiple occurrences on codeableConcepts not supported.
2. Update mappings as INTEROPen curations are published.
3. Update mappings as APPERTA 5N-CKM reviews progress.

### Future work

1. Add further Profiles, Immunisation, Observation.
2. Add further operations (write).
3. Improve low-level datatype mapping.
