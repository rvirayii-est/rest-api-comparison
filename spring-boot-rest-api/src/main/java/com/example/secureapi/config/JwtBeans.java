package com.example.secureapi.config;

import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.*;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;

@Configuration
public class JwtBeans {
  @Value("${app.jwt.public-key}") private String publicPem;
  @Value("${app.jwt.private-key}") private String privatePem;

  @Bean
  JwtDecoder jwtDecoder() throws Exception {
    RSAPublicKey pub = PemUtils.readPublicKey(publicPem);
    return NimbusJwtDecoder.withPublicKey(pub).build();
  }

  @Bean
  JwtEncoder jwtEncoder() throws Exception {
    RSAPublicKey pub = PemUtils.readPublicKey(publicPem);
    RSAPrivateKey priv = PemUtils.readPrivateKey(privatePem);
    RSAKey rsa = new RSAKey.Builder(pub).privateKey(priv).build();
    JWKSource<SecurityContext> jwks = new ImmutableJWKSet<>(new com.nimbusds.jose.jwk.JWKSet(rsa));
    return new NimbusJwtEncoder(jwks);
  }
}
