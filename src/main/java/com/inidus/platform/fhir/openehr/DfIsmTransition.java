package com.inidus.platform.fhir.openehr;

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


/**
 * Handles openEHR ISM_TRANSITION class interface with FHIR, including enumeration of current_state codes
 */
public class DfIsmTransition {

    // Enumeration of the valid openEHR current_state codes
    public enum OpenEhrCurrentStateEnum {
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


        public static OpenEhrCurrentStateEnum fromCode(String codeString) {
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

        public static String toCode(OpenEhrCurrentStateEnum enumCurrentState) {
            switch (enumCurrentState) {
                case INITIAL:
                    return "524";
                case PLANNED:
                    return "526";
                case POSTPONED:
                    return "527";
                case CANCELLED:
                    return "528";
                case SCHEDULED:
                    return "529";
                case ACTIVE:
                    return "245";
                case SUSPENDED:
                    return "530";
                case ABORTED:
                    return "531";
                case COMPLETED:
                    return "532";
                case EXPIRED:
                    return "533";
                default:
                    return "?";
            }
        }

        public static String getDisplay(OpenEhrCurrentStateEnum enumCurrentState) {
            switch (enumCurrentState) {
                case INITIAL:
                    return "initial";
                case PLANNED:
                    return "planned";
                case POSTPONED:
                    return "postponed";
                case CANCELLED:
                    return "cancelled";
                case SCHEDULED:
                    return "scheduled";
                case ACTIVE:
                    return "active";
                case SUSPENDED:
                    return "suspended";
                case ABORTED:
                    return "aborted";
                case COMPLETED:
                    return "completed";
                case EXPIRED:
                    return "expired";
                default:
                    return "?";
            }
        }
    }

}
