package com.jnvstguru.jnvst_guru_backend.api;

import com.jnvstguru.jnvst_guru_backend.api.dto.DistrictResponse;
import com.jnvstguru.jnvst_guru_backend.api.dto.StateResponse;
import com.jnvstguru.jnvst_guru_backend.domain.DistrictEntity;
import com.jnvstguru.jnvst_guru_backend.domain.StateEntity;
import com.jnvstguru.jnvst_guru_backend.service.ApplicationUserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class ReferenceController {
    private final ApplicationUserService applicationUserService;

    public ReferenceController(ApplicationUserService applicationUserService) {
        this.applicationUserService = applicationUserService;
    }

    @GetMapping("/reference/states")
    public List<StateResponse> getActiveStates() {
        return applicationUserService.getActiveStates().stream()
                .map(state -> new StateResponse(state.getId(), state.getCode(), state.getName()))
                .toList();
    }

    @GetMapping("/reference/states/{stateId}/districts")
    public ResponseEntity<List<DistrictResponse>> getDistrictsForState(@PathVariable Long stateId) {
        StateEntity state = applicationUserService.getStateById(stateId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "State not found"));

        return ResponseEntity.ok(
                applicationUserService.getActiveDistrictsByState(state).stream()
                        .map(district -> new DistrictResponse(district.getId(), district.getCode(), district.getName()))
                        .toList()
        );
    }
}
