package ec.edu.upse.backend.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ec.edu.upse.backend.Service.UserService;

@RestController
@RequestMapping("/app/v1/user/available")
public class UserAvailabilityController {

    @Autowired
    private UserService userService;

    @GetMapping("/username/{username}")
    public ResponseEntity<AvailabilityResponse> checkUsernameAvailability(@PathVariable String username) {
        boolean available = userService.isUsernameAvailable(username);
        return ResponseEntity.ok(new AvailabilityResponse(available));
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<AvailabilityResponse> checkEmailAvailability(@PathVariable String email) {
        boolean available = userService.isEmailAvailable(email);
        return ResponseEntity.ok(new AvailabilityResponse(available));
    }

    // Inner class for JSON response
    static class AvailabilityResponse {
        public boolean available;

        public AvailabilityResponse(boolean available) {
            this.available = available;
        }

        public boolean isAvailable() {
            return available;
        }
    }
}
