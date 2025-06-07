package com.himanshu.userManagement.repository;

import com.himanshu.userManagement.model.User;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UserRepositoryInterface {
    Mono<User>save(User user);
    Mono<User>findById(Integer id);
    Flux<User>findAll();
    Mono<Boolean>deleteById(Integer id);
    Mono<User>updateUser(Integer id, User newUser);
    Mono<Boolean>userExists(Integer id);
}
