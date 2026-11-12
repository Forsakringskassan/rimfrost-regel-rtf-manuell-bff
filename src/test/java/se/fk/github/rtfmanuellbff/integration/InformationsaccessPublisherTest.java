package se.fk.github.rtfmanuellbff.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import se.fk.github.rtfmanuellbff.model.BackendAnvandare;
import se.fk.github.rtfmanuellbff.model.BackendGetResponse;
import se.fk.github.rtfmanuellbff.model.BackendInformationsaccess;
import se.fk.rimfrost.framework.bff.ilogg.ILogg;
import se.fk.rimfrost.framework.bff.ilogg.exception.ILoggException;
import se.fk.rimfrost.informationsaccess.InformationsaccessEvent;
import se.fk.rimfrost.regel.rtf.manuell.jaxrsspec.controllers.generatedsource.model.GetDataResponse;
import se.fk.rimfrost.regel.rtf.manuell.jaxrsspec.controllers.generatedsource.model.Kund;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for how {@link InformationsaccessPublisher} builds and publishes the event, with ILogg mocked.
 */
class InformationsaccessPublisherTest
{
   private static final String HANDLAGGNING_ID = "550e8400-e29b-41d4-a716-446655440000";
   private static final UUID UPPGIFT_ID = UUID.fromString("8a2b3c4d-5e6f-4a1b-8c2d-3e4f5a6b7c8d");
   private static final UUID REGEL_ID = UUID.fromString("a11f3429-3a6b-4389-ba1c-c2747b0fb45b");
   private static final String ANVANDARE_TYP_ID = "9b1e4f2a-6c3d-4e8b-a7f5-2d0c1b9e8a36";

   private ILogg iLogg;
   private InformationsaccessPublisher publisher;

   @BeforeEach
   void setUp()
   {
      iLogg = Mockito.mock(ILogg.class);
      publisher = new InformationsaccessPublisher();
      publisher.iLogg = iLogg;
      publisher.objectMapper = new ObjectMapper();
   }

   @Test
   @DisplayName("RTFB-FR-04.1, RTFB-FR-04.2: Eventet byggs av informationsaccessdata och det som visas")
   @SuppressWarnings("unchecked")
   void publish_buildsEventFromInformationsaccessAndData() throws ILoggException
   {
      var fore = OffsetDateTime.now();
      publisher.publish(HANDLAGGNING_ID, response());
      var efter = OffsetDateTime.now();

      var event = publishedEvent();
      var kund = (Map<String, Object>) event.getVisadInformation().get("kund");
      assertAll(
            () -> assertDoesNotThrow(() -> UUID.fromString(event.getId())),
            () -> assertEquals(ANVANDARE_TYP_ID, event.getAnvandare().getTypId()),
            () -> assertEquals("H123456", event.getAnvandare().getVarde()),
            () -> assertFalse(event.getTidpunkt().isBefore(fore)),
            () -> assertFalse(event.getTidpunkt().isAfter(efter)),
            () -> assertEquals(InformationsaccessPublisher.KALLA, event.getKalla()),
            () -> assertEquals(HANDLAGGNING_ID, event.getHandlaggningId()),
            () -> assertEquals(UPPGIFT_ID.toString(), event.getUppgiftId()),
            () -> assertEquals(REGEL_ID.toString(), event.getRegelId()),
            () -> assertEquals("1", event.getRegelVersion()),
            () -> assertEquals("Anna", kund.get("fornamn")),
            () -> assertFalse(event.getVisadInformation().containsKey("informationsaccess")));
   }

   @Test
   @DisplayName("RTFB-FR-04.2: Varje publicering får ett eget id")
   void publish_givesEachEventItsOwnId() throws ILoggException
   {
      publisher.publish(HANDLAGGNING_ID, response());
      publisher.publish(HANDLAGGNING_ID, response());

      var captor = ArgumentCaptor.forClass(InformationsaccessEvent.class);
      Mockito.verify(iLogg, Mockito.times(2)).publish(captor.capture());
      assertNotEquals(captor.getAllValues().get(0).getId(), captor.getAllValues().get(1).getId());
   }

   @Test
   @DisplayName("RTFB-FR-04.5: Om ILogg inte kan publicera kastas ett fel")
   void publish_throws_whenILoggFails() throws ILoggException
   {
      var cause = new ILoggException("Kafka unavailable", new RuntimeException("nack"));
      Mockito.doThrow(cause).when(iLogg).publish(Mockito.any());

      var thrown = assertThrows(InformationsaccessPublishException.class,
            () -> publisher.publish(HANDLAGGNING_ID, response()));
      assertSame(cause, thrown.getCause());
   }

   private InformationsaccessEvent publishedEvent() throws ILoggException
   {
      var captor = ArgumentCaptor.forClass(InformationsaccessEvent.class);
      Mockito.verify(iLogg).publish(captor.capture());
      return captor.getValue();
   }

   private static BackendGetResponse response()
   {
      var kund = new Kund();
      kund.setFornamn("Anna");
      kund.setEfternamn("Svensson");
      var data = new GetDataResponse();
      data.setHandlaggningId(UUID.fromString(HANDLAGGNING_ID));
      data.setKund(kund);
      var informationsaccess = new BackendInformationsaccess(UPPGIFT_ID, REGEL_ID, "1",
            new BackendAnvandare(ANVANDARE_TYP_ID, "H123456"));
      return new BackendGetResponse(data, informationsaccess);
   }
}
