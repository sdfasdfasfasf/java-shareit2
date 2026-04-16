package ru.practicum.shareit.item.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.user.UserDto;
import ru.practicum.shareit.user.UserService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class ItemServiceIntegrationTest {

    @Autowired
    private ItemService itemService;

    @Autowired
    private UserService userService;

    private Long ownerId;

    @BeforeEach
    void setUp() {
        UserDto owner = userService.create(UserDto.builder().name("Owner").email("owner@test.com").build());
        ownerId = owner.getId();
    }

    @Test
    void getAllByOwner_shouldReturnUserItems() {
        ItemDto item1 = ItemDto.builder().name("Item1").description("Desc1").available(true).build();
        ItemDto item2 = ItemDto.builder().name("Item2").description("Desc2").available(false).build();
        itemService.create(ownerId, item1);
        itemService.create(ownerId, item2);

        List<ItemResponseDto> items = itemService.getAllByOwner(ownerId);
        assertThat(items).hasSize(2);
        assertThat(items).extracting(ItemResponseDto::getName).containsExactlyInAnyOrder("Item1", "Item2");
    }
}