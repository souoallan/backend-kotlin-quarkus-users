package skeleton

import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import org.hamcrest.CoreMatchers.equalTo
import org.junit.jupiter.api.Test

@QuarkusTest
class UserResourceTest {

    @Test
    fun testGetAllUsers() {
        given()
            .`when`().get("/api/users")
            .then()
            .statusCode(200)
    }

    // Adicione mais testes conforme necessário
    // Por exemplo, testes para criação, atualização e exclusão de usuários
    // Esses testes exigiriam configuração adequada do banco de dados de teste
} 