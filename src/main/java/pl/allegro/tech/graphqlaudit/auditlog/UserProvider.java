package pl.allegro.tech.graphqlaudit.auditlog;

import pl.allegro.tech.graphqlaudit.auditlog.model.UserId;

public interface UserProvider {

  UserId currentUser();
}
