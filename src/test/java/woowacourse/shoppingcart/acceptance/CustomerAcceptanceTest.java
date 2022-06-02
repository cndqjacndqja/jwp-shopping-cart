package woowacourse.shoppingcart.acceptance;

import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import woowacourse.auth.dto.TokenRequest;
import woowacourse.auth.dto.TokenResponse;
import woowacourse.shoppingcart.dto.ChangeCustomerRequest;
import woowacourse.shoppingcart.dto.ChangePasswordRequest;
import woowacourse.shoppingcart.dto.CustomerCreateRequest;
import woowacourse.shoppingcart.dto.CustomerResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@DisplayName("회원 관련 기능")
public class CustomerAcceptanceTest extends AcceptanceTest {

    @DisplayName("이메일, 패스워드, 닉네임을 입력 받아 회원가입을 한다.")
    @Test
    void addCustomer() {
        CustomerCreateRequest customerCreateRequest = new CustomerCreateRequest(
            "beomWhale1@naver.com", "범고래1", "Password12345!");

        ExtractableResponse<Response> response = RestAssured.given().log().all()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .body(customerCreateRequest)
            .when()
            .post("/api/customers")
            .then().log().all()
            .extract();

        assertAll(
            () -> assertThat(response.header("Location")).isNotEmpty(),
            () -> assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value())
        );
    }

    @DisplayName("닉네임이 중복될 경우, 예외 응답을 반환한다.")
    @Test
    void duplicateCustomerNickname() {
        CustomerCreateRequest customerCreateRequest = new CustomerCreateRequest(
            "beomWhale2@naver.com", "범고래2", "Password12345!");

        createCustomer(customerCreateRequest);
        ExtractableResponse<Response> response = createCustomer(customerCreateRequest);

        assertAll(
            () -> assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value()),
            () -> assertThat(response.body().jsonPath().getString("message")).isEqualTo(
                "이미 존재하는 닉네임입니다.")
        );
    }

    @DisplayName("회원탈퇴")
    @Test
    void deleteMe() {
        // given: 회원 가입이 되어있다.
        String email = "beomWhale1@naver.com";
        String password = "Password12345!";
        CustomerCreateRequest customerCreateRequest = new CustomerCreateRequest(
            email, "범고래1", password);
        createCustomer(customerCreateRequest);

        TokenRequest tokenRequest = new TokenRequest(email, password);

        String token = createToken(email, password);

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
            .auth().oauth2(token)
            .when().log().all()
            .delete("/api/customers")
            .then().extract();

        // then
        ExtractableResponse<Response> loginResponse = RestAssured.given().log().all()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .body(tokenRequest)
            .when().log().all()
            .post("/api/login")
            .then().log().all()
            .extract();

        assertAll(
            () -> assertThat(response.statusCode()).isEqualTo(HttpStatus.NO_CONTENT.value()),
            () -> assertThat(loginResponse.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value()),
            () -> assertThat(loginResponse.body().jsonPath().getString("message")).isEqualTo(
                "존재하지 않는 회원입니다.")
        );
    }

    private String createToken(String email, String password) {
        TokenRequest tokenRequest = new TokenRequest(email, password);
        TokenResponse tokenResponse = RestAssured.given()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .body(tokenRequest)
            .post("/api/login")
            .then().extract().as(TokenResponse.class);
        return tokenResponse.getAccessToken();
    }

    @DisplayName("이전 패스워드와 새로운 패스워드를 입력받아 새로운 패스워드로 변경한다.")
    @Test
    void changePassword() {
        // given: 회원 가입이 되어있다.
        String email = "beomWhale1@naver.com";
        String password = "Password12345!";
        CustomerCreateRequest customerCreateRequest = new CustomerCreateRequest(
            email, "범고래1", password);
        createCustomer(customerCreateRequest);

        String token = createToken(email, password);

        // when
        String newPassword = "Password123456!";
        ChangePasswordRequest changePasswordRequest = new ChangePasswordRequest(password,
            newPassword);

        ExtractableResponse<Response> response = RestAssured.given().log().all()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .auth().oauth2(token)
            .body(changePasswordRequest)
            .when()
            .patch("/api/customers/password")
            .then().extract();

        ExtractableResponse<Response> loginResponse = RestAssured.given()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .body(new TokenRequest(email, newPassword))
            .post("/api/login")
            .then()
            .extract();

        // then
        assertAll(
            () -> assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value()),
            () -> assertThat(loginResponse.statusCode()).isEqualTo(HttpStatus.OK.value())
        );
    }

    @DisplayName("새로운 닉네임으로 회원정보를 수정한다.")
    @Test
    void changeNickname() {
        // given: 회원 가입이 되어있다.
        String email = "beomWhale1@naver.com";
        String password = "Password12345!";
        CustomerCreateRequest customerCreateRequest = new CustomerCreateRequest(
            email, "범고래1", password);
        createCustomer(customerCreateRequest);

        String token = createToken(email, password);

        // when
        String newNickname = "changed";
        ChangeCustomerRequest changeCustomerRequest = new ChangeCustomerRequest(newNickname);

        ExtractableResponse<Response> response = RestAssured.given().log().all()
            .auth().oauth2(token)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .body(changeCustomerRequest)
            .when().log().all()
            .patch("/api/customers")
            .then().log().all()
            .extract();

        // then
        CustomerResponse customerResponse = RestAssured.given()
            .auth().oauth2(token)
            .when()
            .get("/api/customers/me")
            .then()
            .extract().as(CustomerResponse.class);

        assertAll(
            () -> assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value()),
            () -> assertThat(customerResponse.getNickname()).isEqualTo(newNickname)
        );
    }

    @DisplayName("닉네임 변경시 기존에 존재하는 닉네임과 중복되면 예외를 응답한다.")
    @Test
    void throwExceptionWhenDuplicateNickname() {
        // given
        String nickname = "범고래1";
        createCustomer(
            new CustomerCreateRequest("beomWhale1@naver.com", nickname, "Password12345!"));

        String email = "beomWhale2@naver.com";
        String password = "Password12345!";
        createCustomer(new CustomerCreateRequest(email, "범고래2", password));

        String token = createToken(email, password);

        // when
        ChangeCustomerRequest changeCustomerRequest = new ChangeCustomerRequest(nickname);

        ExtractableResponse<Response> response = RestAssured.given().log().all()
            .auth().oauth2(token)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .body(changeCustomerRequest)
            .when().log().all()
            .patch("/api/customers")
            .then().log().all()
            .extract();

        // then
        assertAll(
            () -> assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value()),
            () -> assertThat(response.body().jsonPath().getString("message")).isEqualTo(
                "이미 존재하는 닉네임입니다.")
        );
    }

    private ExtractableResponse<Response> createCustomer(
        CustomerCreateRequest customerCreateRequest) {
        return RestAssured.given()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .body(customerCreateRequest)
            .when()
            .post("/api/customers")
            .then()
            .extract();
    }
}
