graphql-audit
=====

*graphql audit lib*

GraphQL auditing is not out of the box feature. Our lib does all the works to instrument auditing.

## Basic usage

Use ``AuditLogInstrumentationCreator`` to create Bean in your app.

```
@Configuration
class AuditLogInstrumentationConfig {

    @Bean
    AuditLogInstrumentation auditLogInstrumentation(
              InMemoryAuditLogSender inMemoryAuditLogSender,
              UserProvider userProvider
              ){
        return new AuditLogInstrumentationCreator()
                .withActionLogSender(inMemoryAuditLogSender)
                .withUserProvider(userProvider)
                .create()
    }
}
```

## Additional configuration

GraphQL audit creator can take extra option like

- custom audit log destination data source
  Please implement ``AuditLogSender``

```
 /**
   * Enable sending audit log;
   *
   * @param auditLogSender {@link AuditLogSender}
   * @return {@link AuditLogInstrumentationCreator}
   */
  public AuditLogInstrumentationCreator withActionLogSender(AuditLogSender auditLogSender) {
    this.auditLogSender = auditLogSender;
    return this;
  }
 ```

- custom user context provider. Please implement interface ``UserProvider`` 

```
  /**
   * Set user context provider.
   *
   * @param userProvider {@link UserProvider}
   * @return {@link AuditLogInstrumentationCreator}
   */
  public AuditLogInstrumentationCreator withUserProvider(UserProvider userProvider) {
    this.userProvider = userProvider;
    return this;
  }
```

- anonymization can be enabled to not send data which you can easily pick up.
  Please implement interface ``AnonymizedFieldsSetupProvider``

```
  /**
   * Enable log anonymizer.
   *
   * @param auditLogAnonymizer {@link AuditLogAnonymizer}
   * @return {@link AuditLogInstrumentationCreator}
   */
  public AuditLogInstrumentationCreator withAuditLogAnonymizer(
      AuditLogAnonymizer auditLogAnonymizer) {
    this.auditLogAnonymizer = auditLogAnonymizer;
    return this;
  }
```

- extra field like input query field can be logged.
  Please implement interface ``AdditionalFieldsSetupProvider``
```
  /**
   * Enable log additional field.
   *
   * @param auditLogAdditionalFieldFetcher {@link AuditLogAdditionalFieldFetcher}
   * @return {@link AuditLogInstrumentationCreator}
   */
  public AuditLogInstrumentationCreator withAuditLogAdditionalFieldFetcher(
      AuditLogAdditionalFieldFetcher auditLogAdditionalFieldFetcher) {
    this.auditLogAdditionalFieldFetcher = auditLogAdditionalFieldFetcher;
    return this;
  }
```

## License

**graphql-audit** is published under [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0).
