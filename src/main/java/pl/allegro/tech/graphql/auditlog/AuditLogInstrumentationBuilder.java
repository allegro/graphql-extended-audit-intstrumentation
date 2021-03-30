package pl.allegro.tech.graphql.auditlog;

import java.time.Clock;

public class AuditLogInstrumentationBuilder {

  public static final String ANONYMIZED_FIELDS_CONFIG_FILE = "auditLogAnonymizedFields.json";
  public static final String ADDITIONAL_FIELDS_CONFIG_FILE = "auditLogAdditionalFields.json";

  private Clock clock = Clock.systemUTC();
  private AuditLogSender auditLogSender = new AuditLogSenderImpl();
  private UserProvider userProvider = new AnonymousUserProvider();
  private AuditLogAnonymizer auditLogAnonymizer = new AuditLogAnonymizer(
      new DefaultAnonymizedFieldsSetupPreconditions(
          JsonUtil.jsonFieldSetups(ANONYMIZED_FIELDS_CONFIG_FILE)));
  private AuditLogAdditionalFieldFetcher auditLogAdditionalFieldFetcher =
      new AuditLogAdditionalFieldFetcher(new DefaultAdditionalFieldsSetupPreconditions(
          JsonUtil.jsonFieldSetups(ADDITIONAL_FIELDS_CONFIG_FILE)));

  public AuditLogInstrumentationBuilder() {
  }

  public AuditLogInstrumentationBuilder(
      Clock clock,
      AuditLogSender auditLogSender,
      UserProvider userProvider,
      AuditLogAnonymizer auditLogAnonymizer,
      AuditLogAdditionalFieldFetcher auditLogAdditionalFieldFetcher) {
    this.clock = clock;
    this.auditLogSender = auditLogSender;
    this.userProvider = userProvider;
    this.auditLogAnonymizer = auditLogAnonymizer;
    this.auditLogAdditionalFieldFetcher = auditLogAdditionalFieldFetcher;
  }

  /**
   * Enable sending audit log;
   *
   * @param auditLogSender {@link AuditLogSender}
   * @return {@link AuditLogInstrumentationBuilder}
   */
  public AuditLogInstrumentationBuilder withActionLogSender(AuditLogSender auditLogSender) {
    this.auditLogSender = auditLogSender;
    return this;
  }

  /**
   * Set clock for audit log;
   *
   * @param clock {@link Clock}
   * @return {@link AuditLogInstrumentationBuilder}
   */
  public AuditLogInstrumentationBuilder withClock(Clock clock) {
    this.clock = clock;
    return this;
  }

  /**
   * Set user context provider.
   *
   * @param userProvider {@link UserProvider}
   * @return {@link AuditLogInstrumentationBuilder}
   */
  public AuditLogInstrumentationBuilder withUserProvider(UserProvider userProvider) {
    this.userProvider = userProvider;
    return this;
  }

  /**
   * Enable log anonymizer.
   *
   * @param auditLogAnonymizer {@link AuditLogAnonymizer}
   * @return {@link AuditLogInstrumentationBuilder}
   */
  public AuditLogInstrumentationBuilder withAuditLogAnonymizer(
      AuditLogAnonymizer auditLogAnonymizer) {

    this.auditLogAnonymizer = auditLogAnonymizer;
    return this;
  }

  /**
   * Enable log additional field.
   *
   * @param auditLogAdditionalFieldFetcher {@link AuditLogAdditionalFieldFetcher}
   * @return {@link AuditLogInstrumentationBuilder}
   */
  public AuditLogInstrumentationBuilder withAuditLogAdditionalFieldFetcher(
      AuditLogAdditionalFieldFetcher auditLogAdditionalFieldFetcher) {
    this.auditLogAdditionalFieldFetcher = auditLogAdditionalFieldFetcher;
    return this;
  }

  /**
   * Create the {@link AuditLogInstrumentation} instance
   *
   * @return {@link AuditLogInstrumentation}
   */
  public AuditLogInstrumentation build() {
    return new AuditLogInstrumentation(
        this.clock,
        this.auditLogSender,
        this.userProvider,
        this.auditLogAnonymizer,
        this.auditLogAdditionalFieldFetcher
    );
  }
}

