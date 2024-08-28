package bugupsert

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.stringParam
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.upsert
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.testcontainers.containers.PostgreSQLContainer
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class BugUpsertTest {
    private val jdbcUrl = postgres.getJdbcUrl().replace("jdbc:postgresql", "jdbc:pgsql")
    private val username = postgres.username
    private val password = postgres.password

    @BeforeTest
    fun setUp() {
        Database.connect(jdbcUrl, user = username, password = password)
        transaction {
            SchemaUtils.createMissingTablesAndColumns(TestTable)
        }
    }

    @AfterTest
    fun cleanUp() {
        Database.connect(jdbcUrl, user = username, password = password)
        transaction {
            SchemaUtils.drop(TestTable)
        }
    }

    @Test
    fun `must read the right value after update`() {
        Database.connect(jdbcUrl, user = username, password = password)
        val testingContent = """
            This is a content with new line
            and some other difficult string '
            
            Should be preserved
        """.trimIndent()
        transaction {
            TestTable.upsert() { statement ->
                statement[id] = 1
                statement[content] = testingContent
            }
            val savedContent =
                TestTable.select(listOf(TestTable.content)).where({ TestTable.id eq 1 }).first().get(TestTable.content)
            assertEquals(testingContent, savedContent)
            TestTable.upsert(
                TestTable.id,
                onUpdate = listOf(TestTable.content to stringParam(testingContent)),
                where = { TestTable.id eq 1 }
            ) { statement ->
                statement[id] = 1
                statement[content] = testingContent
            }

            val updatedContent =
                TestTable.select(listOf(TestTable.content)).where({ TestTable.id eq 1 }).first().get(TestTable.content)
            assertEquals(testingContent, updatedContent)
        }

    }

    companion object {
        @JvmStatic
        val postgres = PostgreSQLContainer("postgres:16-alpine")

        @JvmStatic
        @BeforeAll
        fun beforeAll() {
            postgres.start()
        }

        @JvmStatic
        @AfterAll
        fun afterAll() {
            postgres.stop()
        }
    }
}

object TestTable : IntIdTable("TestTable") {
    val content = text("content")
}