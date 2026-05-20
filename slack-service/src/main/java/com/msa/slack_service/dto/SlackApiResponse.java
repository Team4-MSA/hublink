package com.msa.slack_service.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SlackApiResponse {
    private boolean ok;
    private String error;
    private String channel;
    private String ts;
}