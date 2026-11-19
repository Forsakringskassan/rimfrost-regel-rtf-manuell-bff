package se.fk.github.rtfmanuellbff;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.fk.github.rtfmanuellbff.integration.InformationsaccessPublisher;
import se.fk.github.rtfmanuellbff.integration.RtfManuellClient;
import se.fk.github.rtfmanuellbff.model.BackendGetResponse;
import se.fk.github.rtfmanuellbff.model.BackendPatchRequest;
import se.fk.github.rtfmanuellbff.model.BackendUpdateErsattning;
import se.fk.github.rtfmanuellbff.model.BackendUppgiftsbeskrivning;
import se.fk.rimfrost.framework.bff.logging.LogContext;
import se.fk.rimfrost.regel.rtf.manuell.bff.jaxrsspec.controllers.generatedsource.RtfManuellBffControllerApi;
import se.fk.rimfrost.regel.rtf.manuell.bff.jaxrsspec.controllers.generatedsource.model.GetTaskResponse;
import se.fk.rimfrost.regel.rtf.manuell.bff.jaxrsspec.controllers.generatedsource.model.GetUppgiftsbeskrivningResponse;
import se.fk.rimfrost.regel.rtf.manuell.bff.jaxrsspec.controllers.generatedsource.model.PatchErsattningarRequest;

import java.util.UUID;
import java.util.function.Supplier;

// Paths, parameters and validation come from the generated interface (rimfrost-regel-rtf-manuell-bff-openapi).
// The interface has no class-level @Path, so it is declared here to make this a REST resource.
@ApplicationScoped
@Path("")
public class RtfManuellBffController implements RtfManuellBffControllerApi
{
   private static final Logger LOGGER = LoggerFactory.getLogger(RtfManuellBffController.class);

   @RestClient
   RtfManuellClient backendClient;

   @Inject
   InformationsaccessPublisher informationsaccessPublisher;

   @Override
   public GetTaskResponse getTask(UUID handlaggningId)
   {
      LOGGER.debug("GET /api/task/{}", handlaggningId);
      return withLogContext(handlaggningId, () -> {
         BackendGetResponse response = backendClient.getTask(handlaggningId.toString());
         // Map before publishing so that no access is logged for data that is never shown
         GetTaskResponse task = GetTaskResponseMapper.toGetTaskResponse(response.data());
         informationsaccessPublisher.publish(handlaggningId.toString(), response);
         return task;
      });
   }

   @Override
   public void patchErsattningar(UUID handlaggningId, PatchErsattningarRequest body)
   {
      LOGGER.debug("POST /api/{}/patchErsattningar", handlaggningId);
      withLogContext(handlaggningId, () -> {
         BackendPatchRequest backendBody = new BackendPatchRequest(
               body.getErsattningar().stream()
                     .map(e -> new BackendUpdateErsattning(
                           e.getErsattningId().toString(),
                           e.getBeslutsutfall().toString(),
                           e.getAvslagsanledning(),
                           true))
                     .toList());
         backendClient.patchErsattningar(handlaggningId.toString(), backendBody);
         backendClient.done(handlaggningId.toString());
      });
   }

   // uppgiftstyp is accepted in the path for FE compatibility but the backend exposes a single endpoint
   @Override
   public GetUppgiftsbeskrivningResponse getUppgiftsbeskrivning(String uppgiftstyp)
   {
      LOGGER.debug("GET /api/uppgiftsbeskrivning");
      BackendUppgiftsbeskrivning response = backendClient.getUppgiftsbeskrivning();
      return new GetUppgiftsbeskrivningResponse().beskrivning(response == null ? null : response.beskrivning());
   }

   // Exceptions are left to propagate to the framework's GlobalExceptionMapper; this only
   // scopes the handlaggningId MDC key for the duration of the call.
   private <T> T withLogContext(UUID handlaggningId, Supplier<T> action)
   {
      try (LogContext ignored = LogContext.put("handlaggningId", handlaggningId.toString()))
      {
         return action.get();
      }
   }

   private void withLogContext(UUID handlaggningId, Runnable action)
   {
      withLogContext(handlaggningId, () -> {
         action.run();
         return null;
      });
   }
}
