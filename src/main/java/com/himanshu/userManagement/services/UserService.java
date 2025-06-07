package com.himanshu.userManagement.services;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import com.himanshu.userManagement.model.User;
import com.himanshu.userManagement.repository.ElasticsearchUserRepository;
import com.himanshu.userManagement.repository.RedisUserRepository;
import reactor.core.publisher.Mono;

@Service
public class UserService {
    // private final UserRepository userRepository;
    private final ElasticsearchUserRepository elasticsearchUserRepository;
    private final RedisUserRepository redisUserRepository;
    
    public UserService(ElasticsearchUserRepository elasticsearchUserRepository, RedisUserRepository redisUserRepository) {
        // this.userRepository = userRepository;
        this.elasticsearchUserRepository = elasticsearchUserRepository;
        this.redisUserRepository = redisUserRepository;
    }
    
    public Mono<User> saveUser(User user) {
        return validateUser(user).flatMap(usr->{
            return determineStorageLocationById(usr.getId()).flatMap(storageLocation->{
                return storageLocation.equals("REDIS") ? redisUserRepository.save(usr) : elasticsearchUserRepository.save(usr);
            });
        }).doOnError(e->{});
    }
    
    public Mono<User> findById(Integer id) {
        if(id == null || id <= 0) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid user ID"));
        }
        return determineStorageLocationById(id).flatMap(storageLocation->{
            return storageLocation.equals("REDIS") ? redisUserRepository.findById(id) : elasticsearchUserRepository.findById(id);
        });
    }
    

    public Mono<Boolean> deleteUser(Integer id) {
        if(id == null || id <= 0) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid user ID"));
        }
        return determineStorageLocationById(id).flatMap(storageLocation->{
            return storageLocation.equals("REDIS") ? redisUserRepository.deleteById(id) : elasticsearchUserRepository.deleteById(id);
        });
    }

    public Mono<User> updateUser(Integer id, User newUser) {
        if(id == null || id <= 0) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid user ID"));
        }
        if(newUser == null) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "New user is required"));
        }

        try{
            return determineStorageLocationById(id).flatMap(storageLocation->{
                return storageLocation.equals("REDIS") ? redisUserRepository.updateUser(id, newUser) : elasticsearchUserRepository.updateUser(id, newUser);
            });
        } catch(Exception e){
            return Mono.error(e);
        }
        // return findById(id).flatMap(usr->{
        //     return elasticsearchUserRepository.updateUser(id, newUser);
        // }).switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")));

        // return redisUserRepository.updateUser(id, newUser);
    }

    private Mono<User> validateUser(User user){
        if(user.getEmail() == null || user.getEmail().isEmpty()){
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is required"));
        }
        if(user.getPassword() == null || user.getPassword().isEmpty()){
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password is required"));
        }
        return Mono.just(user);
    }

    private Mono<String> determineStorageLocationById(Integer id){
        return id % 2 == 1 ? Mono.just("REDIS") : Mono.just("ELASTICSEARCH");
    }

}
