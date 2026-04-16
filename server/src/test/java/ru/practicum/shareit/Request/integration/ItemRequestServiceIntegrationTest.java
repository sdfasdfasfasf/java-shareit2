package ru.practicum.shareit.request.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.request.ItemRequestService;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.user.UserDto;
import ru.practicum.shareit.user.UserService;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class ItemRequestServiceIntegrationTest {

    @Autowired
    private ItemRequestService requestService;

    @Autowired
    private UserService userService;

    private Long userId;

    @BeforeEach
    void setUp() {
        UserDto user = userService.create(UserDto.builder().name("Requester").email("req@test.com").build());
        userId = user.getId();
    }

    @Test
    void create_shouldSaveRequest() {
        ItemRequestCreateDto dto = new ItemRequestCreateDto();
        dto.setDescription("Need a ladder");
        ItemRequestResponseDto response = requestService.create(userId, dto);
        assertThat(response.getId()).isNotNull();
        assertThat(response.getDescription()).isEqualTo("Need a ladder");
        assertThat(response.getItems()).isEmpty();
    }
}