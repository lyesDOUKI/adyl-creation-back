package ld.e2e;

import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/** Rend un JSON stable : une instance par test, pour garder la même numérotation d'un appel à l'autre. */
public final class Snapshots {

    private static final Pattern UUID = Pattern.compile(
            "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}");
    private static final Pattern DATE = Pattern.compile(
            "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d+)?(Z|[+-]\\d{2}:\\d{2})");
    private static final Pattern REFERENCE = Pattern.compile(
            "(\"(?:[A-Za-z0-9_]*(?:Reference|_reference)|reference[A-Za-z0-9_]*)\"\\s*:\\s*)\"[^\"]*\"");

    private final Map<String, String> uuids = new HashMap<>();

    public String stable(String json) {
        String result = UUID.matcher(json).replaceAll(m ->
                uuids.computeIfAbsent(m.group().toLowerCase(), k -> "<uuid-" + (uuids.size() + 1) + ">"));
        result = DATE.matcher(result).replaceAll("<date>");
        return REFERENCE.matcher(result).replaceAll("$1\"<reference>\"");
    }

    /** JSON de la réponse, indenté puis rendu stable (quel que soit le content-type). */
    public String of(Response response) {
        return stable(JsonPath.from(response.asString()).prettify());
    }
}