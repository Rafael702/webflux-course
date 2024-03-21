package br.com.udemy.webfluxcourse.controller;

import br.com.udemy.webfluxcourse.entity.User;
import br.com.udemy.webfluxcourse.mapper.UserMapper;
import br.com.udemy.webfluxcourse.model.request.UserRequest;
import br.com.udemy.webfluxcourse.model.response.UserResponse;
import br.com.udemy.webfluxcourse.service.UserService;
import com.mongodb.reactivestreams.client.MongoClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.BodyInserters.fromValue;
import static reactor.core.publisher.Mono.just;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureWebTestClient
class UserControllerImplTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private UserService service;

    @MockBean
    private UserMapper mapper;

    @MockBean
    private MongoClient mongoClient;

    @Test
    @DisplayName("Test endpoint save with success")
    void testSaveWithSuccess() {
        UserRequest request = new UserRequest("Rafael", "rafael@email.com", "123");
        when(service.save(any(UserRequest.class))).thenReturn(just(User.builder().build()));

        webTestClient.post().uri("/users")
                .contentType(APPLICATION_JSON)
                .body(fromValue(request))
                .exchange()
                .expectStatus().isCreated();

        verify(service, times(1)).save(any(UserRequest.class));
    }

    @Test
    @DisplayName("Test endpoint save with bad request when name is invalid")
    void testSaveWithBadRequestWhenNameIsInvalid() {
        UserRequest request = new UserRequest(" Rafael", "rafael@email.com", "123");

        webTestClient.post().uri("/users")
                .contentType(APPLICATION_JSON)
                .body(fromValue(request))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.path").isEqualTo("/users")
                .jsonPath("$.status").isEqualTo(BAD_REQUEST.value())
                .jsonPath("$.error").isEqualTo("Validation Error")
                .jsonPath("$.message").isEqualTo("Error on validation attributes")
                .jsonPath("$.errors[0].fieldName").isEqualTo("name")
                .jsonPath("$.errors[0].message").isEqualTo("field cannot have blank space at the beginning or at end");

    }

    @Test
    @DisplayName("Test endpoint save with bad request when password is invalid")
    void testSaveWithBadRequestWhenPasswordIsInvalid() {
        UserRequest request = new UserRequest("Rafael", "rafael@email.com", " 123");

        webTestClient.post().uri("/users")
                .contentType(APPLICATION_JSON)
                .body(fromValue(request))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.path").isEqualTo("/users")
                .jsonPath("$.status").isEqualTo(BAD_REQUEST.value())
                .jsonPath("$.error").isEqualTo("Validation Error")
                .jsonPath("$.message").isEqualTo("Error on validation attributes")
                .jsonPath("$.errors[0].fieldName").isEqualTo("password")
                .jsonPath("$.errors[0].message").isEqualTo("field cannot have blank space at the beginning or at end");

    }

    @Test
    @DisplayName("Test endpoint save with bad request when email is invalid")
    void testSaveWithBadRequestWhenEmailIsInvalid() {
        UserRequest request = new UserRequest("Rafael", "rafaelemail.com", "123");

        webTestClient.post().uri("/users")
                .contentType(APPLICATION_JSON)
                .body(fromValue(request))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.path").isEqualTo("/users")
                .jsonPath("$.status").isEqualTo(BAD_REQUEST.value())
                .jsonPath("$.error").isEqualTo("Validation Error")
                .jsonPath("$.message").isEqualTo("Error on validation attributes")
                .jsonPath("$.errors[0].fieldName").isEqualTo("email")
                .jsonPath("$.errors[0].message").isEqualTo("invalid email");
    }

    @Test
    @DisplayName("Test find by id endpoint with success")
    void testFindByIdWithSuccess() {
        final var id = "123456";
        final var userResponse = new UserResponse(id, "Rafael", "rafael@mail.com", "123");
        when(service.findById(anyString())).thenReturn(Mono.just(User.builder().build()));
        when(mapper.toResponse(any(User.class))).thenReturn(userResponse);

        webTestClient.get().uri("/users/" + id)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(id)
                .jsonPath("$.name").isEqualTo(userResponse.name())
                .jsonPath("$.email").isEqualTo(userResponse.email())
                .jsonPath("$.password").isEqualTo(userResponse.password());

        verify(service, times(1)).findById(anyString());
        verify(mapper, times(1)).toResponse(any(User.class));

    }

    @Test
    @DisplayName("Test find all endpoint with success")
    void testFindAllWithSuccess() {
        final var id = "123456";
        final var userResponse = new UserResponse(id, "Rafael", "rafael@mail.com", "123");
        when(service.findAll()).thenReturn(Flux.just(User.builder().build()));
        when(mapper.toResponse(any(User.class))).thenReturn(userResponse);

        webTestClient.get().uri("/users")
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].id").isEqualTo(id)
                .jsonPath("$[0].name").isEqualTo(userResponse.name())
                .jsonPath("$[0].email").isEqualTo(userResponse.email())
                .jsonPath("$[0].password").isEqualTo(userResponse.password());

        verify(service, times(1)).findAll();
        verify(mapper, times(1)).toResponse(any(User.class));
    }

    @Test
    @DisplayName("Test update endpoint with success")
    void update() {
        final var id = "123456";
        UserRequest request = new UserRequest("Rafael", "rafael@email.com", "123");
        final var userResponse = new UserResponse(id, "Rafael", "rafael@mail.com", "123");

        when(service.update(anyString(), any(UserRequest.class))).thenReturn(just(User.builder().build()));
        when(mapper.toResponse(any(User.class))).thenReturn(userResponse);


        webTestClient.patch().uri("/users/" + id)
                .contentType(APPLICATION_JSON)
                .body(fromValue(request))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(userResponse.id())
                .jsonPath("$.name").isEqualTo(userResponse.name())
                .jsonPath("$.email").isEqualTo(userResponse.email())
                .jsonPath("$.password").isEqualTo(userResponse.password());

        verify(service, times(1)).update(anyString(),any(UserRequest.class));
        verify(mapper, times(1)).toResponse(any(User.class));
    }

    @Test
    void delete() {
    }
}