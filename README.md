graphql-audit
=====
![Java CI with Gradle status](https://github.com/allegro/graphql-extended-audit-intstrumentation/actions/workflows/ci.yml/badge.svg?branch=master)

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

You can provide your own implementation of ``AuditLogSender`` to sent logs somewhere (i.e. database, event queue, or a file).

Implement ``AuditLogSender``

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

Implement interface ``UserProvider`` 

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

Anonymization can be enabled to not send data that you can easily pick up.
This function ensures compliance with the log audit with the GDPR.
Thanks to it you can, for example, anonymize personal data in an audit log.

Implement interface ``AnonymizedFieldsSetupProvider``

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

An extra field like an input query field can be logged.
It can be useful in situations where we want the audit log to include fields even if the client does not ask for them.
For example, the client fetches the user's email, and we want that user's ID to be included in each audit log containing user entity regardless of the client fetches the user ID.

Implement interface ``AdditionalFieldsSetupProvider``

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
