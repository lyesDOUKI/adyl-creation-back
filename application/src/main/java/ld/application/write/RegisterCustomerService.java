package ld.application.write;

import ld.application.request.RegisterCustomerRequest;
import ld.application.response.CustomerResponse;
import ld.standard.lib.validation.Result;

import java.util.UUID;

public interface RegisterCustomerService {
    Result<CustomerResponse> register(UUID identitySubject, RegisterCustomerRequest request);
}