package com.example.promptengineering.restController;

import com.example.promptengineering.dto.RegisterNodeRequest;
import com.example.promptengineering.entity.HostedNode;
import com.example.promptengineering.entity.HostedNode.Status;
import com.example.promptengineering.entity.User;
import com.example.promptengineering.exception.UserAlreadyExistsException;
import com.example.promptengineering.model.AppRole;
import com.example.promptengineering.repository.HostedNodeRepository;
import com.example.promptengineering.repository.UserRepository;
import com.example.promptengineering.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
public class HostedNodeApiControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private HostedNodeRepository hostedNodeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private User adminUser;
    private User normalUser;
    private User otherUser;

    @BeforeEach
    void setUp() throws UserAlreadyExistsException {
        adminUser = userService.createUser("admin@nodes.com", "password",
                List.of(AppRole.ADMIN));
        normalUser = userService.createUser("user@nodes.com", "password",
                List.of(AppRole.USER));
        otherUser = userService.createUser("other@nodes.com", "password",
                List.of(AppRole.USER));
    }

    @Test
    void registerNode_asUser_shouldReturnTokenAndSave() throws Exception {
        RegisterNodeRequest req = new RegisterNodeRequest("my-4090",
                "mistral-7b-instruct", "community", true);

        mockMvc.perform(post("/api/nodes").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)).with(csrf())
                .with(user(userService.loadUserByUsername(normalUser.getEmail()))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.authToken", matchesPattern("[A-Za-z0-9_-]{40,}")))
                .andExpect(jsonPath("$.status", is("OFFLINE")))
                .andExpect(jsonPath("$.modelFamily", is("community")))
                .andExpect(jsonPath("$.allowPublicUse", is(true)));

        List<HostedNode> all = hostedNodeRepository.findAll();
        assertThat(all).hasSize(1);
        HostedNode node = all.get(0);
        assertThat(node.getOwner().getId()).isEqualTo(normalUser.getId());
        assertThat(node.getNodeName()).isEqualTo("my-4090");
    }

    @Test
    void registerNode_withoutAuthentication_shouldReturnUnauthorized() throws Exception {
        RegisterNodeRequest req = new RegisterNodeRequest("x", "phi-3", "community",
                true);

        mockMvc.perform(post("/api/nodes").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)).with(csrf()))
                .andExpect(status().isUnauthorized());

        assertThat(hostedNodeRepository.findAll()).isEmpty();
    }

    @Test
    void registerNode_withEmptyName_shouldReturnBadRequest() throws Exception {
        RegisterNodeRequest req = new RegisterNodeRequest("", "llama3", "community",
                true);

        mockMvc.perform(post("/api/nodes").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)).with(csrf())
                .with(user(userService.loadUserByUsername(normalUser.getEmail()))))
                .andExpect(status().isBadRequest());

        assertThat(hostedNodeRepository.findAll()).isEmpty();
    }

    @Test
    void listMyNodes_asUser_shouldReturnOnlyOwn() throws Exception {
        createNode(normalUser, "n1", Status.ONLINE, true);
        createNode(normalUser, "n2", Status.OFFLINE, false);
        createNode(otherUser, "other", Status.ONLINE, true);

        mockMvc.perform(get("/api/nodes")
                .with(user(userService.loadUserByUsername(normalUser.getEmail()))))
                .andExpect(status().isOk())
                .andExpect((ResultMatcher) jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].nodeName", containsInAnyOrder("n1", "n2")));
    }

    @Test
    void listPublicCommunityNodes_shouldReturnOnlyOnlineAndPublic() throws Exception {
        createNode(normalUser, "public-online", Status.ONLINE, true);
        createNode(normalUser, "public-offline", Status.OFFLINE, true);
        createNode(normalUser, "private-online", Status.ONLINE, false);

        mockMvc.perform(get("/api/nodes/public")
                .with(user(userService.loadUserByUsername(normalUser.getEmail()))))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nodeName", is("public-online")))
                .andExpect(jsonPath("$[0].modelFamily", is("community")));
    }

    @Test
    void deleteNode_asOwner_shouldRemove() throws Exception {
        HostedNode node = createNode(normalUser, "todelete", Status.OFFLINE, true);

        mockMvc.perform(delete("/api/nodes/{id}", node.getId()).with(csrf())
                .with(user(userService.loadUserByUsername(normalUser.getEmail()))))
                .andExpect(status().isNoContent());

        assertThat(hostedNodeRepository.findById(node.getId())).isEmpty();
    }

    @Test
    void deleteNode_asOtherUser_shouldReturnForbidden() throws Exception {
        HostedNode node = createNode(normalUser, "protected", Status.ONLINE, true);

        mockMvc.perform(delete("/api/nodes/{id}", node.getId()).with(csrf())
                .with(user(userService.loadUserByUsername(otherUser.getEmail()))))
                .andExpect(status().isForbidden());

        assertThat(hostedNodeRepository.findById(node.getId())).isPresent();
    }

    @Test
    void deleteNode_withoutAuthentication_shouldReturnUnauthorized() throws Exception {
        HostedNode node = createNode(normalUser, "x", Status.OFFLINE, true);

        mockMvc.perform(delete("/api/nodes/{id}", node.getId()).with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    private HostedNode createNode(User owner, String name, Status status,
                                  boolean allowPublic) {
        HostedNode n = new HostedNode();
        n.setOwner(owner);
        n.setNodeName(name);
        n.setModelName("test-model");
        n.setModelFamily("community");
        n.setAuthToken(java.util.UUID.randomUUID().toString().replace("-", ""));
        n.setStatus(status);
        n.setAllowPublicUse(allowPublic);
        return hostedNodeRepository.save(n);
    }
}
