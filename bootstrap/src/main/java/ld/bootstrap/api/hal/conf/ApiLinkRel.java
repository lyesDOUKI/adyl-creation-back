package ld.bootstrap.api.hal.conf;

import java.util.Map;

public interface ApiLinkRel {
    String rel();
    String operationId();
    String description();
    Map<String, String> parameters();
}