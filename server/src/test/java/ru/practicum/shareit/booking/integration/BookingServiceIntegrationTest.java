package ru.practicum.shareit.booking.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingService;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.UserDto;
import ru.practicum.shareit.user.UserService;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class BookingServiceIntegrationTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private UserService userService;

    @Autowired
    private ItemService itemService;

    private Long bookerId;
    private Long itemId;
    private Long ownerId;

    @BeforeEach
    void setUp() {
        UserDto owner = userService.create(UserDto.builder().name("Owner").email("owner@test.com").build());
        ownerId = owner.getId();
        UserDto booker = userService.create(UserDto.builder().name("Booker").email("booker@test.com").build());
        bookerId = booker.getId();
        ItemDto item = ItemDto.builder().name("Drill").description("Cordless drill").available(true).build();
        itemId = itemService.create(ownerId, item).getId();
    }

    @Test
    void create_shouldSaveBooking() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);
        BookingRequestDto request = new BookingRequestDto();
        request.setItemId(itemId);
        request.setStart(start);
        request.setEnd(end);
        BookingResponseDto booking = bookingService.create(bookerId, request);
        assertThat(booking.getId()).isNotNull();
        assertThat(booking.getStatus()).isEqualTo(Booking.BookingStatus.WAITING);
        assertThat(booking.getItem().getId()).isEqualTo(itemId);
    }

    @Test
    void create_shouldThrowBadRequest_whenItemNotAvailable() {
        UserDto anotherOwner = userService.create(UserDto.builder().name("Owner2").email("owner2@test.com").build());
        ItemDto itemUnavail = ItemDto.builder().name("Broken").description("Broken").available(false).build();
        Long unavailId = itemService.create(anotherOwner.getId(), itemUnavail).getId();

        BookingRequestDto request = new BookingRequestDto();
        request.setItemId(unavailId);
        request.setStart(LocalDateTime.now().plusDays(1));
        request.setEnd(LocalDateTime.now().plusDays(2));

        assertThatThrownBy(() -> bookingService.create(bookerId, request))
                .isInstanceOf(BadRequestException.class);
    }
}