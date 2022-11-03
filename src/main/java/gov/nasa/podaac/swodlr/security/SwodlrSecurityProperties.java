package gov.nasa.podaac.swodlr.security;

import com.nimbusds.jose.JWEDecrypter;
import com.nimbusds.jose.JWEEncrypter;
import com.nimbusds.jose.KeyLengthException;
import com.nimbusds.jose.crypto.DirectDecrypter;
import com.nimbusds.jose.crypto.DirectEncrypter;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.security.crypto.codec.Hex;

@ConfigurationProperties("swodlr.security")
@ConstructorBinding
@Qualifier("securityConfig")
public class SwodlrSecurityProperties {
  private final JWEEncrypter encrypter;
  private final JWEDecrypter decrypter;
  private final Duration sessionLength;

  /**
   * Configuration properties for swodlr sessions.
   *
   * @param sessionEncryptionKey Encryption key to use for web sessions
   * @param sessionLength How long sessions are valid
   * @throws KeyLengthException
   *     sessionEncryptionKey must be 128 bits (16 bytes), 192 bits (24 bytes), 256 bits (32 bytes),
   *     384 bits (48 bytes) or 512 bits (64 bytes) long. Must not be null.
   */
  public SwodlrSecurityProperties(String sessionEncryptionKey, Duration sessionLength)
      throws KeyLengthException {
    byte[] key = Hex.decode(sessionEncryptionKey);

    this.encrypter = new DirectEncrypter(key);
    this.decrypter = new DirectDecrypter(key);
    this.sessionLength = sessionLength;
  }

  public JWEEncrypter encrypter() {
    return encrypter;
  }

  public JWEDecrypter decrypter() {
    return decrypter;
  }

  public Duration sessionLength() {
    return sessionLength;
  }
}
