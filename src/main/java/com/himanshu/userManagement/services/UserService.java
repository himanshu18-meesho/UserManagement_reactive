package com.himanshu.userManagement.services;

import org.springframework.stereotype.Service;
import com.himanshu.userManagement.model.User;
import com.himanshu.userManagement.repository.UserRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class UserService {
    private final UserRepository userRepository;
    
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    public Mono<User> saveUser(User user) {
        return userRepository.save(user);
    }
    
    public Mono<User> findById(Integer id) {
        return userRepository.findById(id);
    }
    
    public Flux<User> findAllUsers() {
        return userRepository.findAll();
    }
    
    public Mono<Boolean> deleteUser(Integer id) {
        return userRepository.deleteById(id);
    }

    public Mono<User> updateUser(Integer id, User newUser) {
        return userRepository.updateUser(id, newUser);
    }
}
