package com.himanshu.userManagement.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.himanshu.userManagement.services.UserService;
import com.himanshu.userManagement.model.User;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.springframework.web.bind.annotation.PutMapping;


@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public Flux<User> getUsers() {
        // return Flux.just(new User(1, "John", "john@gmail.com", "password123"), new User(2, "Jane", "jane@gmail.com", "password123"));
        return userService.findAllUsers();
        // return userService.findById(18);
    }
    @GetMapping("/{id}")
    public Mono<User>getUserById(@PathVariable Integer id){
        return userService.findById(id);
    }

    @PostMapping
    public Mono<User> createUser(@RequestBody User user) {
        return userService.saveUser(user);
    }

    @DeleteMapping("/{id}")
    public Mono<Boolean> deleteUser(@PathVariable Integer id) {
        return userService.deleteUser(id);
    }

    @PutMapping("/{id}")
    public Mono<User> updateUser(@PathVariable Integer id, @RequestBody User newUser) {    
        return userService.updateUser(id, newUser);
    }
    
}
