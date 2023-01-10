package gov.nasa.podaac.swodlr.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.ContextValue;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
public class UserController {
  @Autowired
  UserRepository userRepository;

  @QueryMapping
  public User currentUser(@ContextValue UserReference userRef) {
    return userRef.fetch();
  }
}
