package ld.application.write;

import ld.application.request.RegisterCustomerRequest;
import ld.application.response.CustomerResponse;

import java.util.UUID;

public interface RegisterCustomerService {
    CustomerResponse register(UUID identitySubject, RegisterCustomerRequest request);
}