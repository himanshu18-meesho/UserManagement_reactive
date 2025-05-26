package com.himanshu.userManagement.config;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import javax.net.ssl.SSLContext;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;





@Configuration
public class ElasticsearchConfig {

    @Bean
    public ElasticsearchClient elasticsearchClient() throws Exception {
        String hostname = "localhost";
        int port = 9200;
        String scheme = "https";
        String username = "elastic";
        String password = "5ujDFgIWxaxfYBhicQJ4";

        BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));

        SSLContext sslContext = SSLContextBuilder.create()
            .loadTrustMaterial(null, (TrustStrategy) (chain, authType) -> true)
            .build();

        RestClient restClient = RestClient.builder(new HttpHost(hostname, port, scheme))
            .setHttpClientConfigCallback(httpClientBuilder ->
                httpClientBuilder
                    .setDefaultCredentialsProvider(credentialsProvider)
                    .setSSLContext(sslContext)
            ).build();

        RestClientTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
        return new ElasticsearchClient(transport);
    }
}