package ld.e2e.support;

import io.restassured.http.ContentType;

import java.util.List;
import java.util.UUID;

public final class PhotoFixtures {

    private final Api api;

    public PhotoFixtures(Api api) {
        this.api = api;
    }

    public String add(UUID productId, String fileName, byte[] content) {
        List<String> urls = api.admin()
                .contentType(ContentType.MULTIPART)
                .multiPart("photos", fileName, content, "image/png")
                .post("/products/{id}/photos", productId)
                .then().statusCode(201)
                .extract().path("photos.url");

        String last = urls.getLast();
        return last.substring(last.lastIndexOf('/') + 1);
    }
}