package ld.bootstrap.write;

import ld.bootstrap.request.RegisterCustomerRequest;
import ld.bootstrap.response.CustomerResponse;

import java.util.UUID;

public interface RegisterCustomerService {
    CustomerResponse register(UUID identitySubject, RegisterCustomerRequest request);
}