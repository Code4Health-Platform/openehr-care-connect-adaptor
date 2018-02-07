Feature: Allow an openEHR CDR to expose a FHIR Care-Connect Condition resource profile for read, search and confomance operations.

Scenario: User calls a FHIR Care-Connect Condition resource profile, requesting all Condition resources.
    Given the openEHR CDR service is available and contains valid Condition resources
    When a user calls the read Condition operation
    Then the status code is 200
    And the response includes
      | entry.resource.resourceType |  Condition |
      | entry.resource.id | 01fc6ad3-3d10-4bbf-98ce-99c81113ff9c::vm01.ethercis.org::1_null  |
      | entry.resource.meta.profile | https://fhir.hl7.org.uk/STU3/StructureDefinition/CareConnect-Condition-1  |
    And the response includes
      | entry.resource.code.text |  asasdas |
      | entry.resource.code.coding.system | http://snomed.info/sct  |
      | entry.resource.code.coding.code | http://snomed.info/sct  |
      | entry.resource.code.coding.display | asdasd  |
      | entry.resource.code.coding.userSelected | true  |
    And the response includes
      | entry.resource.clinicalStatus |  asasdas |
      | entry.resource.verificationStatus | http://snomed.info/sct  |
    And the response includes
      | entry.resource.subject.reference | http://snomed.info/sct  |
      | entry.resource.subject.identifier.system | https://fhir.nhs.uk/Id/nhs-number  |
      | entry.resource.subject.identifier.value | 9999999000  |
    And the response includes
      | entry.resource.assertedDate |  asasdas |
    And the response includes
      | entry.resource.reaction.manifestation.text |  asasdas |

