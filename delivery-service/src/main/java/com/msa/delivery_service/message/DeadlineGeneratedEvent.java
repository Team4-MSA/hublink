package com.msa.delivery_service.message;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DeadlineGeneratedEvent {

    private UUID eventId;

    @NotNull
    private UUID deliveryId;

    private UUID aiMessageId;
    private UUID receiverUserId;
    private String receiverSlackId;

    @NotNull
    private LocalDateTime finalDepartureDeadline;

    private String messageType;
    private String message;
}
