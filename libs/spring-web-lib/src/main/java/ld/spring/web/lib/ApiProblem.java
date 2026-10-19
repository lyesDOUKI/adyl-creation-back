package ld.spring.web.lib;

import java.util.Map;

public interface ApiProblem {
    String title();
    String detail();
    default Map<String, ?> properties() { return Map.of(); }
}
