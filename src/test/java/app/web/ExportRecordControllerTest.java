package app.web;

import app.config.ApiKeyAuthenticationFilter;
import app.config.SecurityConfig;
import app.model.ExportStatus;
import app.service.ExportRecordService;
import app.web.dto.exportRecord.ExportCreateRequestDto;
import app.web.dto.exportRecord.ExportResponseDto;
import app.web.dto.exportRecord.ExportUpdateRequestDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.UUID;

import static app.util.ExportTestFactory.*;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@ActiveProfiles("test")
@WebMvcTest(ExportRecordController.class)
@Import({SecurityConfig.class, ApiKeyAuthenticationFilter.class, SecurityTestConfig.class})
@TestPropertySource(properties = "export-record.service.api-key=api-key")
public class ExportRecordControllerTest {
    @MockitoBean
    private ExportRecordService exportRecordService;
    @MockitoBean
    private CacheManager cacheManager;
    @Autowired
    MockMvc mockMvc;
    @Autowired
    JsonMapper jsonMapper;

    @Test
    public void anyRequest_whenMissingApiKey_returns401() throws Exception {
        //Given
        MockHttpServletRequestBuilder request = get("/api/v1/exportRecord")
                .param("userId", UUID.randomUUID().toString());

        //When & Then
        mockMvc.perform(request)
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    public void anyRequest_whenInvalidApiKey_returns401() throws Exception {
        //Given
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-API-Key", "wrong-key");

        MockHttpServletRequestBuilder request = get("/api/v1/exportRecord")
                .headers(headers)
                .param("userId", UUID.randomUUID().toString());

        //When & Then
        mockMvc.perform(request)
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    public void create_whenValid_returns201() throws Exception {
        //Given
        ExportCreateRequestDto createDto = getExportCreateRequestDto();
        ExportResponseDto responseDto = getExportResponseDto();
        when(exportRecordService.create(any())).thenReturn(responseDto);

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-API-Key", SecurityTestConfig.TEST_API_KEY);
        MockHttpServletRequestBuilder request = post("/api/v1/exportRecord")
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(createDto));

        //When & Then
        mockMvc.perform(request)
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.fileName").isNotEmpty());
    }

    @Test
    public void create_whenBindingErrors_returns400() throws Exception {
        //Given
        ExportCreateRequestDto invalidDto = ExportCreateRequestDto.builder().build();
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-API-Key", SecurityTestConfig.TEST_API_KEY);
        MockHttpServletRequestBuilder request = post("/api/v1/exportRecord")
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(invalidDto));

        //When & Then
        mockMvc.perform(request)
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void getById_whenValid_returns200() throws Exception {
        //Given
        ExportResponseDto responseDto = getExportResponseDto();
        when(exportRecordService.getById(any(), any())).thenReturn(responseDto);

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-API-Key", SecurityTestConfig.TEST_API_KEY);
        MockHttpServletRequestBuilder request = get("/api/v1/exportRecord/{id}", UUID.randomUUID())
                .headers(headers)
                .param("userId", UUID.randomUUID().toString());

        //When & Then
        mockMvc.perform(request)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.fileName").isNotEmpty());
    }

    // ---- getHistory ----

    @Test
    public void getHistory_whenValid_returns200() throws Exception {

        //Given
        List<ExportResponseDto> responses = List.of(getExportResponseDto(), getExportResponseDto());

        when(exportRecordService.getHistory(any())).thenReturn(responses);

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-API-Key", SecurityTestConfig.TEST_API_KEY);

        MockHttpServletRequestBuilder request = get("/api/v1/exportRecord")
                .headers(headers)
                .param("userId", UUID.randomUUID().toString());

        //When & Then

        mockMvc.perform(request)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    // ---- getFailedByUser ----

    @Test
    public void getFailedByUser_whenValid_returns200() throws Exception {

        //Given
        List<ExportResponseDto> responses = List.of(getExportResponseDto());

        when(exportRecordService.getFailedByUserId(any())).thenReturn(responses);

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-API-Key", SecurityTestConfig.TEST_API_KEY);

        MockHttpServletRequestBuilder request = get("/api/v1/exportRecord/failed")
                .headers(headers)
                .param("userId", UUID.randomUUID().toString());

        //When & Then

        mockMvc.perform(request)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    // ---- update ----

    @Test
    public void update_whenValid_returns200() throws Exception {

        //Given
        ExportUpdateRequestDto updateDto = getExportUpdateRequestDto();
        ExportResponseDto responseDto = getExportResponseDto();

        when(exportRecordService.update(any(), any(), any())).thenReturn(responseDto);

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-API-Key", SecurityTestConfig.TEST_API_KEY);

        MockHttpServletRequestBuilder request = put("/api/v1/exportRecord/{id}", UUID.randomUUID())
                .headers(headers)
                .param("userId", UUID.randomUUID().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(updateDto));

        //When & Then

        mockMvc.perform(request)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").isNotEmpty());
    }

    // ---- retry ----

    @Test
    public void retry_whenValid_returns202() throws Exception {

        //Given
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-API-Key", SecurityTestConfig.TEST_API_KEY);

        MockHttpServletRequestBuilder request = put("/api/v1/exportRecord/{id}/retry", UUID.randomUUID())
                .headers(headers)
                .param("status", ExportStatus.SUCCEEDED.name())
                .param("userId", UUID.randomUUID().toString());

        //When & Then

        mockMvc.perform(request)
                .andExpect(MockMvcResultMatchers.status().isAccepted());
    }

    // ---- delete ----

    @Test
    public void delete_whenValid_returns202() throws Exception {

        //Given
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-API-Key", SecurityTestConfig.TEST_API_KEY);

        MockHttpServletRequestBuilder request = delete("/api/v1/exportRecord/{id}", UUID.randomUUID())
                .headers(headers)
                .param("userId", UUID.randomUUID().toString());

        //When & Then

        mockMvc.perform(request)
                .andExpect(MockMvcResultMatchers.status().isAccepted());
    }
}
