package se.fk.github.rtfmanuellbff;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ProcessingException;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.fk.github.rtfmanuellbff.integration.RtfManuellClient;
import se.fk.github.rtfmanuellbff.model.*;

import java.time.Instant;
import java.util.Map;
import java.util.function.Supplier;

@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RtfManuellBffController
{
   private static final Logger LOGGER = LoggerFactory.getLogger(RtfManuellBffController.class);

   @RestClient
   RtfManuellClient backendClient;

   @GET
   @Path("/health")
   public Response health()
   {
      return Response.ok(Map.of("status", "ok", "timestamp", Instant.now().toString())).build();
   }

   @GET
   @Path("/task/{handlaggningId}")
   public Response getTask(
         @PathParam("handlaggningId") String handlaggningId,
         @HeaderParam("Authorization") String authorization)
   {
      LOGGER.debug("GET /api/task/{}", handlaggningId);
      return call(handlaggningId, () -> {
         RawGetDataResponse raw = backendClient.getTask(handlaggningId, authorization);
         return Response.ok(RtfManuellMapper.transform(raw)).build();
      });
   }

   @POST
   @Path("/{handlaggningId}/patchErsattningar")
   public Response patchErsattningar(
         @PathParam("handlaggningId") String handlaggningId,
         @Valid PatchErsattningRequest body,
         @HeaderParam("Authorization") String authorization)
   {
      LOGGER.debug("POST /api/{}/patchErsattningar", handlaggningId);
      return call(handlaggningId, () -> {
         BackendPatchRequest backendBody = new BackendPatchRequest(
               body.ersattningar().stream()
                     .map(e -> new BackendUpdateErsattning(e.ersattningId(), e.beslutsutfall(), e.avslagsanledning(), true))
                     .toList()
         );
         backendClient.patchErsattningar(handlaggningId, backendBody, authorization);
         backendClient.done(handlaggningId, authorization);
         return Response.noContent().build();
      });
   }

   @GET
   @Path("/uppgiftsbeskrivning/{uppgiftstyp}")
   public Response getUppgiftsbeskrivning(@PathParam("uppgiftstyp") String uppgiftstyp)
   {
      LOGGER.debug("GET /api/uppgiftsbeskrivning/{}", uppgiftstyp);
      return call(null, () -> {
         JsonNode data = backendClient.getUppgiftsbeskrivning();
         return Response.ok(data).build();
      });
   }

   private Response call(String contextId, Supplier<Response> action)
   {
      try
      {
         return action.get();
      }
      catch (WebApplicationException e)
      {
         LOGGER.error("Upstream error contextId={}, status={}", contextId, e.getResponse().getStatus(), e);
         return Response.status(e.getResponse().getStatus()).entity(Map.of("error", "Upstream error")).build();
      }
      catch (ProcessingException e)
      {
         LOGGER.error("Backend unreachable contextId={}", contextId, e);
         return Response.status(502).entity(Map.of("error", "Upstream unavailable")).build();
      }
      catch (Exception e)
      {
         LOGGER.error("Internal error contextId={}", contextId, e);
         return Response.status(500).entity(Map.of("error", "Internal server error")).build();
      }
   }
}
