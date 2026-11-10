package ld.e2e;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetupTest;
import ld.bootstrap.Launcher;
import ld.e2e.support.*;
import no.nav.security.mock.oauth2.MockOAuth2Server;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.postgresql.PostgreSQLContainer;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

/** Infra uniquement. Les scénarios et les données de départ vivent dans ld.e2e.support. */
@SpringBootTest(classes = Launcher.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("e2e")
public abstract class E2EBase {

    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:16");
    static final MockOAuth2Server OIDC = new MockOAuth2Server();
    protected static final GreenMail SMTP = new GreenMail(ServerSetupTest.SMTP.dynamicPort());
    static final Path PHOTOS_DIR;

    static final String ISSUER = "default";
    static final String AUDIENCE = "adyl-creation-api";

    static {
        POSTGRES.start();
        OIDC.start();
        SMTP.start();
        try {
            PHOTOS_DIR = Files.createTempDirectory("e2e-photos");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            OIDC.shutdown();
            SMTP.stop();
        }));
    }

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        r.add("spring.datasource.username", POSTGRES::getUsername);
        r.add("spring.datasource.password", POSTGRES::getPassword);

        r.add("spring.security.oauth2.resourceserver.jwt.issuer-uri",
                () -> OIDC.issuerUrl(ISSUER).toString());
        r.add("spring.security.oauth2.resourceserver.jwt.audiences[0]", () -> AUDIENCE);

        r.add("spring.mail.host", () -> "localhost");
        r.add("spring.mail.port", () -> SMTP.getSmtp().getPort());

        r.add("app.photos.storage-path", PHOTOS_DIR::toString);
    }

    @Value("${local.server.port}")
    int port;

    @Value("${server.servlet.context-path:}")
    String contextPath;

    @Autowired
    JdbcTemplate jdbc;

    @Autowired
    JsonMapper jsonMapper;

    // --- Ce que les tests utilisent ---
    protected Api api;
    protected ProductFixtures products;
    protected PhotoFixtures photos;
    protected CustomerFixtures customers;
    protected OrderFixtures orders;
    protected final Snapshots snapshots = new Snapshots();

    @BeforeEach
    void resetState() {
        List<String> tables = jdbc.queryForList("""
                select tablename from pg_tables
                where schemaname = 'public' and tablename <> 'flyway_schema_history'
                """, String.class);
        if (!tables.isEmpty()) {
            String list = String.join(", ", tables.stream().map(t -> "\"" + t + "\"").toList());
            jdbc.execute("TRUNCATE TABLE " + list + " RESTART IDENTITY CASCADE");
        }
        try {
            SMTP.purgeEmailFromAllMailboxes();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

        api = new Api(port, contextPath, jsonMapper, OIDC, ISSUER, AUDIENCE);
        products = new ProductFixtures(api);
        photos = new PhotoFixtures(api);
        customers = new CustomerFixtures(api);
        orders = new OrderFixtures(api, products, customers);
    }
}