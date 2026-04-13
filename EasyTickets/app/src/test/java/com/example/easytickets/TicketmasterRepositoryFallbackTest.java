package com.example.easytickets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import com.example.easytickets.data.ticketmaster.TicketmasterQueryFactory;
import com.example.easytickets.data.ticketmaster.TicketmasterRepositoryImpl;
import com.example.easytickets.domain.model.EventCategory;
import com.example.easytickets.util.AppConfig;

import org.junit.Test;

import java.util.List;

/**
 * Unit test covering the built-in fallback category list exposed by {@link TicketmasterRepositoryImpl}.
 */
public class TicketmasterRepositoryFallbackTest {

    @Test
    public void fallbackCategories_provideTheExpectedTopLevelOptions() {
        TicketmasterRepositoryImpl repository = new TicketmasterRepositoryImpl(
                null,
                new TicketmasterQueryFactory(),
                new AppConfig("google-key", "ticketmaster-key")
        );

        List<EventCategory> categories = repository.getFallbackCategories();

        assertEquals(5, categories.size());
        assertEquals("Music", categories.get(0).getName());
        assertFalse(categories.get(0).getId().isEmpty());
    }
}
