package pl.allegro.tech.graphql.auditlog


import com.netflix.graphql.dgs.internal.DgsSchemaProvider
import graphql.schema.GraphQLSchema
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AuditLogConfig {

    def schema = """

            scalar UserId
            
            type UserNotFound {
                    userId: UserId
                }

            union UserCandidate = User | UserNotFound

            type User {
                userId: UserId
                login: String
                firstName: String
                lastName: String
                phone: String,
                userSettings: UserSettings
                }
                
            type UserSettings {
                socials: [Social!]
                codesApplication: Boolean
                filters: [QueryFilter!]
                }
            
            type QueryFilter {
                name: String
                valueNames: [String!]
            }    
                
            type Social {
                name: SocialName
            }
            
            enum SocialName {
                FACEBOOK
                GOOGLE
            }
            
            type AddUserPayload {
              created: Boolean
            }    

            type Query {
                user(userId : UserId!, otherArgument: String): UserCandidate
                userWithRuntimeException(userId : UserId!): UserCandidate 
            }
            
            type Mutation {
                addUser(firstName: String!, lastName: String!, email: String!): AddUserPayload
                likeUser(userId: UserId!): UserId
                likeUserWithNull(userId: UserId!): UserId
            }
            """

    @Bean
    DgsSchemaProvider dgsSchemaProvider(ApplicationContext applicationContext) {

        def provider = new DgsSchemaProvider(
                applicationContext,
                Optional.empty(),
                Optional.empty(),
                Optional.empty()
        )
        provider.schema(schema)
        return provider
    }

    @Bean
    GraphQLSchema schema(DgsSchemaProvider dgsSchemaProvider) {
        return dgsSchemaProvider.schema(schema)
    }
}
