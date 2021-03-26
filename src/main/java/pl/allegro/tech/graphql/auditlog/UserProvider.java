package pl.allegro.tech.graphql.auditlog;

import pl.allegro.tech.graphql.auditlog.model.UserId;

public interface UserProvider {

  UserId currentUser();
}
