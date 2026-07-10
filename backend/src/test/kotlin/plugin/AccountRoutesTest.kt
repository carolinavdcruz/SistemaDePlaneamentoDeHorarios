package plugin

import io.ktor.server.testing.testApplication
import testsuport.TestDatabase
import kotlin.test.BeforeTest
import kotlin.test.Test

/**
 * Testes de integração HTTP (via testApplication) para registo, login e
 * associação professor-aluno. Usa a mesma BD H2 partilhada dos outros testes.
 */
class AccountRoutesTest {

    @BeforeTest
    fun setup() {
        TestDatabase.reset()
    }

    @Test
    fun `registar professor com sucesso devolve 201 e o id criado`() = testApplication {
        TODO()
    }

    @Test
    fun `registar professor com email repetido devolve 409 com mensagem amigavel`() = testApplication {
        TODO()
    }

    @Test
    fun `registar aluno e associar a um professor`() = testApplication {
        TODO()
    }

    @Test
    fun `login com credenciais corretas devolve userId e ownerType`() = testApplication {
        TODO()
    }

    @Test
    fun `login com password errada e rejeitado com 401`() = testApplication {
        TODO()
    }
}