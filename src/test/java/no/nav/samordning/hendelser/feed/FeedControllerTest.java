package no.nav.samordning.hendelser.feed;

import no.nav.samordning.hendelser.TestDataHelper;
import no.nav.samordning.hendelser.hendelse.Hendelse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class FeedControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TestDataHelper testData;

    @Test
    public void greeting_should_return_message_from_service() throws Exception {
        mockMvc.perform(get("/hendelser")
            .with(user("srvTest")))
            .andDo(print()).andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE));
    }

    @Test
    public void service_shouldnt_accept_too_large_requests() throws Exception {
        mockMvc.perform(get("/hendelser")
            .with(user("srvTest"))
            .param("antall", "10001"))
            .andDo(print()).andExpect(status().is4xxClientError());
    }

    @Test
    public void greeting_should_return_message_from_service_with_first_record() throws Exception {
        List<Hendelse> result = testData.mapJsonToHendelser(
            mockMvc.perform(get("/hendelser?antall=1")
                .with(user("srvTest")))
                .andDo(print()).andReturn().getResponse().getContentAsString());
        assertThat(result.get(0), samePropertyValuesAs(testData.hendelse("0")));
    }

    @Test
    public void greeting_should_return_message_from_service_with_size_check() throws Exception {
        mockMvc.perform(get("/hendelser?side=0&antall=5")
            .with(user("srvTest")))
            .andExpect(MockMvcResultMatchers.jsonPath("$.hendelser", hasSize(5)));
    }

    @Test
    public void bad_parameters_return_400() throws Exception {
        mockMvc.perform(get("/hendelser?side=-1")
            .with(user("srvTest")))
            .andDo(print()).andExpect(status().isBadRequest());
    }
}
