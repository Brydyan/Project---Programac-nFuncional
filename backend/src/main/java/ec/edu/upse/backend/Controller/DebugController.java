package ec.edu.upse.backend.Controller;

import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/app/v1/debug")
public class DebugController {

    @GetMapping("/headers")
    public ResponseEntity<Map<String, String>> headers(HttpServletRequest request) {
        Map<String, String> map = new LinkedHashMap<>();
        Enumeration<String> names = request.getHeaderNames();
        if (names == null) return ResponseEntity.ok(Collections.emptyMap());
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            map.put(name, request.getHeader(name));
        }
        // Also add remoteAddr for clarity
        map.put("remoteAddr", request.getRemoteAddr());
        return ResponseEntity.ok(map);
    }
}
