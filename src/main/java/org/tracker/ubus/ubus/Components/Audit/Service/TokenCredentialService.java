package org.tracker.ubus.ubus.Components.Audit.Service;

import org.tracker.ubus.ubus.Components.Shared.Entities.BaseService;
import org.tracker.ubus.ubus.Components.Users.User.Enum.UserRole;

/**
 * Abstract class responsible for managing credentials.
 * Provides a method to handle the removal of expired credentials.
 */
public abstract class TokenCredentialService extends BaseService {

    /**
     * Removes expired credentials from the database.
     */
    protected abstract  void removedExpiredCredentials();

    /**
     * Verifies the uniqueness of the provided token within the system.
     * This method checks whether the token already exists or conflicts
     * with existing tokens.
     *
     * @param token the token to be verified for uniqueness
     * @return the same token if its unique, otherwise a new one
     */
    protected abstract String verifyTokenUniqueness(String token, UserRole userRole);
}
