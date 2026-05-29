package com.msa.delivery_service.enums;

public enum DeliveryStatus {
    PENDING,
    HUB_IN_TRANSIT,
    DESTINATION_HUB_ARRIVED,
    OUT_FOR_DELIVERY,
    DELIVERED,
    CANCELLED;

    public boolean canChangeTo(DeliveryStatus next) {
        return switch (this) {
            case PENDING -> next == HUB_IN_TRANSIT || next == CANCELLED;
            case HUB_IN_TRANSIT -> next == DESTINATION_HUB_ARRIVED || next == CANCELLED;
            case DESTINATION_HUB_ARRIVED ->  next == OUT_FOR_DELIVERY || next == CANCELLED;
            case OUT_FOR_DELIVERY -> next == DELIVERED || next == CANCELLED;
            case DELIVERED, CANCELLED -> false;
        };
    }
}
