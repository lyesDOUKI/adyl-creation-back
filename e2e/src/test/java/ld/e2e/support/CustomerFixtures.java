package ld.e2e.support;

import ld.bootstrap.request.RegisterCustomerRequest;

public final class CustomerFixtures {

    private final Api api;

    public CustomerFixtures(Api api) {
        this.api = api;
    }

    public void register() {
        api.customer()
                .body(new RegisterCustomerRequest("marie@example.com", "+33612345678"))
                .post("/customer/me")
                .then().statusCode(201);
    }

    public void registerOther() {
        api.otherCustomer()
                .body(new RegisterCustomerRequest("paul@example.com", "+33698765432"))
                .post("/customer/me")
                .then().statusCode(201);
    }
}