package pl.allegro.tech.graphql.auditlog;

import static com.google.common.io.Resources.getResource;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.paranamer.ParanamerModule;
import com.google.common.io.Resources;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

class JsonUtil {

  private JsonUtil() {}

  static List<FieldSetup> jsonFieldSetups(String file) {
    try {
      ObjectMapper objectMapper = new ObjectMapper();
      objectMapper.registerModule(new ParanamerModule());
      URL url = getResource(file);
      String text = Resources.toString(url, StandardCharsets.UTF_8);
      return objectMapper.readValue(text, new TypeReference<>() {});
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }
}
