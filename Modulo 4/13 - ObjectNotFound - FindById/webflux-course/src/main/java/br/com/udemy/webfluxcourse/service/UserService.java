package br.com.udemy.webfluxcourse.service;

import br.com.udemy.webfluxcourse.entity.User;
import br.com.udemy.webfluxcourse.mapper.UserMapper;
import br.com.udemy.webfluxcourse.model.request.UserRequest;
import br.com.udemy.webfluxcourse.repository.UserRepository;
import br.com.udemy.webfluxcourse.service.exception.ObjectNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository repository;
    private final UserMapper mapper;

    public Mono<User> save(final UserRequest request) {
        return repository.save(mapper.toEntity(request));
    }

    public Mono<User> findById(final String id) {
        return repository.findById(id).switchIfEmpty(
                Mono.error(new ObjectNotFoundException(
                        String.format("Object not found. Id: %s, Type: %s", id, User.class.getSimpleName())
                ))
        );
    }
}
