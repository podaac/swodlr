package gov.nasa.podaac.swodlr;

import java.util.UUID;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class Utils implements ApplicationContextAware {
  public static final UUID NULL_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");

  private static ApplicationContext applicationContext;

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    Utils.applicationContext = applicationContext;
  }

  public static ApplicationContext applicationContext() {
    return applicationContext;
  }
}
