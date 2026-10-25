package com.example.promptengineering.restController;

import com.example.promptengineering.BaseIntegrationTest;
import com.example.promptengineering.entity.User;
import com.example.promptengineering.repository.UserRepository;
import com.example.promptengineering.service.TwoFactorEmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class TwoFactorAuthControllerIntegrationTest extends BaseIntegrationTest {
    @Autowired
    private UserRepository userRepository;

    @MockitoBean
    private TwoFactorEmailService twoFactorEmailService;

    private User testUser;
    private MockHttpSession session;

    @BeforeEach
    void setUp() {
        String testEmail = "test-" + UUID.randomUUID() + "@example.com";

        testUser = new User();
        testUser.setEmail(testEmail);
        testUser.setPassword("encoded");
        testUser.setTwoFactorEnabled(true);
        testUser.setTwoFactorEmail(testEmail);
        testUser.setEncryptedKeys("some-encrypted-keys");
        testUser = userRepository.save(testUser);

        session = new MockHttpSession();
    }

    @Test
    void show2faForm_withValidSession_shouldReturnForm() throws Exception {
        session.setAttribute("2fa_user_id", testUser.getId());

        mockMvc.perform(get("/auth/2fa").session(session)).andExpect(status().isOk())
                .andExpect(view().name("2fa-form"))
                .andExpect(model().attributeDoesNotExist("error"));
    }

    @Test
    void show2faForm_withoutUserIdInSession_shouldRedirectToLogin() throws Exception {
        mockMvc.perform(get("/auth/2fa").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/login"));
    }

    @Test
    void verify2faCode_withValidCode_shouldRedirectToSavedRequest() throws Exception {
        session.setAttribute("2fa_user_id", testUser.getId());
        when(twoFactorEmailService.verifyCode(anyString(), eq("123456")))
                .thenReturn(true);

        mockMvc.perform(
                post("/auth/2fa").session(session).param("code", "123456").with(csrf()))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/"));
    }

    @Test
    void verify2faCode_withInvalidCode_shouldStayOnFormWithError() throws Exception {
        session.setAttribute("2fa_user_id", testUser.getId());
        when(twoFactorEmailService.verifyCode(anyString(), eq("000000")))
                .thenReturn(false);

        mockMvc.perform(
                post("/auth/2fa").session(session).param("code", "000000").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/2fa"));
    }

    @Test
    void resendCode_shouldReturnOkAndCallService() throws Exception {
        session.setAttribute("2fa_user_id", testUser.getId());
        doNothing().when(twoFactorEmailService).createAndSendCode(anyString(),
                anyString());

        mockMvc.perform(post("/auth/2fa/resend").session(session).with(csrf()))
                .andExpect(status().isOk());

        verify(twoFactorEmailService).createAndSendCode(session.getId(),
                testUser.getTwoFactorEmail());
    }

    @Test
    void resendCode_withoutUserId_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/auth/2fa/resend").session(session).with(csrf()))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(twoFactorEmailService);
    }
}
