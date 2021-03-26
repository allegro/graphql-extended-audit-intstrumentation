package pl.allegro.tech.graphql.auditlog.model;

public class UserId {
  private final String raw;

  public UserId(String raw) {
    this.raw = raw;
  }

  public String raw() {
    return raw;
  }

  public static final UserId ANONYMOUS = new UserId("anonymousUser");
}
