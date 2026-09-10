package ld.application.tests.repository;

import ld.application.config.common.SharedPostgresContainer;
import ld.application.context.ProductIntegrationTest;
import ld.application.infra.db.jooq.JooqGetProductQueryRepository;
import ld.application.infra.db.jooq.ProductQuery;
import ld.application.infra.db.jooq.exception.InvalidSortFieldException;
import ld.domain.features.product.model.ProductStatus;
import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.testcontainers.containers.PostgreSQLContainer;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static ld.application.jooq.tables.Customers.CUSTOMERS;
import static ld.application.jooq.tables.OrderDetails.ORDER_DETAILS;
import static ld.application.jooq.tables.Orders.ORDERS;
import static ld.application.jooq.tables.ProductColors.PRODUCT_COLORS;
import static ld.application.jooq.tables.ProductPhotos.PRODUCT_PHOTOS;
import static ld.application.jooq.tables.Products.PRODUCTS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ProductIntegrationTest
class JooqGetProductEntityQueryRepositoryTest {

    @ServiceConnection
    static PostgreSQLContainer<?> POSTGRES = SharedPostgresContainer.INSTANCE;

    @Autowired
    private DSLContext dsl;

    private JooqGetProductQueryRepository repository;

    @BeforeEach
    void setUp() {
        repository = new JooqGetProductQueryRepository(dsl);
        dsl.deleteFrom(ORDER_DETAILS).execute();
        dsl.deleteFrom(ORDERS).execute();
        dsl.deleteFrom(CUSTOMERS).execute();
        dsl.deleteFrom(PRODUCT_PHOTOS).execute();
        dsl.deleteFrom(PRODUCT_COLORS).execute();
        dsl.deleteFrom(PRODUCTS).execute();
    }

    @Test
    void findById_aggregates_colors_photos_in_order_and_distinct_order_count() {
        UUID productId = UUID.randomUUID();
        insertProduct(productId, "T-shirt", BigDecimal.valueOf(19.90));

        dsl.insertInto(PRODUCT_COLORS, PRODUCT_COLORS.PRODUCT_ID, PRODUCT_COLORS.COLOR)
                .values(productId, "rouge")
                .values(productId, "bleu")
                .execute();

        dsl.insertInto(PRODUCT_PHOTOS, PRODUCT_PHOTOS.ID, PRODUCT_PHOTOS.PRODUCT_ID, PRODUCT_PHOTOS.STORAGE_KEY, PRODUCT_PHOTOS.POSITION)
                .values(UUID.randomUUID(), productId, "second.jpg", 2)
                .values(UUID.randomUUID(), productId, "first.jpg", 1)
                .execute();

        UUID customerId = UUID.randomUUID();
        insertCustomer(customerId);

        UUID orderId = UUID.randomUUID();
        insertOrder(orderId, customerId);

        dsl.insertInto(ORDER_DETAILS, ORDER_DETAILS.ID, ORDER_DETAILS.ORDER_ID,
                        ORDER_DETAILS.PRODUCT_ID, ORDER_DETAILS.QUANTITY,
                        ORDER_DETAILS.UNIT_PRICE, ORDER_DETAILS.TOTAL_AMOUNT)
                .values(UUID.randomUUID(), orderId, productId, BigDecimal.valueOf(1), BigDecimal.valueOf(19.90), BigDecimal.valueOf(19.90))
                .values(UUID.randomUUID(), orderId, productId, BigDecimal.valueOf(1), BigDecimal.valueOf(19.90), BigDecimal.valueOf(19.90))
                .execute();

        Optional<ProductQuery> result = repository.findById(productId);

        assertThat(result).isPresent();
        ProductQuery product = result.get();
        assertThat(product.name()).isEqualTo("T-shirt");
        assertThat(product.colors()).containsExactlyInAnyOrder("rouge", "bleu");
        assertThat(product.photoStorageKeys()).containsExactly("first.jpg", "second.jpg");
        assertThat(product.numberOfOrders()).isEqualTo(1);
    }

    @Test
    void findById_returns_empty_optional_when_product_does_not_exist() {
        Optional<ProductQuery> result = repository.findById(UUID.randomUUID());

        assertThat(result).isEmpty();
    }

    @Test
    void findById_returns_empty_collections_when_product_has_no_colors_or_photos() {
        UUID productId = UUID.randomUUID();
        insertProduct(productId, "Mug", BigDecimal.ONE);

        Optional<ProductQuery> result = repository.findById(productId);

        assertThat(result).isPresent();
        assertThat(result.get().colors()).isEmpty();
        assertThat(result.get().photoStorageKeys()).isEmpty();
        assertThat(result.get().numberOfOrders()).isZero();
    }

    @Test
    void findAll_sorts_by_requested_field_and_paginates() {
        insertProduct(UUID.randomUUID(), "A", BigDecimal.valueOf(30));
        insertProduct(UUID.randomUUID(), "B", BigDecimal.valueOf(10));
        insertProduct(UUID.randomUUID(), "C", BigDecimal.valueOf(20));

        Page<ProductQuery> page = repository.findAll(
                PageRequest.of(0, 2, Sort.by("price").ascending())
        );

        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(page.getContent()).extracting(ProductQuery::name)
                .containsExactly("B", "C");
    }

    @Test
    void findAll_second_page_returns_remaining_elements() {
        insertProduct(UUID.randomUUID(), "A", BigDecimal.valueOf(30));
        insertProduct(UUID.randomUUID(), "B", BigDecimal.valueOf(10));
        insertProduct(UUID.randomUUID(), "C", BigDecimal.valueOf(20));

        Page<ProductQuery> page = repository.findAll(
                PageRequest.of(1, 2, Sort.by("price").ascending())
        );

        assertThat(page.getContent()).extracting(ProductQuery::name)
                .containsExactly("A");
    }

    @Test
    void findAll_throw_exception_when_field_is_unknown() {
        insertProduct(UUID.randomUUID(), "A", BigDecimal.ONE);
        insertProduct(UUID.randomUUID(), "B", BigDecimal.TEN);

        assertThatThrownBy(() ->
                repository.findAll(
                        PageRequest.of(0, 10, Sort.by("champInexistant"))
                )
        )
                .isInstanceOf(InvalidSortFieldException.class);
    }

    @Test
    void findAll_returns_zero_total_and_empty_content_when_table_is_empty() {
        Page<ProductQuery> page = repository.findAll(PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isZero();
        assertThat(page.getContent()).isEmpty();
    }


    private void insertProduct(UUID productId, String name, BigDecimal price) {
        dsl.insertInto(PRODUCTS)
                .columns(PRODUCTS.ID, PRODUCTS.NAME, PRODUCTS.UNIT_PRICE, PRODUCTS.STATUS)
                .values(productId, name, price, String.valueOf(ProductStatus.AVAILABLE))
                .execute();
    }

    private void insertCustomer(UUID customerId) {
        dsl.insertInto(CUSTOMERS, CUSTOMERS.ID, CUSTOMERS.IDENTITY_SUBJECT,
                        CUSTOMERS.EMAIL, CUSTOMERS.PHONE)
                .values(UUID.randomUUID(), customerId, "jean.dupont+" + customerId + "@test.com", "0600000000")
                .execute();
    }

    private void insertOrder(UUID orderId, UUID customerId) {
        dsl.insertInto(ORDERS, ORDERS.ID, ORDERS.CUSTOMER_ID)
                .values(orderId, customerId)
                .execute();
    }
}