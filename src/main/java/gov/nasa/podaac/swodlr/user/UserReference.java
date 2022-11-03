package gov.nasa.podaac.swodlr.user;

import gov.nasa.podaac.swodlr.Utils;
import java.io.Serializable;
import java.util.Optional;
import java.util.UUID;

public class UserReference implements Serializable {
  private static UserRepository userRepository;

  private final UUID id;

  public UserReference(User user) {
    id = user.getId();
  }

  public UUID getId() {
    return id;
  }

  /*
   * Fetch the User object that this object references. The user may have
   * been deleted from the database since the last lookup so the presence
   * of the user is not guaranteed
   * 
   * @return a user object wrapped in an optional
   */
  public Optional<User> fetch() {
    return getUserRepository().findById(id);
  }

  private UserRepository getUserRepository() {
    if (userRepository == null) {
      userRepository = Utils
          .applicationContext()
          .getBean(UserRepository.class);
    }

    return userRepository;
  }
}
