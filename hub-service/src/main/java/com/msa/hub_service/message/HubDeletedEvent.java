package com.msa.hub_service.message;

import java.util.UUID;

public record HubDeletedEvent(UUID hubId) {
}
