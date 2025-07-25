package util; // Puedes crear un nuevo paquete 'util' o 'service'

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONObject; // Asegúrate de tener la dependencia org.json en tu pom.xml

/**
 * Clase de servicio para interactuar con la API de consulta de DNI de
 * ApiPeru.dev. Permite obtener información de una persona a partir de su número
 * de DNI.
 */
public class DniApiService implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(DniApiService.class.getName());

    // Tu token de autorización proporcionado
    private static final String API_TOKEN = "0ed818390670e8d545b98b9f2183fa953ec75ca3227ab5ecebcf2b1d01eb4669";
    // URL base de la API de DNI
    private static final String API_BASE_URL = "https://apiperu.dev/api/dni/";

    /**
     * Consulta la información de un DNI en la API de ApiPeru.dev.
     *
     * @param dni El número de DNI de 8 dígitos a consultar.
     * @return Un mapa (Map) con los datos del DNI (nombreCompleto, nombres,
     * apellidoPaterno, apellidoMaterno) o un mapa vacío si la consulta falla o
     * el DNI no se encuentra.
     */
    public Map<String, String> consultarDni(String dni) {
        Map<String, String> dniData = new HashMap<>();
        if (dni == null || dni.trim().isEmpty() || dni.length() != 8) {
            LOGGER.log(Level.WARNING, "DNI inválido para la consulta: {0}", dni);
            return dniData;
        }

        HttpURLConnection connection = null;
        BufferedReader reader = null;

        try {
            // Construye la URL completa para la consulta del DNI
            URL url = new URL(API_BASE_URL + dni);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            // Establece la cabecera de autorización con tu token
            connection.setRequestProperty("Authorization", "Bearer " + API_TOKEN);
            connection.setRequestProperty("Content-Type", "application/json");

            int responseCode = connection.getResponseCode();
            LOGGER.log(Level.INFO, "Respuesta HTTP de la API de DNI para {0}: {1}", new Object[]{dni, responseCode});

            StringBuilder fullResponse = new StringBuilder();

            if (responseCode == HttpURLConnection.HTTP_OK) { // Código 200 OK
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8")); // Explicit UTF-8
            } else {
                // Si la respuesta no es 200 OK, lee el stream de error para obtener más detalles
                reader = new BufferedReader(new InputStreamReader(connection.getErrorStream(), "UTF-8")); // Explicit UTF-8
            }

            String inputLine;
            while ((inputLine = reader.readLine()) != null) {
                fullResponse.append(inputLine);
            }

            String responseString = fullResponse.toString();
            LOGGER.log(Level.INFO, "Respuesta completa de la API para DNI {0} (Código {1}): {2}",
                    new Object[]{dni, responseCode, responseString});

            if (responseCode == HttpURLConnection.HTTP_OK) {
                JSONObject jsonResponse = new JSONObject(responseString);

                if (jsonResponse.has("success") && jsonResponse.getBoolean("success") && jsonResponse.has("data")) {
                    JSONObject data = jsonResponse.getJSONObject("data");

                    String nombreCompleto = data.optString("nombre_completo", "");
                    String nombres = data.optString("nombres", "");
                    String apellidoPaterno = data.optString("apellido_paterno", "");
                    String apellidoMaterno = data.optString("apellido_materno", "");

                    // Log the extracted values BEFORE putting into map
                    LOGGER.log(Level.INFO, "Valores extraídos de JSON para DNI {0}: "
                            + "nombreCompleto=''{1}'', nombres=''{2}'', apellidoPaterno=''{3}'', apellidoMaterno=''{4}''",
                            new Object[]{dni, nombreCompleto, nombres, apellidoPaterno, apellidoMaterno});

                    dniData.put("nombreCompleto", nombreCompleto);
                    dniData.put("nombres", nombres);
                    dniData.put("apellidoPaterno", apellidoPaterno);
                    dniData.put("apellidoMaterno", apellidoMaterno);
                    LOGGER.log(Level.INFO, "Datos de DNI puestos en el mapa para {0}: {1}", new Object[]{dni, dniData});
                } else {
                    String message = jsonResponse.optString("message", "DNI no encontrado o error en la API.");
                    LOGGER.log(Level.WARNING, "Respuesta exitosa de la API, pero datos no válidos para DNI {0}: {1}", new Object[]{dni, message});
                }

            } else {
                LOGGER.log(Level.SEVERE, "Error HTTP al consultar DNI {0}. Código: {1}, Respuesta: {2}",
                        new Object[]{dni, responseCode, responseString});
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, String.format("Excepción al consultar DNI %s: %s", dni, e.getMessage()), e);
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
                if (connection != null) {
                    connection.disconnect();
                }
            } catch (Exception ex) {
                LOGGER.log(Level.WARNING, "Error al cerrar recursos de conexión HTTP: " + ex.getMessage(), ex);
            }
        }
        return dniData;
    }
}
