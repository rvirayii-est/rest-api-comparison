package com.example.secureapi.config;

import java.io.IOException;
import java.io.StringReader;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class PemUtils {
  public static RSAPublicKey readPublicKey(String pem) throws GeneralSecurityException, IOException {
    String content = pem.replace("-----BEGIN PUBLIC KEY-----", "")
                        .replace("-----END PUBLIC KEY-----", "")
                        .replaceAll("\n", "").trim();
    byte[] decoded = Base64.getDecoder().decode(content);
    X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decoded);
    return (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(keySpec);
  }

  public static RSAPrivateKey readPrivateKey(String pem) throws GeneralSecurityException, IOException {
    String content = pem.replace("-----BEGIN PRIVATE KEY-----", "")
                        .replace("-----END PRIVATE KEY-----", "")
                        .replaceAll("\n", "").trim();
    byte[] decoded = Base64.getDecoder().decode(content);
    PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decoded);
    return (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(keySpec);
  }
}
