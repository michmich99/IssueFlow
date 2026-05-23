package com.issueflow.entity.enums;

public enum TicketStatus {
    TODO,
    IN_PROGRESS,
    IN_REVIEW,
    DONE;

    public boolean canTransitionTo(TicketStatus newStatus) {
        if (this == newStatus) {
            return true;
        }
        
        return switch (this) {
            case TODO -> newStatus == IN_PROGRESS;
            case IN_PROGRESS -> newStatus == IN_REVIEW;
            case IN_REVIEW -> newStatus == DONE;
            case DONE -> false;
        };
    }
}
