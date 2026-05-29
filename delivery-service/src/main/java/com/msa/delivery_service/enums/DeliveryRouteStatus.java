package com.msa.delivery_service.enums;

public enum DeliveryRouteStatus {
    PENDING,
    IN_TRANSIT,
    COMPLETED,
    SKIPPED,
    FAILED;

    public boolean canChangeTo(DeliveryRouteStatus next) {
        return switch (this) {
            case PENDING -> next == IN_TRANSIT || next == SKIPPED || next == FAILED;
            case IN_TRANSIT -> next == COMPLETED ||  next == SKIPPED || next == FAILED;
            case COMPLETED, SKIPPED, FAILED -> false;
        };
    }
}
