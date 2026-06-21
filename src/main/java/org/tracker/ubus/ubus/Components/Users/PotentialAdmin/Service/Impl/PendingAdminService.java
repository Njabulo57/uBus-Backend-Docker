package org.tracker.ubus.ubus.Components.Users.PotentialAdmin.Service.Impl;


import org.springframework.stereotype.Service;
import org.tracker.ubus.ubus.Components.Auth.Service.Interface.AuthTokenGenerationService;
import org.tracker.ubus.ubus.Components.EventHandler.Publisher.MultiEvenPublisher;
import org.tracker.ubus.ubus.Components.OneTimePassword.Service.Interface.IOneTimePasswordService;
import org.tracker.ubus.ubus.Components.Shared.Entities.BaseService;
import org.tracker.ubus.ubus.Components.Users.PotentialAdmin.Entity.PendingAdmin;
import org.tracker.ubus.ubus.Components.Users.PotentialAdmin.Events.PendingAdminAdditionEvent;
import org.tracker.ubus.ubus.Components.Users.PotentialAdmin.Exception.PendingAdminExistsException;
import org.tracker.ubus.ubus.Components.Users.PotentialAdmin.PendingAdminRepository.PendingAdminRepository;
import org.tracker.ubus.ubus.Components.Users.PotentialAdmin.Service.Interface.IPendingAdminService;
import org.tracker.ubus.ubus.Components.Users.User.Repository.UserRepository;

@Service
public class PendingAdminService extends BaseService implements IPendingAdminService {

    private final UserRepository userRepository;
    private final MultiEvenPublisher multiEvenPublisher;
    private final PendingAdminRepository pendingAdminRepository;
    private final AuthTokenGenerationService authTokenGenerationService;


    public PendingAdminService(UserRepository userRepository, PendingAdminRepository pendingAdminRepository,
                               IOneTimePasswordService authTokenGenerationService,
                               MultiEvenPublisher multiEvenPublisher) {
        this.userRepository = userRepository;
        this.pendingAdminRepository = pendingAdminRepository;
        this.authTokenGenerationService = authTokenGenerationService;
        this.multiEvenPublisher = multiEvenPublisher;
    }


    @Override
    public void addPendingAdminEmail(String email) {


        var user = getCurrentUser();
        var exists = checkIfEmailExists(email);

        if(exists)
            throw new PendingAdminExistsException("Email Already Exists");

        var pendingAdmin = new PendingAdmin(email, user);
        this.pendingAdminRepository.save(pendingAdmin);
    }


    @Override
    public void removePendingAdminEmail(String email) {

    }

    @Override
    public void sendEmailVerification(String email) {
        var otpCarrier = this.authTokenGenerationService.generateAuthToken(email);
        multiEvenPublisher.publish(() -> new PendingAdminAdditionEvent(this, email, otpCarrier));
    }


    /**
     * Checks whether a given email exists in the user repository but does not exist
     * in the pending administrator repository.
     *
     * @param email the email address to check for existence
     * @return true if the email exists in the user repository and does not exist
     *         in the pending administrator repository, false otherwise
     */
    private boolean checkIfEmailExists(String email) {
        return this.userRepository.existsByEmail(email) &&
                !this.pendingAdminRepository.existsByEmail(email);
    }
}
