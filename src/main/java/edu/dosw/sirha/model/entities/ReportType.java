package edu.dosw.sirha.model.entities;

public enum ReportType {

    PENDING_PETITIONS_REPORT("pendingPetitionsReport"),
    MOST_WANTED_GROUPS_REPORT("mostWantedGroupsReport"),
    GENERAL_PETITIONS_REPORT("generalPetitionsReport"),
    REASSIGNMENT_STATISTICS_REPORT("reassignmentStatisticsReport");

    private final String code;

    /**
     * Constructor for ReportType enum.
     * @param code The short code of the report type.
     */
    ReportType(String code) {
        this.code = code;
    }

    /**
     * Gets the code of the report type.
     * @return The code of the report type.
     */
    public String getCode() {
        return code;
    }

    @Override
    public String toString() {
        return code;
    }

    /**
     * Finds a ReportType by its code.
     * @param code The code to search for.
     * @return The matching ReportType.
     * @throws IllegalArgumentException if no type matches the code.
     */
    public static ReportType fromCode(String code) {
        for (ReportType type : values()) {
            if (type.getCode().equalsIgnoreCase(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown code: " + code);
    }
}