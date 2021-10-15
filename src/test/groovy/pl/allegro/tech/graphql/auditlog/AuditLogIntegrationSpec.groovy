package pl.allegro.tech.graphql.auditlog

import com.netflix.graphql.dgs.DgsQueryExecutor
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.support.AnnotationConfigContextLoader
import pl.allegro.tech.graphql.auditlog.config.AuditLogConfig
import pl.allegro.tech.graphql.auditlog.graphql.UserDataFetcher
import pl.allegro.tech.graphql.auditlog.graphql.UserMutation
import pl.allegro.tech.graphql.auditlog.model.ResultData
import pl.allegro.tech.graphql.auditlog.root.IntegrationSpec

import javax.inject.Inject

import static pl.allegro.tech.graphql.auditlog.AuditLogItem.OperationType.MUTATION
import static pl.allegro.tech.graphql.auditlog.AuditLogItem.OperationType.QUERY

@ContextConfiguration(classes = [AuditLogConfig.class, UserDataFetcher.class, UserMutation.class], loader = AnnotationConfigContextLoader.class)
class AuditLogIntegrationSpec extends IntegrationSpec {

    @Inject
    DgsQueryExecutor dgsQueryExecutor

    def "Should log audit if result data contains list of objects"() {

        when: "I fetch some data"
        def executionResult = dgsQueryExecutor.execute("""{
                                user(userId: "$existingUserId"){
                                        ... on User {
                                        userId
                                        userSettings {
                                            socials {
                                                name
                                            }
                                        }
                                      }
                                }
                              }""")

        then: "no errors occurred"
        executionResult.errors.isEmpty()

        and: "data is present"
        executionResult.isDataPresent()

        and: "audit log contains one record"
        actionLog.sendAuditLogItems().size() == 1
        AuditLogItem logItem = actionLog.sendAuditLogItems()[0]

        and: "audit log item result data contains one root element"
        logItem.resultData().size() == 1
        ResultData.ObjectResultData rootResultData = logItem.resultData()[0]

        and: "audit log contains result data"
        rootResultData.getName() == "user"
        rootResultData.getTypeName() == "User"
        rootResultData.getFields() as Set == [
                new ResultData.ScalarResultData("userId", "UserId", "$existingUserId"),
                new ResultData.ObjectResultData("userSettings", "UserSettings", [
                        new ResultData.ArrayResultData("socials", [
                                new ResultData.ObjectResultData("0", "Social", [
                                        new ResultData.ScalarResultData("name", "SocialName", "FACEBOOK")
                                ])
                        ])
                ])
        ].toSet()
    }

    def "Should log audit if result data return union type"() {

        when: "I fetch some data"
        def executionResult = dgsQueryExecutor.execute("""{
                                user(userId: "$noExistingUserId"){
                                       ... on  User {
                                            userId
                                       }
                                       ... on UserNotFound {
                                            userId
                                       }
                                }
                              }""")

        then: "no errors occurred"
        executionResult.errors.isEmpty()

        and: "data is present"
        executionResult.isDataPresent()

        and: "audit log contains one record"
        actionLog.sendAuditLogItems().size() == 1
        AuditLogItem logItem = actionLog.sendAuditLogItems()[0]

        and: "audit log item result data contains one root element"
        logItem.resultData().size() == 1
        ResultData.ObjectResultData rootResultData = logItem.resultData()[0]

        and: "audit log contains result data"
        rootResultData.getName() == "user"
        rootResultData.getTypeName() == "UserNotFound"
        rootResultData.getType() == "object"
        rootResultData.getFields() as Set == [
                new ResultData.ScalarResultData("userId", "UserId", "$noExistingUserId")
        ].toSet()
    }

    def "Should log audit if request failed and result data is null"() {

        when: "I fetch some data"
        def executionResult = dgsQueryExecutor.execute("""{
                                userWithRuntimeException(userId: "$existingUserId"){
                                        ... on User {
                                        userId
                                      }
                                }
                              }""")

        then: "errors occurred"
        executionResult.errors.size() > 0

        and: "data is present"
        executionResult.data == ["userWithRuntimeException": null]

        and: "audit log contains one record"
        actionLog.sendAuditLogItems().size() == 1
        AuditLogItem logItem = actionLog.sendAuditLogItems()[0]

        and: "audit log item result data contains one root element"
        logItem.resultData().size() == 1
        ResultData.NullResultData rootResultData = logItem.resultData()[0]

        and: "audit log contains result data"
        rootResultData.getName() == "userWithRuntimeException"
        rootResultData.getTypeName() == "null"
        rootResultData.getType() == "null"

        and: "audit log contains error data"
        logItem.errors() == [
                [
                        "message"   : "java.lang.RuntimeException: An error occurred when creating request to service",
                        "locations" : [],
                        "path"      : ["userWithRuntimeException"],
                        "extensions": ["errorType": "INTERNAL"]
                ]
        ]

        and: "audit log contains query name"
        logItem.operations() == [
                new AuditLogItem.Operation(
                        QUERY,
                        "userWithRuntimeException",
                        ["userId": existingUserId])
        ]
    }

    def "Should log original name when aliases are used"() {
        when: "I fetch some data"
        def executionResult = dgsQueryExecutor.execute("""{
                                u: user(userId: "$existingUserId"){
                                        ... on User {
                                     i: userId
                                        userSettings {
                                          s: socials {
                                             n: name
                                            }
                                        }
                                      }
                                }
                              }""")

        then: "no errors occurred"
        executionResult.errors.isEmpty()

        and: "data is present"
        executionResult.isDataPresent()

        and: "audit log contains one record"
        actionLog.sendAuditLogItems().size() == 1
        AuditLogItem logItem = actionLog.sendAuditLogItems()[0]

        and: "audit log item result data contains one root element"
        logItem.resultData().size() == 1
        ResultData.ObjectResultData rootResultData = logItem.resultData()[0]

        and: "audit log contains result data"
        rootResultData.getName() == "user"
        rootResultData.getTypeName() == "User"
        rootResultData.getFields() as Set == [
                new ResultData.ScalarResultData("userId", "UserId", "$existingUserId"),
                new ResultData.ObjectResultData("userSettings", "UserSettings", [
                        new ResultData.ArrayResultData("socials", [
                                new ResultData.ObjectResultData("0", "Social", [
                                        new ResultData.ScalarResultData("name", "SocialName", "FACEBOOK")
                                ])
                        ])
                ])
        ].toSet()
    }

    def "Should log two audits if one request contains two query"() {
        when: "I fetch data of two users"
        def executionResult = dgsQueryExecutor.execute("""{
                               user1: user(userId: "$existingUserId"){
                                        ... on User {
                                        userId
                                        userSettings {
                                          socials {
                                              name
                                            }
                                        }
                                      }
                                }
                                
                               user2: user(userId: "$noExistingUserId"){
                                         ... on  User {
                                            userId
                                       }
                                       ... on UserNotFound {
                                            userId
                                       }
                               }                            
                              }""")

        then: "no errors occurred"
        executionResult.errors.isEmpty()

        and: "data is present"
        executionResult.isDataPresent()

        and: "audit log contains one record"
        actionLog.sendAuditLogItems().size() == 1
        AuditLogItem logItem = actionLog.sendAuditLogItems()[0]

        and: "audit log item result data contains two root elements"
        logItem.resultData().size() == 2
        ResultData.ObjectResultData firstRootResultData = logItem.resultData()[0]
        ResultData.ObjectResultData secondRootResultData = logItem.resultData()[1]

        and: "audit log contains result data"
        firstRootResultData.getName() == "user"
        firstRootResultData.getTypeName() == "User"
        firstRootResultData.getFields() as Set == [
                new ResultData.ScalarResultData("userId", "UserId", "$existingUserId"),
                new ResultData.ObjectResultData("userSettings", "UserSettings", [
                        new ResultData.ArrayResultData("socials", [
                                new ResultData.ObjectResultData("0", "Social", [
                                        new ResultData.ScalarResultData("name", "SocialName", "FACEBOOK")
                                ])
                        ])
                ])
        ].toSet()
        secondRootResultData.getName() == "user"
        secondRootResultData.getTypeName() == "UserNotFound"
        secondRootResultData.getType() == "object"
        secondRootResultData.getFields() as Set == [
                new ResultData.ScalarResultData("userId", "UserId", "$noExistingUserId")
        ].toSet()

        and: "audit log contains query name"
        logItem.operations() == [
                new AuditLogItem.Operation(
                        QUERY,
                        "user",
                        ["userId": existingUserId]),
                new AuditLogItem.Operation(
                        QUERY,
                        "user",
                        ["userId": noExistingUserId])
        ]
    }

    def "Should log audit for mutations"() {
        given: "user data"
        String firstName = "Tom"
        String lastName = "Morgan"
        String email = "t.morgan@devil.com"
        when: "I send mutation to add data"
        def executionResult = dgsQueryExecutor.execute("""
                                  mutation {
                                   addUser(firstName: "$firstName", lastName: "$lastName", email: "$email"){
                                        created
                                    }
                                  }
                              """)

        then: "no errors occurred"
        executionResult.errors.isEmpty()

        and: "data is present"
        executionResult.isDataPresent()

        and: "audit log contains one record"
        actionLog.sendAuditLogItems().size() == 1
        AuditLogItem logItem = actionLog.sendAuditLogItems()[0]

        and: "audit log item result data contains root element"
        logItem.resultData().size() == 1
        ResultData.ObjectResultData rootResultData = logItem.resultData()[0]

        and: "audit log contains result data"
        rootResultData.getName() == "addUser"
        rootResultData.getTypeName() == "AddUserPayload"
        rootResultData.getFields().size() == 1

        and: "audit log contains query name"
        logItem.operations() == [
                new AuditLogItem.Operation(
                        MUTATION,
                        "addUser",
                        ["firstName": firstName, "lastName": lastName, "email": email])
        ]
    }

    def "Should log audit if result data is a scalar"() {
        given: "Some userId"
        String userId = "12345"
        when: "I send mutation to like user"
        def executionResult = dgsQueryExecutor.execute("""
                                  mutation {
                                   likeUser(userId: "$userId")
                                  }
                              """)

        then: "no errors occurred"
        executionResult.errors.isEmpty()

        and: "data is present"
        executionResult.isDataPresent()

        and: "audit log contains one record"
        actionLog.sendAuditLogItems().size() == 1
        AuditLogItem logItem = actionLog.sendAuditLogItems()[0]

        and: "audit log item result data contains root element"
        logItem.resultData().size() == 1
        ResultData.ScalarResultData rootResultData = logItem.resultData()[0]

        and: "audit log contains result data"
        rootResultData.getName() == "likeUser"
        rootResultData.getTypeName() == "UserId"
        rootResultData.getType() == "scalar"
        rootResultData.getValue() == userId

        and: "audit log contains query name"
        logItem.operations() == [
                new AuditLogItem.Operation(
                        MUTATION,
                        "likeUser",
                        ["userId": userId])
        ]
    }

    def "Should log action  if result data is null"() {
        given: "some userId"
        String userId = "12345"

        when: "I send mutation to like user"
        def executionResult = dgsQueryExecutor.execute("""
                                  mutation {
                                   likeUserWithNull(userId: "$userId")
                                  }
                              """)

        then: "no errors occurred"
        executionResult.errors.isEmpty()

        and: "data is present"
        executionResult.isDataPresent()

        and: "data contains null"
        executionResult.data == [likeUserWithNull: null]

        and: "audit log contains one record"
        actionLog.sendAuditLogItems().size() == 1
        AuditLogItem logItem = actionLog.sendAuditLogItems()[0]

        and: "audit log contains query name"
        logItem.operations() == [
                new AuditLogItem.Operation(
                        MUTATION,
                        "likeUserWithNull",
                        ["userId": userId])
        ]

        and: "action log item result data contains empty list"
        logItem.resultData() == [new ResultData.NullResultData("likeUserWithNull")]
    }

    def "Should log audit if result data contains list of strings"() {

        when: "I fetch some data"
        def executionResult = dgsQueryExecutor.execute("""{
                                user(userId: "$existingUserId"){
                                        ... on User {
                                        userId
                                        userSettings {
                                            filters {
                                                valueNames                                            
                                            }
                                        }
                                      }
                                }
                              }""")

        then: "no errors occurred"
        executionResult.errors.isEmpty()

        and: "data is present"
        executionResult.isDataPresent()

        and: "audit log contains one record"
        actionLog.sendAuditLogItems().size() == 1
        AuditLogItem logItem = actionLog.sendAuditLogItems()[0]

        and: "audit log item result data contains one root element"
        logItem.resultData().size() == 1
        ResultData.ObjectResultData rootResultData = logItem.resultData()[0]

        and: "audit log contains query data"
        logItem.operations() == [
                new AuditLogItem.Operation(
                        QUERY,
                        "user",
                        ["userId": existingUserId])
        ]

        and: "audit log contains result data"
        rootResultData.getName() == "user"
        rootResultData.getTypeName() == "User"
        rootResultData.getFields() as Set == [
                new ResultData.ScalarResultData("userId", "UserId", "$existingUserId"),
                new ResultData.ObjectResultData("userSettings", "UserSettings", [
                        new ResultData.ArrayResultData("filters", [
                                new ResultData.ObjectResultData("0", "QueryFilter", [
                                        new ResultData.ArrayResultData("valueNames", [new ResultData.ScalarResultData("0", "String", "valueName")])
                                ])
                        ])
                ])
        ].toSet()
    }

    def "Should log audit with resolved variables"() {

        when: "I fetch some data"
        def executionResult = dgsQueryExecutor.execute("""
                                query User(\$userId: UserId!) {
                                            user(userId: \$userId) {
                                        ... on User {
                                        userId
                                       }
                                            }
                                }
                              """, ["userId": existingUserId] as Map)

        then: "no errors occurred"
        executionResult.errors.isEmpty()

        and: "data is present"
        executionResult.isDataPresent()

        and: "audit log contains one record"
        actionLog.sendAuditLogItems().size() == 1
        AuditLogItem logItem = actionLog.sendAuditLogItems()[0]

        and: "audit log item result data contains one root element"
        logItem.resultData().size() == 1
        ResultData.ObjectResultData rootResultData = logItem.resultData()[0]

        and: "AUDIT log contains query arguments with resolver variables"
        logItem.operations() == [
                new AuditLogItem.Operation(
                        QUERY,
                        "user",
                        ["userId": existingUserId])
        ]

        and: "audit log contains result data"
        rootResultData.getName() == "user"
        rootResultData.getTypeName() == "User"
        rootResultData.getFields() as Set == [
                new ResultData.ScalarResultData("userId", "UserId", "$existingUserId")
        ].toSet()
    }

    def "Should log audit when null is added by graphql variables"() {

        when: "I fetch some data"
        def executionResult = dgsQueryExecutor.execute("""
                                query User(\$userId: UserId!, \$otherArgument: String) {
                                            user(userId: \$userId, otherArgument: \$otherArgument) {
                                        ... on User {
                                        userId
                                       }
                                            }
                                }
                              """, ["userId": existingUserId, "otherArgument": null] as Map)

        then: "no errors occurred"
        executionResult.errors.isEmpty()

        and: "data is present"
        executionResult.isDataPresent()

        and: "audit log contains one record"
        actionLog.sendAuditLogItems().size() == 1
        AuditLogItem logItem = actionLog.sendAuditLogItems()[0]

        and: "audit log contains operation with null variables"
        logItem.operations() == [
                new AuditLogItem.Operation(
                        QUERY,
                        "user",
                        ["userId": existingUserId, "otherArgument": Optional.empty()])
        ]
    }
}
