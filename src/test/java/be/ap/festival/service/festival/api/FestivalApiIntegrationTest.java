package be.ap.festival.service.festival.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import be.ap.festival.service.data.FestivalDataStore;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class FestivalApiIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private FestivalDataStore dataStore;

    @Test
    void getFestivals_returnsExactSnapshotFromJson() throws Exception {
        // Ensure the in-memory datastore is loaded for MockMvc-based test
        dataStore.load();

        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(context).build();

        // 1) Call the MVC endpoint in a fully started Spring context
        String actualBody = mockMvc.perform(get("/api/festivals"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        // 2) Load expected array (the 'festivals' array) from festivals.json on classpath
        String expectedArrayJson = loadExpectedFestivalsArrayJson();

        // 3) Strict JSON compare: any change in order/values/structure will fail the test
        JSONAssert.assertEquals(expectedArrayJson, actualBody, true);
    }

    private String loadExpectedFestivalsArrayJson() throws IOException {
        ClassPathResource resource = new ClassPathResource("festivals.json");
        ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
        JsonNode root = mapper.readTree(resource.getInputStream());
        JsonNode festivalsArray = root.get("festivals");
        if (festivalsArray == null || !festivalsArray.isArray()) {
            throw new IllegalStateException("festivals.json does not contain an array property 'festivals'");
        }
        return mapper.writeValueAsString(festivalsArray);
    }
}
