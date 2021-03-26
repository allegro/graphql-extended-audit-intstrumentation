package pl.allegro.tech.graphql.auditlog;

import pl.allegro.tech.graphql.auditlog.model.UserId;

class AnonymousUserProvider implements UserProvider {

  @Override
  public UserId currentUser() {
    return UserId.ANONYMOUS;
  }
}
