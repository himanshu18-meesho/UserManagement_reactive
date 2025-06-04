package com.himanshu.userManagement.repository;


import com.himanshu.userManagement.model.User;
import org.springframework.stereotype.Repository;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.springframework.beans.factory.annotation.Autowired;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.core.GetResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;

@Repository
public class UserRepository {
    private final ReactiveRedisTemplate<String, User> redisTemplate;
    private final ReactiveValueOperations<String, User> valueOps;

    @Autowired
    private ElasticsearchClient client;
    
    public UserRepository(ReactiveRedisTemplate<String, User> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.valueOps = redisTemplate.opsForValue();
    }
    
    public Mono<User> save(User user) {
        double random = Math.random(); // Generates random number between 0.0 and 1.0

        return Mono.fromCallable(() -> {
            IndexResponse response = client.index(i -> i.index("users").id(user.getId().toString()).document(user));
            return response;
        }).flatMap(response -> {
            if (random > 0.5) {
                return valueOps.set(user.getId().toString(), user)
                    .map(success -> user);
            }
            return Mono.just(user);
        });
    }
    
    public Mono<User> findById(Integer id) {
        return valueOps.get(id.toString())
                .switchIfEmpty(Mono.fromCallable(()->{
                    GetResponse<User> response = client.get(g -> g.index("users").id(id.toString()), User.class);
                    return response.source();
                })).flatMap(user->{
                    if(user != null){
                        return valueOps.set(user.getId().toString(), user).thenReturn(user);
                    }
                    return Mono.empty();
                });
    }
    
    public Flux<User> findAll() {

        // RedisTemplate is for general Redis operations, valueOps is specialized for key-value operations
        // We use flatMap because we're dealing with nested reactive types
        // valueOps.get() returns Mono<User> (single user)
        // The final stream is Flux<User> (stream of users)
        
        
        // ❓ we need to apply that logic of searching into redis then elasticsearch  --------------------------------

        // return redisTemplate.scan()
        //         .filter(key -> key.startsWith("user:"))
        //         .flatMap(key -> valueOps.get(key));

        // ------------------------------------------------------------------------------------------------------------

        return Mono.fromCallable(() -> {
            SearchResponse<User> response = client.search(s -> s
                .index("users"), User.class);
                return response.hits().hits();
        }).flatMapMany(hits -> Flux.fromIterable(hits)).map(hit -> hit.source());

        // return Flux.just(new User(1, "John", "john@gmail.com", "password123"), 
        //                  new User(2, "Jane", "jane@gmail.com", "password123"));
    }
    
    public Mono<Boolean> deleteById(Integer id) {
        // return Mono.just(true);
        return Mono.fromCallable(() -> {
            client.delete(d -> d.index("users").id(id.toString()));
            return true;
        }).flatMap(success -> {
            if(success){
                return valueOps.delete(id.toString()).thenReturn(true);
            }
            return Mono.just(false);
        });
    }

    public Mono<User> updateUser(Integer id, User newUser) {
        // return Mono.just(new User(id, name, "john@gmail.com", "password123"));
        // User newUser=new User(id)
        return Mono.fromCallable(() -> {
            // First check if document exists
            GetResponse<User> response = client.get(g -> g.index("users").id(id.toString()), User.class);
            if (response.source() == null) {
                throw new RuntimeException("User not found with id: " + id);
            }
            // If exists, perform update
            client.update(u -> u.index("users").id(id.toString()).doc(newUser), User.class);
            return newUser;
        }).flatMap(user -> {
            return valueOps.delete(user.getId().toString()).thenReturn(user);
        }).onErrorResume(e -> Mono.empty());
    }
}

