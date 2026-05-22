package com.msa.slack_service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SlackApiRequest {
    private String channel; // 슬랙 아이디
    private String text;
}
