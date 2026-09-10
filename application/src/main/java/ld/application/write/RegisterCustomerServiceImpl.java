package ld.application.write;

import ld.application.infra.db.jooq.Customer;
import ld.application.infra.db.jooq.CustomerRepository;
import ld.application.request.RegisterCustomerRequest;
import ld.application.response.CustomerResponse;
import ld.standard.lib.validation.Result;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class RegisterCustomerServiceImpl implements RegisterCustomerService {

    private final CustomerRepository customerRepository;

    public RegisterCustomerServiceImpl(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    public Result<CustomerResponse> register(UUID identitySubject, RegisterCustomerRequest request) {
        Customer customer = customerRepository.register(
                identitySubject,
                request.email(),
                request.phone()
        );

        return Result.success(new CustomerResponse(
                customer.id(),
                customer.identitySubject(),
                customer.email(),
                customer.phone()
        ));
    }
}
