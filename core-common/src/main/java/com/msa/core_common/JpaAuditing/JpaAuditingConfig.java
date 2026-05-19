package com.msa.core_common.JpaAuditing;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "awareUserAudit")
public class JpaAuditingConfig {
}
