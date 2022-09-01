package org.nasa.podaac.swodlr.user;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
public class UserController {
    private final UUID TEMP_USER_ID_DO_NOT_USE_IN_UAT_OR_OPS = UUID.fromString("fee1dc78-0604-4fa6-adae-0b4b55440e7d");

    @Autowired
    UserRepository userRepository;

    @QueryMapping
    public User currentUser() {
        var result = userRepository.findById(TEMP_USER_ID_DO_NOT_USE_IN_UAT_OR_OPS);
        return result.isPresent() ? result.get() : null;
    }
}
