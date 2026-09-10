package ld.application.infra.db.jooq;

import org.jooq.DSLContext;
import org.jooq.Record;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.UUID;

import static ld.application.jooq.tables.Customers.CUSTOMERS;

@Repository
public class JooqCustomerRepository implements CustomerRepository {

    private final DSLContext dsl;

    public JooqCustomerRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public Customer register(UUID identitySubject, String email, String phone) {
        UUID customerId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();

        dsl.insertInto(CUSTOMERS)
                .set(CUSTOMERS.ID, customerId)
                .set(CUSTOMERS.IDENTITY_SUBJECT, identitySubject)
                .set(CUSTOMERS.EMAIL, email)
                .set(CUSTOMERS.PHONE, phone)
                .set(CUSTOMERS.CREATED_AT, now)
                .set(CUSTOMERS.UPDATED_AT, now)
                .onConflict(CUSTOMERS.IDENTITY_SUBJECT)
                .doUpdate()
                .set(CUSTOMERS.EMAIL, email)
                .set(CUSTOMERS.PHONE, phone)
                .set(CUSTOMERS.UPDATED_AT, now)
                .execute();

        return dsl.select(
                        CUSTOMERS.ID,
                        CUSTOMERS.IDENTITY_SUBJECT,
                        CUSTOMERS.EMAIL,
                        CUSTOMERS.PHONE
                )
                .from(CUSTOMERS)
                .where(CUSTOMERS.IDENTITY_SUBJECT.eq(identitySubject))
                .fetchOne(this::toDomain);
    }

    private Customer toDomain(Record record) {
        return new Customer(
                record.get(CUSTOMERS.ID),
                record.get(CUSTOMERS.IDENTITY_SUBJECT),
                record.get(CUSTOMERS.EMAIL),
                record.get(CUSTOMERS.PHONE)
        );
    }
}
