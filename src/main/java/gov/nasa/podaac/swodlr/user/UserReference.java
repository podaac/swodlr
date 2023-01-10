package gov.nasa.podaac.swodlr.user;

import gov.nasa.podaac.swodlr.Utils;
import gov.nasa.podaac.swodlr.exception.SwodlrException;
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
   * of the user is not guaranteed. If the user is not found, a SwodlrException
   * is thrown
   */
  public User fetch() {
    Optional<User> result = getUserRepository().findById(id);
    if (result.isEmpty()) {
      throw new SwodlrException("User cannot be found. Try clearing your cookies and try again.");
    }

    return result.get();
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
