package ld.bootstrap.write;

import ld.bootstrap.infra.db.jooq.Customer;
import ld.bootstrap.infra.db.jooq.CustomerRepository;
import ld.bootstrap.request.RegisterCustomerRequest;
import ld.bootstrap.response.CustomerResponse;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class RegisterCustomerServiceImpl implements RegisterCustomerService {

    private final CustomerRepository customerRepository;

    public RegisterCustomerServiceImpl(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    public CustomerResponse register(UUID identitySubject, RegisterCustomerRequest request) {
        Customer customer = customerRepository.register(
                identitySubject,
                request.email(),
                request.phone()
        );

        return new CustomerResponse(
                customer.id(),
                customer.identitySubject(),
                customer.email(),
                customer.phone()
        );
    }
}