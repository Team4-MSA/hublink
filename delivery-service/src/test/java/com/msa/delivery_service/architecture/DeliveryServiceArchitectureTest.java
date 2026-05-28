package com.msa.delivery_service.architecture;

import com.hublink.common.architecture.HubLinkArchRules;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(packages = "com.msa.delivery_service")
class DeliveryServiceArchitectureTest {
    @ArchTest
    static final ArchRule layerRule = HubLinkArchRules.LAYER_RULE;

    @ArchTest
    static final ArchRule entityIsolationRule = HubLinkArchRules.ENTITY_ISOLATION_RULE;

    @ArchTest
    static final ArchRule controllerConventionRule = HubLinkArchRules.CONTROLLER_CONVENTION_RULE;

    @ArchTest
    static final ArchRule dtoIsolationRule = HubLinkArchRules.DTO_ISOLATION_RULE;

    @ArchTest
    static final ArchRule noStandardStreamsRule = HubLinkArchRules.NO_STANDARD_STREAMS_RULE;
}