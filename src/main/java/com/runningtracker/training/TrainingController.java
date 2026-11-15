package com.runningtracker.training;

import com.runningtracker.training.dto.HistoryResponse;
import com.runningtracker.training.dto.TrainingRequest;
import com.runningtracker.training.dto.TrainingResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

/**
 * Training endpoints (see {@code docs/openapi.yml}): history, details,
 * creation, edit, and deletion.
 */
@RestController
@RequestMapping("/api/trainings")
public class TrainingController {

    private final TrainingService trainingService;
    private final TrainingHistoryService trainingHistoryService;

    public TrainingController(TrainingService trainingService,
                              TrainingHistoryService trainingHistoryService) {
        this.trainingService = trainingService;
        this.trainingHistoryService = trainingHistoryService;
    }

    /**
     * Returns the account's full Training history (REQ-F-8).
     */
    @GetMapping
    public HistoryResponse history(Authentication authentication) {
        return trainingHistoryService.history(authentication.getName());
    }

    /**
     * Returns one Training owned by the session's account (REQ-F-9).
     */
    @GetMapping("/{id}")
    public TrainingResponse get(@PathVariable long id, Authentication authentication) {
        return trainingService.get(authentication.getName(), id);
    }

    /**
     * Records a Training for the session's account (REQ-F-4).
     */
    @PostMapping
    public ResponseEntity<TrainingResponse> create(@RequestBody TrainingRequest request,
                                                   Authentication authentication) {
        TrainingResponse created = trainingService.create(authentication.getName(), request);

        return ResponseEntity
                .created(URI.create("/api/trainings/" + created.id()))
                .body(created);
    }

    /**
     * Replaces a Training's fields under the same validation rules as creation
     * (REQ-F-10).
     */
    @PutMapping("/{id}")
    public TrainingResponse update(@PathVariable long id,
                                   @RequestBody TrainingRequest request,
                                   Authentication authentication) {
        return trainingService.update(authentication.getName(), id, request);
    }

    /**
     * Permanently removes a Training (REQ-F-11).
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long id, Authentication authentication) {
        trainingService.delete(authentication.getName(), id);
    }
}
