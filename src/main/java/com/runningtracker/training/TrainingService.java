package com.runningtracker.training;

import com.runningtracker.domain.Training;
import com.runningtracker.domain.UserAccount;
import com.runningtracker.repository.TrainingRepository;
import com.runningtracker.repository.UserAccountRepository;
import com.runningtracker.training.dto.TrainingRequest;
import com.runningtracker.training.dto.TrainingResponse;
import com.runningtracker.validation.TrainingDateValidator;
import com.runningtracker.validation.TrainingDistanceValidator;
import com.runningtracker.validation.TrainingDurationValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Creates, updates, and deletes Trainings for the authenticated account
 * (REQ-F-4, REQ-F-10, REQ-F-11).
 *
 * <p>Every field passes through its ticket 5 validator before a Training is
 * persisted or changed, so the server-side rules hold regardless of which
 * client submitted the request.
 */
@Service
public class TrainingService {

    private final TrainingRepository trainings;
    private final UserAccountRepository users;
    private final TrainingDateValidator dateValidator;
    private final TrainingDistanceValidator distanceValidator;
    private final TrainingDurationValidator durationValidator;

    public TrainingService(TrainingRepository trainings,
                           UserAccountRepository users,
                           TrainingDateValidator dateValidator,
                           TrainingDistanceValidator distanceValidator,
                           TrainingDurationValidator durationValidator) {
        this.trainings = trainings;
        this.users = users;
        this.dateValidator = dateValidator;
        this.distanceValidator = distanceValidator;
        this.durationValidator = durationValidator;
    }

    /**
     * Validates and stores a Training owned by the given account.
     *
     * @param ownerEmail the authenticated account's email
     * @throws com.runningtracker.validation.TrainingValidationException if a field is invalid
     */
    @Transactional
    public TrainingResponse create(String ownerEmail, TrainingRequest request) {
        UserAccount owner = users.findByEmail(ownerEmail)
                .orElseThrow(() -> new IllegalStateException("Authenticated account not found."));

        ValidatedTraining validated = validate(request);
        Training training = trainings.save(new Training(
                validated.date(), validated.distanceMetres(), validated.durationSeconds(), owner));
        return TrainingResponse.from(training);
    }

    /**
     * Replaces a Training's Date, Distance, and Duration under the same rules
     * as creation (REQ-F-10). Validation runs before any field is changed, so a
     * rejected request leaves the stored Training untouched.
     *
     * @throws TrainingNotFoundException if no such Training belongs to the account,
     *                                   including when the identifier belongs to another account
     */
    @Transactional
    public TrainingResponse update(String ownerEmail, long id, TrainingRequest request) {
        Training training = trainings.findByIdAndOwnerEmail(id, ownerEmail)
                .orElseThrow(TrainingNotFoundException::new);

        ValidatedTraining validated = validate(request);
        training.update(validated.date(), validated.distanceMetres(), validated.durationSeconds());
        return TrainingResponse.from(training);
    }

    /**
     * Permanently removes a Training owned by the given account (REQ-F-11).
     * Derived figures need no correction because they are never stored.
     *
     * @throws TrainingNotFoundException if no such Training belongs to the account,
     *                                   including when the identifier belongs to another account
     */
    @Transactional
    public void delete(String ownerEmail, long id) {
        Training training = trainings.findByIdAndOwnerEmail(id, ownerEmail)
                .orElseThrow(TrainingNotFoundException::new);
        trainings.delete(training);
    }

    /**
     * Returns one Training owned by the given account (REQ-F-9).
     *
     * @throws TrainingNotFoundException if no such Training belongs to the account,
     *                                   including when the identifier belongs to another account
     */
    @Transactional(readOnly = true)
    public TrainingResponse get(String ownerEmail, long id) {
        Training training = trainings.findByIdAndOwnerEmail(id, ownerEmail)
                .orElseThrow(TrainingNotFoundException::new);
        return TrainingResponse.from(training);
    }

    private ValidatedTraining validate(TrainingRequest request) {
        return new ValidatedTraining(
                dateValidator.validate(request.date()),
                distanceValidator.validateAndNormalise(request.distanceKm()),
                durationValidator.validateAndTruncate(request.durationSeconds()));
    }

    private record ValidatedTraining(LocalDate date, int distanceMetres, int durationSeconds) {
    }
}
