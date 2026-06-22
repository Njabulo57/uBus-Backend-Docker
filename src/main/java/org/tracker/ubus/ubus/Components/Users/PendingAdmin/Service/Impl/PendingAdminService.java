package org.tracker.ubus.ubus.Components.Users.PendingAdmin.Service.Impl;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.tracker.ubus.ubus.Components.Auth.Service.Interface.AuthTokenGenerationService;
import org.tracker.ubus.ubus.Components.EventHandler.Publisher.MultiEvenPublisher;
import org.tracker.ubus.ubus.Components.OneTimePassword.Service.Interface.IOneTimePasswordService;
import org.tracker.ubus.ubus.Components.Shared.Entities.BaseService;
import org.tracker.ubus.ubus.Components.Users.PendingAdmin.DTO.Response.PendingAdminResponse;
import org.tracker.ubus.ubus.Components.Users.PendingAdmin.Entity.PendingAdmin;
import org.tracker.ubus.ubus.Components.Users.PendingAdmin.Events.PendingAdminAdditionEvent;
import org.tracker.ubus.ubus.Components.Users.PendingAdmin.Exception.PendingAdminExistsException;
import org.tracker.ubus.ubus.Components.Users.PendingAdmin.Mapper.PendingAdminMapper;
import org.tracker.ubus.ubus.Components.Users.PendingAdmin.PendingAdminRepository.PendingAdminRepository;
import org.tracker.ubus.ubus.Components.Users.PendingAdmin.Service.Interface.IPendingAdminService;
import org.tracker.ubus.ubus.Components.Users.User.Repository.UserRepository;

import java.util.Collection;


@Slf4j
@Service
public class PendingAdminService extends BaseService implements IPendingAdminService {

    private final UserRepository userRepository;
    private final MultiEvenPublisher multiEvenPublisher;
    private final PendingAdminMapper pendingAdminMapper;
    private final PendingAdminRepository pendingAdminRepository;
    private final AuthTokenGenerationService authTokenGenerationService;


    public PendingAdminService(UserRepository userRepository, PendingAdminRepository pendingAdminRepository,
                               IOneTimePasswordService authTokenGenerationService,
                               MultiEvenPublisher multiEvenPublisher, PendingAdminMapper pendingAdminMapper) {
        this.userRepository = userRepository;
        this.pendingAdminRepository = pendingAdminRepository;
        this.authTokenGenerationService = authTokenGenerationService;
        this.multiEvenPublisher = multiEvenPublisher;
        this.pendingAdminMapper = pendingAdminMapper;
    }


    @Override
    public void addPendingAdminEmail(String email) {


        var exists = checkIfEmailExists(email);

        if(exists)
            throw new PendingAdminExistsException("Email Already Exists");

        var pendingAdmin = new PendingAdmin(email);
        this.pendingAdminRepository.save(pendingAdmin);
    }


    @Override
    public void removePendingAdminEmail(String email) {

        var pendingAdmin = this.pendingAdminRepository.findByEmailOrThrow(email);
        this.pendingAdminRepository.delete(pendingAdmin);
    }

    @Override
    public void sendEmailVerification(String email) {

        log.info("Sending Email Verification to {}", email);
        var otpCarrier = this.authTokenGenerationService.generateAuthToken(email);
        var pendingAdmin = this.pendingAdminRepository.findByEmailOrThrow(email);

        log.info("almost thereyy");
        pendingAdmin.setEmailSent(true); //set the email sent flag to true
        this.pendingAdminRepository.save(pendingAdmin); //save the pending admin entity

        log.info("Email Verification sent to {}", email);
        multiEvenPublisher.publish(() -> new PendingAdminAdditionEvent(this, email, otpCarrier));
    }


    @Override
    public Collection<PendingAdminResponse> getPendingAdmins() {
        var pendingAdmins = this.pendingAdminRepository.findAll();
        return this.pendingAdminMapper.toDTO(pendingAdmins);
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
        return this.userRepository.existsByEmail(email) ||
                this.pendingAdminRepository.existsByEmail(email);
    }
}
