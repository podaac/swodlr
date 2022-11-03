package gov.nasa.podaac.swodlr.security;

import com.nimbusds.jose.JWEDecrypter;
import com.nimbusds.jose.JWEEncrypter;
import com.nimbusds.jose.KeyLengthException;
import com.nimbusds.jose.crypto.DirectDecrypter;
import com.nimbusds.jose.crypto.DirectEncrypter;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.security.crypto.codec.Hex;

@ConfigurationProperties("swodlr.security")
@ConstructorBinding 
public class SwodlrSecurityProperties {
  private final JWEEncrypter encrypter;
  private final JWEDecrypter decrypter;
  private final Duration sessionLength;

  public SwodlrSecurityProperties(String sessionEncryptionKey, long sessionLength)
      throws KeyLengthException {
    byte[] key = Hex.decode(sessionEncryptionKey);

    this.encrypter = new DirectEncrypter(key);
    this.decrypter = new DirectDecrypter(key);
    this.sessionLength = Duration.ofSeconds(sessionLength);
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
