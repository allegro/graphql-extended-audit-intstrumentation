graphql-audit
=====

*graphql audit lib*

GraphQL auditing is not out of the box feature. Our lib does all the works to instrument auditing.

## Basic usage

Use ``AuditLogInstrumentationBuilder`` to create Bean in your app.

```
@Configuration
class AuditLogInstrumentationConfig {

    @Bean
    AuditLogInstrumentation auditLogInstrumentation(
              InMemoryAuditLogSender inMemoryAuditLogSender,
              UserProvider userProvider
              ){
        return new AuditLogInstrumentationBuilder()
                .withActionLogSender(inMemoryAuditLogSender)
                .withUserProvider(userProvider)
                .create()
    }
}
```

## Additional configuration

GraphQL audit creator can take extra option like

### Custom audit log destination data source

You can provide your own implementation of ``AuditLogSender`` so that the logs can, for example, be sent to the database, to the event queue or to a file.

Please implement ``AuditLogSender``

```
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
 ```

### Custom user context provider

Please implement interface ``UserProvider`` 

```
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
```

### Anonymization

Anonymization can be enabled to not send data which you can easily pick up.
This function ensures compliance of the log audit with the GDPR.
Thanks to it, you can, for example, anonymize personal data in an audit log.

Please implement interface ``AnonymizedFieldsSetupProvider``

```
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
```

### Extra field

Extra field like input query field can be logged.
It can be useful in situations where we want the audit log to include fields even if the client does not ask for them, 
for example, the client fetch the user's email, and we want that users ID to be included in each audit log containing user entity regardless the client fetch the user ID.
Please implement interface ``AdditionalFieldsSetupProvider``

```
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
```

## License

**graphql-audit** is published under [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0).
