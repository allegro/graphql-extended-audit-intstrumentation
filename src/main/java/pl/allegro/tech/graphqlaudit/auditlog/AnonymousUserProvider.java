package pl.allegro.tech.graphqlaudit.auditlog;

import pl.allegro.tech.graphqlaudit.auditlog.model.UserId;

class AnonymousUserProvider implements UserProvider {

  @Override
  public UserId currentUser() {
    return UserId.ANONYMOUS;
  }
}
