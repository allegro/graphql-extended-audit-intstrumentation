package pl.allegro.tech.graphql.auditlog

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsData
import com.netflix.graphql.dgs.DgsScalar
import com.netflix.graphql.dgs.InputArgument
import graphql.language.StringValue
import graphql.schema.Coercing
import graphql.schema.CoercingParseLiteralException
import graphql.schema.CoercingParseValueException
import graphql.schema.CoercingSerializeException

@DgsComponent
class UserDataFetcher {
    private final User user = new User(
            new UserId("12345"),
            "login",
            "firstName",
            "lastName",
            "phone",
            new UserSettings(
                    List.of(new Social(SocialName.FACEBOOK)),
                    List.of(new QueryFilter("name", List.of("valueName")))
            )
    )

    private final UserNotFound userNotFound = new UserNotFound(new UserId("54321"))

    @DgsData(parentType = "Query", field = "user")
    UserCandidate user(@InputArgument("userId") UserId userId, @InputArgument("otherArgument") String otherArgument) {
        if (userId.raw.contains("12345")){
            return user
        }else {
            return userNotFound
        }
    }

    @DgsData(parentType = "Query", field = "userWithRuntimeException")
    UserCandidate userWithRuntimeException(@InputArgument("userId") UserId userId) {
        throw new RuntimeException("An error occurred when creating request to service")
    }
}

@DgsScalar(name = "UserId")
class UserIdScalar implements Coercing<UserId, String> {

    @Override
    String serialize(Object dataFetcherResult) throws CoercingSerializeException {
        if (dataFetcherResult instanceof UserId) {
            return ((UserId) dataFetcherResult).raw
        } else {
            throw new CoercingSerializeException("Not a valid UserId");
        }
    }

    @Override
    UserId parseValue(Object input) throws CoercingParseValueException {
        return new UserId(input.toString())
    }

    @Override
    UserId parseLiteral(Object input) throws CoercingParseLiteralException {
        if (input instanceof StringValue) {
            return new UserId(input.toString())
        }

        throw new CoercingParseLiteralException("Value is not a valid ");
    }
}

interface UserCandidate {}

class UserId {
    String raw

    @JsonCreator
    UserId(@JsonProperty("raw") String raw) {
        this.raw = raw
    }

    String getRaw() {
        return raw
    }

    void setRaw(String raw) {
        this.raw = raw
    }
}

class UserNotFound implements UserCandidate {
    private final UserId userId

    UserNotFound(UserId userId) {
        this.userId = userId
    }

    UserId getUserId() {
        return userId
    }
}

class User implements UserCandidate {
    private final UserId userId
    private final String login
    private final String firstName
    private final String lastName
    private final String phone
    private final UserSettings userSettings

    User(
            UserId userId,
            String login,
            String firstName,
            String lastName,
            String phone,
            UserSettings userSettings
    ) {
        this.userId = userId
        this.login = login
        this.firstName = firstName
        this.lastName = lastName
        this.phone = phone
        this.userSettings = userSettings
    }

    UserId getUserId() {
        return userId
    }

    String getLogin() {
        return login
    }

    String getFirstName() {
        return firstName
    }

    String getLastName() {
        return lastName
    }

    String getPhone() {
        return phone
    }

    UserSettings getUserSettings() {
        return userSettings
    }
}

class QueryFilter {
    private final String name
    private final List<String> valueNames

    QueryFilter(String name, List<String> valueNames) {
        this.name = name
        this.valueNames = valueNames
    }

    String getName() {
        return name
    }

    List<String> getValueNames() {
        return valueNames
    }
}

class UserSettings {
    private final List<Social> socials
    private final List<QueryFilter> filters

    UserSettings(List<Social> socials, List<QueryFilter> filters) {
        this.socials = socials
        this.filters = filters
    }

    List<Social> getSocials() {
        return socials
    }

    List<QueryFilter> getFilters() {
        return filters
    }
}

class Social {
    private final SocialName name

    Social(SocialName name) {
        this.name = name
    }

    SocialName getSocialName() {
        return name
    }
}

enum SocialName {
    FACEBOOK,
    GOOGLE
}
