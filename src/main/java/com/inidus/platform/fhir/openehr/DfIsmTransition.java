package com.inidus.platform.fhir.openehr;

import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Procedure;
import org.hl7.fhir.exceptions.FHIRException;
import org.openehr.rm.datatypes.text.CodePhrase;
import org.openehr.rm.datatypes.text.DvCodedText;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

// openEHR currentState Codes
//• 524: initial
//• 526: planned
//• 527: postponed
//• 528: cancelled
//• 529: scheduled
//• 245: active
//• 530: suspended
//• 531: aborted
//• 532: completed
//• 533: expired

//            case PREPARATION: return "preparation";
//            case INPROGRESS: return "in-progress";
//            case SUSPENDED: return "suspended";
//            case ABORTED: return "aborted";
//            case COMPLETED: return "completed";
//            case ENTEREDINERROR: return "entered-in-error";
//            case UNKNOWN: return "unknown";


public class DfIsmTransition {

    private static final Map<String, String> openEHRCurrentStateToFHIRProcedureStatusCode = new HashMap<>();
    private static final Map<String, String> FHIRProcedureStatusToOpenEhrCurrentStateCode = new HashMap<>();
    private static final Map<String, String> OpenEhrCurrentStateToFhirProcedureSystem = new HashMap<>();
    private static final Map<String, String> FhirProcedureStatusToOpenEhrCurrentStateSystem = new HashMap<>();

    //    private static final Logger log = LoggerFactory.getLogger(DfText.class);

    static {
        openEHRCurrentStateToFHIRProcedureStatusCode.put("524", "preparation"); //initial
        openEHRCurrentStateToFHIRProcedureStatusCode.put("526", "preparation"); //planned
        openEHRCurrentStateToFHIRProcedureStatusCode.put("527", "suspended"); //postponed
        openEHRCurrentStateToFHIRProcedureStatusCode.put("528", "aborted"); //aborted
        openEHRCurrentStateToFHIRProcedureStatusCode.put("529", "preparation"); //scheduled
        openEHRCurrentStateToFHIRProcedureStatusCode.put("245", "in-progress"); //active
        openEHRCurrentStateToFHIRProcedureStatusCode.put("530", "suspended"); //suspended
        openEHRCurrentStateToFHIRProcedureStatusCode.put("531", "aborted"); //aborted
        openEHRCurrentStateToFHIRProcedureStatusCode.put("532", "completed"); //completed
        openEHRCurrentStateToFHIRProcedureStatusCode.put("533", "aborted"); //expired

        FHIRProcedureStatusToOpenEhrCurrentStateCode.put("preparation","526"); //planned
        FHIRProcedureStatusToOpenEhrCurrentStateCode.put("suspended","530"); //suspended
        FHIRProcedureStatusToOpenEhrCurrentStateCode.put("aborted","528"); //aborted
        FHIRProcedureStatusToOpenEhrCurrentStateCode.put( "in-progress","245"); //active
        FHIRProcedureStatusToOpenEhrCurrentStateCode.put("completed","532"); //completed

        OpenEhrCurrentStateToFhirProcedureSystem.put("openehr.org","http://hl7.org/fhir/event-status");
        FhirProcedureStatusToOpenEhrCurrentStateSystem.put("http://hl7.org/fhir/event-status","openehr.org");
   }


   public enum OpenEhrCurrentState {
        INITIAL,
        PLANNED,
        POSTPONED,
        CANCELLED,
        SCHEDULED,
        ACTIVE,
        SUSPENDED,
        ABORTED,
        COMPLETED,
        EXPIRED;

        public static DfIsmTransition.OpenEhrCurrentState fromCode(String codeString) {
            if (codeString == null || "".equals(codeString))
                return null;
            if ("524".equals(codeString))
                return INITIAL;
            if ("526".equals(codeString))
                return PLANNED;
            if ("527".equals(codeString))
                return POSTPONED;
            if ("528".equals(codeString))
                return CANCELLED;
            if ("529".equals(codeString))
                return SCHEDULED;
            if ("245".equals(codeString))
                return ACTIVE;
            if ("530".equals(codeString))
                return SUSPENDED;
            if ("531".equals(codeString))
                return ABORTED;
            if ("532".equals(codeString))
                return COMPLETED;
            if ("533".equals(codeString))
                return EXPIRED;
            //default
            return null;
        }


        public String toCode() {
            switch (this) {
                case INITIAL: return "524";
                case PLANNED: return "526";
                case POSTPONED: return "527";
                case CANCELLED: return "528";
                case SCHEDULED: return "529";
                case ACTIVE: return "245";
                case SUSPENDED: return "530";
                case ABORTED : return "531";
                case COMPLETED: return "532";
                case EXPIRED: return "533";
                default: return "?";
            }
        }

        public String getSystem() {
             return "openehr";
        }
        public String getDisplay() {
            switch (this) {
                case INITIAL: return "initial";
                case PLANNED: return "planned";
                case POSTPONED: return "postponed";
                case CANCELLED: return "cancelled";
                case SCHEDULED: return "scheduled";
                case ACTIVE: return "active";
                case SUSPENDED: return "suspended";
                case ABORTED : return "aborted";
                case COMPLETED: return "completed";
                case EXPIRED: return "expired";
                default: return "?";
            }
        }


    }

    public static Procedure.ProcedureStatus getProcedureStatusEnumFromCode(String currentStatusCode) throws FHIRException {

        return Procedure.ProcedureStatus.fromCode(
                openEHRCurrentStateToFHIRProcedureStatusCode.get(currentStatusCode));
    }

}
