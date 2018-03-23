
![Logos](docs/images/combined.png)

# openEHR-FHIR INTEROpen Care Connect STU3 Adaptor

This is a proof-of-concept Web service commissioned by NHS Digital Code4Health that exposes [HL7 FHIR](https://www.hl7.org/fhir) operations (`read`, `search`, `conformance`) from a choice of [openEHR CDRs](https://docs.code4health.org/ES0-overview-openehr-ehrscape.html) (Ehrscape compliant e.g. Marand Think!Ehr or Ripple EtherCis) for a small range of [INTEROpen Care-Connect](https://nhsconnect.github.io/CareConnectAPI/) profiles, using the [HAPI FHIR](http://hapifhir.io) stack.

## Build the project
`./mvnw clean package` (inside the project folder)

## Deploy
Copy `fhir-adaptor.jar` from `target` folder to your server

### Run with Marand backend
`java -jar fhir-adaptor.jar --spring.profiles.active=marand` (will run on port 8082)

### Run with Ethercis backend
`java -jar fhir-adaptor.jar --spring.profiles.active=ethercis` (will run on port 8083)

### Docker
TBD


### Code4Health Demo endpoints


#### Using Marand ThinkEHR openEHR CDR

`read` (all)

- [AllergyIntolerance](https://platform.code4health.org/marand/fhir/AllergyIntolerance)
- [Condition](https://platform.code4health.org/marand/fhir/Condition)
- [Procedure](https://platform.code4health.org/marand/fhir/Procedure)
- [MedicationStatement](https://platform.code4health.org/marand/fhir/MedicationStatement)

`search by NHS Number`

- [AllergyIntolerance](https://platform.code4health.org/marand/fhir/AllergyIntolerance?patient.identifier=https%3A%2F%2Ffhir.nhs.uk%2FId%2Fnhs-number%7C9999999000)
- [Condition](https://platform.code4health.org/marand/fhir/Condition?patient.identifier=https%3A%2F%2Ffhir.nhs.uk%2FId%2Fnhs-number%7C9999999000)
- [Procedure](https://platform.code4health.org/marand/fhir/Procedure?patient.identifier=https%3A%2F%2Ffhir.nhs.uk%2FId%2Fnhs-number%7C9999999000)
- [MedicationStatement](https://platform.code4health.org/marand/fhir/MedicationStatement?patient.identifier=https%3A%2F%2Ffhir.nhs.uk%2FId%2Fnhs-number%7C9999999000)

`conformance`
- [Conformance](https://platform.code4health.org/marand/fhir/Conformance)


#### Using Ripple EtherCis openEHR CDR

`read` (all)

- [AllergyIntolerance](https://platform.code4health.org/ethercis/fhir/AllergyIntolerance)
- [Condition](https://platform.code4health.org/ethercis/fhir/Condition)
- [Procedure](https://platform.code4health.org/ethercis/fhir/Procedure)
- [MedicationStatement](https://platform.code4health.org/ethercis/fhir/MedicationStatement)

`search by NHS Number`

- [AllergyIntolerance](https://platform.code4health.org/ethercis/fhir/AllergyIntolerance?patient.identifier=https%3A%2F%2Ffhir.nhs.uk%2FId%2Fnhs-number%7C9999999000)
- [Condition](https://platform.code4health.org/ethercis/fhir/Condition?patient.identifier=https%3A%2F%2Ffhir.nhs.uk%2FId%2Fnhs-number%7C9999999000)
- [Procedure](https://platform.code4health.org/ethercis/fhir/Procedure?patient.identifier=https%3A%2F%2Ffhir.nhs.uk%2FId%2Fnhs-number%7C9999999000)
- [MedicationStatement](https://platform.code4health.org/ethercis/fhir/MedicationStatement?patient.identifier=https%3A%2F%2Ffhir.nhs.uk%2FId%2Fnhs-number%7C9999999000)

`conformance`
- [Conformance](https://platform.code4health.org/ethercis/fhir/Conformance)


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

The scope of this project was therefore limited to the essential data points in the profiles stable enough to allow clinically useful mappings and for which clear Search specifications are available:

## Care-Connect FHIR Profiles

- [AllergyIntolerance](https://nhsconnect.github.io/CareConnectAPI/api_clinical_allergyintolerance.html)
- [Condition](https://nhsconnect.github.io/CareConnectAPI/api_clinical_condition.html)
- [Procedure](https://nhsconnect.github.io/CareConnectAPI/api_clinical_procedure.html)
- [Medication Statement](https://nhsconnect.github.io/CareConnectAPI/api_medication_medicationstatement.html)
- [Medication](https://nhsconnect.github.io/CareConnectAPI/api_medication_medication.html)

#### openEHR Apperta CKM Templates

- [IDCR - Adverse Reaction List.v1| Adverse reaction list](http://ckm.apperta.org/ckm/#showTemplate_1051.57.71)
- [IDCR - Medication Statement List.v1| Medication Statement list](http://ckm.apperta.org/ckm/#showTemplate_1051.57.143)
- [IDCR - Problem List.v1| Problem list](http://ckm.apperta.org/ckm/#showTemplate_1051.57.134)
- [IDCR - Procedure list .v1| Procedure list](http://ckm.apperta.org/ckm/#showTemplate_1051.57.140)

#### Mapping Guidance documents

- [openEHR AdverseReactionRisk to FHIR allergyIntolerance STU3 mappings](docs/mapping_guidance/openEHR-AdverseReactionRisk-to-FHIR-AllergyIntolerance-STU3-mappings.adoc)
- [openEHR Medication Order (as MedicationStatement) to FHIR MedicationStatement STU3 mappings](docs/mapping_guidance/openEHR-MedicationOrder-to-FHIR-MedicationStatement-STU3-mappings.adoc)
- [openEHR ProblemDiagnosis to FHIR condition STU3 mappings](docs/mapping_guidance/openEHR-ProblemDiagnosis-to-FHIR-Condition-STU3-mappings.adoc)
- [openEHR Procedure to FHIR Procedure STU3 mappings](docs/mapping_guidance/openEHR-Procedure-to-FHIR-Procedure-STU3-mappings.adoc)

#### General Approach

The general approach has been to construct openEHR Archetype Language (AQL) statements to retrieve appropriate data from target openEHR templates. The mappings have been built in java, with considerable use made of generic mapping functions, facilitated by the close alignment of many openEHR and FHIR datatypes.

For more on AQL see:

- [Querying EHR Data with Archetype Query Language](https://www.slideshare.net/borutf/querying-ehr-data-with-archetype-query-language)
- [openEHR in Context](https://www.slideshare.net/freshehr/1-7-openehr-in-context)

or the examples given in the guidance documents.

##### AQL example for AllergyIntolerance
```sql
select
    e/ehr_id/value as ehrId,
    e/ehr_status/subject/external_ref/id/value as subjectId,
    e/ehr_status/subject/external_ref/namespace as subjectNamespace,
    a/uid/value as compositionId,
    a/composer/name as composerName,
    a/composer/external_ref/id/value as composerIdentifier,
    a/composer/external_ref/namespace as composerNamespace,
    a/context/start_time/value as compositionStartTime,
    b_a/uid/value as entryId,
    b_a/data[at0001]/items[at0002]/value as Causative_agent,
    b_a/data[at0001]/items[at0063]/value/defining_code/code_string as Status_code,
    b_a/data[at0001]/items[at0101]/value/defining_code/code_string as Criticality_code,
    b_a/data[at0001]/items[at0120]/value/defining_code/code_string as Category_code,
    b_a/data[at0001]/items[at0117]/value/value as Onset_of_last_reaction,
    b_a/data[at0001]/items[at0058]/value/defining_code/code_string as Reaction_mechanism_code,
    b_a/data[at0001]/items[at0006]/value/value as Comment,
    b_a/protocol[at0042]/items[at0062]/value/value as Adverse_reaction_risk_Last_updated,
    b_a/data[at0001]/items[at0009]/items[at0010]/value as Specific_substance,
    b_a/data[at0001]/items[at0009]/items[at0021]/value/defining_code/code_string as Certainty_code,
    b_a/data[at0001]/items[at0009]/items[at0011]/value as Manifestation,
    b_a/data[at0001]/items[at0009]/items[at0012]/value/value as Reaction_description,
    b_a/data[at0001]/items[at0009]/items[at0027]/value/value as Onset_of_reaction,
    b_a/data[at0001]/items[at0009]/items[at0089]/value/defining_code/code_string as Severity_code,
    b_a/data[at0001]/items[at0009]/items[at0106]/value as Route_of_exposure,
    b_a/data[at0001]/items[at0009]/items[at0032]/value/value as Adverse_reaction_risk_Comment
from EHR e

contains COMPOSITION a[openEHR-EHR-COMPOSITION.adverse_reaction_list.v1]
contains EVALUATION b_a[openEHR-EHR-EVALUATION.adverse_reaction_risk.v1]

where a/name/value='Adverse reaction list'
-- Optional parameters, depending on FHIR search criteria
and e/ehr_id/value = '{{fhir.patient.id_param}}'
and e/ehr_status/subject/external_ref/id/value = '{{fhir.patient.identifier.value.param}}'
and e/ehr_status/subject/external_ref/namespace =  '{{fhir.patient.identifier.system.param}}'
and b_a/data[at0001]/items[at0120]/value/defining_code_string = '{{fhir_category_params}}'
and b_a/protocol[at0042]/items[at0062]/value/value >= '{{fhir_date_param_min}}'
and b_a/protocol[at0042]/items[at0062]/value/value <= '{{fhir_date_param_max}}'

```
### Limitations

1. Multiple occurrences on codeableConcept not supported.
2. Update mappings as INTEROPen curations are published.
3. Update mappings as Apperta 5N-CKM reviews progress.

### Future work

1. Add further Profiles, Immunisation, Observation.
2. Add further operations (write).
3. Improve low-level datatype mapping.
3. Improve and extend search criteria.
4. Adapt to replace the Ehrscape API with formal openEHR REST API.
