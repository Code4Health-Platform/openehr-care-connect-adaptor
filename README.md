# FHIR Adaptor
Web service that exposes data (read-only) from an openEHR CDR via FHIR resources (HAPI FHIR)

## Build the project
`./mvnw clean package` (inside the project folder)

## Deploy
Copy `fhir-adaptor.jar` from `target` folder to your server

### Run with Marand backend
`java -jar fhir-adaptor.jar --spring.profiles.active=marand` (will run on port 8082)

### Run with Ethercis backend
`java -jar fhir-adaptor.jar --spring.profiles.active=ethercis` (will run on port 8083)

## Links
https://docs.google.com/document/d/1UHezdomM3_QUkFIFqg6_XCi0ui7FKYo2QC10_jE7BrU

https://www.hl7.org/fhir/allergyintolerance.html

https://fhir.nhs.uk/STU3/StructureDefinition/CareConnect-GPC-AllergyIntolerance-1

http://hapifhir.io/doc_rest_operations.html
