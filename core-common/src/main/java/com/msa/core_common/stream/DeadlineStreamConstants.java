package com.msa.core_common.stream;

public final class DeadlineStreamConstants {
    private DeadlineStreamConstants() {
    }

    // 스트림
    public static final String DEADLINE_REQUESTED_STREAM = "deadline:requested:stream";
    public static final String DEADLINE_GENERATED_STREAM = "deadline:generated:stream";

    // 서비스 그룹
    public static final String AI_SERVICE_GROUP = "ai-service-group";
    public static final String SLACK_SERVICE_GROUP = "slack-service-group";

    // 소비자
    public static final String AI_SERVICE_CONSUMER = "ai-service";
    public static final String SLACK_SERVICE_CONSUMER = "slack-service";
}
