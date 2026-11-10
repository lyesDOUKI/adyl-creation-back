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
    private static final Pattern PROCESSING_TIMESTAMP = Pattern.compile(
            "(\"(?:submittedAt|cancelledAt|createdAt|updatedAt)\"\\s*:\\s*)\"[^\"]*\"");

    private final Map<String, String> uuids = new HashMap<>();

    /** Masque tous les horodatages : pour les réponses dont les dates ne dépendent pas du test. */
    public String stable(String json) {
        String result = maskUuids(json);
        result = DATE.matcher(result).replaceAll("<date>");
        return REFERENCE.matcher(result).replaceAll("$1\"<reference>\"");
    }

    public String of(Response response) {
        return stable(prettify(response));
    }

    /** Garde les horaires de créneaux (déterministes), ne masque que les horodatages de traitement. */
    public String withSlotTimes(Response response) {
        String result = maskUuids(prettify(response));
        result = PROCESSING_TIMESTAMP.matcher(result).replaceAll("$1\"<date>\"");
        return REFERENCE.matcher(result).replaceAll("$1\"<reference>\"");
    }

    private String maskUuids(String json) {
        return UUID.matcher(json).replaceAll(m ->
                uuids.computeIfAbsent(m.group().toLowerCase(), k -> "<uuid-" + (uuids.size() + 1) + ">"));
    }

    private static String prettify(Response response) {
        return JsonPath.from(response.asString()).prettify();
    }
}