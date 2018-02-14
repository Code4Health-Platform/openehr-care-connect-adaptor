Feature: Allow an openEHR CDR to expose a FHIR Care-Connect Procedure resource profile for read, search and conformance operations.

Scenario: User calls a FHIR Care-Connect Procedure resource profile, requesting all Condition resources.
    Given the openEHR CDR service is available, a valid authentication is given and the CDR contains valid Procedure resources
    When a user calls the read FHIR Procedure operation
    Then the status code is 200
    And the response includes
      | entry.resource.resourceType |  Procedure |
      | entry.resource.id | a valid EntryID for the underlying openEHR Procedure  |
      | entry.resource.meta.profile | https://fhir.hl7.org.uk/STU3/StructureDefinition/CareConnect-Procedure-1  |
    And the response includes
      | entry.resource.code.text |  total replacement of hip |
      | entry.resource.code.coding.system | http://snomed.info/sct  |
      | entry.resource.code.coding.code | 52734007  |
      | entry.resource.code.coding.display | total replacement of hip  |
      | entry.resource.code.coding.userSelected | true  |
  And the response includes
    | entry.resource.bodySite.text |  Left hip |
    | entry.resource.bodySite.coding.system | http://snomed.info/sct  |
    | entry.resource.bodySite.coding.code | 287679003  |
    | entry.resource.bodySite.coding.display | Left hip  |
    | entry.resource.bodySite.coding.userSelected | true  |
    And the response includes
      | entry.resource.status |  completed |
    And the response includes
      | entry.resource.subject.reference |   |
      | entry.resource.subject.identifier.system | https://fhir.nhs.uk/Id/nhs-number  |
      | entry.resource.subject.identifier.value | 9999999000  |
    And the response includes
      | entry.resource.performedDateTime |  2016-10-15T15:11:33.829Z |
    And the response includes
      | entry.resource.performer.role |  performer |
      | entry.resource.performer.actor. |   |
    And the response includes
      | entry.resource.outcome |  successful |
    And the response includes
      | entry.resource.complication |  blood loss |
    And the response includes
      | entry.resource.note  |  good recovery |
    And the response includes
      | entry.resource.code.text |  Osteoarthritis |
      | entry.resource.code.coding.system | http://snomed.info/sct  |
      | entry.resource.code.coding.code | 396275006 |
      | entry.resource.code.coding.display | Osteoarthritis  |
      | entry.resource.code.coding.userSelected | true  |
  And the response includes
    | entry.resource.category.text | Surgical procedure |
    | entry.resource.category.coding.system | http://snomed.info/sct  |
    | entry.resource.category.coding.code | 387713003   |
    | entry.resource.category.coding.display | Surgical procedure  |
    | entry.resource.category.coding.userSelected | true  |


