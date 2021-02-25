package pl.allegro.tech.graphqlaudit.auditlog

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsData
import com.netflix.graphql.dgs.InputArgument

@DgsComponent
class UserMutation {
    @DgsData(parentType = "Mutation", field = "addUser")
    AddUserPayload addUser(
            @InputArgument("firstName") String firstName,
            @InputArgument("lastName") String lastName,
            @InputArgument("email") String email
    ) {
        return new AddUserPayload(true)
    }

    @DgsData(parentType = "Mutation", field = "likeUser")
    UserId likeUser(@InputArgument("userId") UserId UserId) {
        return new UserId("12345")
    }

    @DgsData(parentType = "Mutation", field = "likeUserWithNull")
    UserId likeUserWitNull(@InputArgument("userId") UserId UserId) {
        return null
    }
}

class AddUserPayload {
    private Boolean created

    AddUserPayload(Boolean created) {
        this.created = created
    }

    Boolean getCreated() {
        return created
    }

    void setCreated(Boolean created) {
        this.created = created
    }
}
