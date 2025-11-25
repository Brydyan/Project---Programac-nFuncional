package ec.edu.upse.backend.Service;

import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class IpLocationService {

    private final RestTemplate rest = new RestTemplate();

    /**
     * Consulta ipapi.co para obtener una descripción de la ubicación (city, region, country).
     * Devuelve null si no se puede obtener información.
     */
    public String getLocation(String ip) {
        if (ip == null || ip.isBlank()) return null;
        try {
            String url = "https://ipapi.co/" + ip + "/json/";
            @SuppressWarnings("unchecked")
            Map<String, Object> resp = rest.getForObject(url, Map.class);
            if (resp == null) return null;

            String city = (String) resp.get("city");
            String region = (String) resp.get("region");
            String country = (String) resp.get("country_name");

            StringBuilder sb = new StringBuilder();
            if (city != null && !city.isBlank()) sb.append(city);
            if (region != null && !region.isBlank()) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(region);
            }
            if (country != null && !country.isBlank()) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(country);
            }

            return sb.length() > 0 ? sb.toString() : null;
        } catch (Exception e) {
            // En caso de error (timeout, rate limit, etc.) devolvemos null y dejamos que la sesión se cree sin ubicación
            return null;
        }
    }
}
