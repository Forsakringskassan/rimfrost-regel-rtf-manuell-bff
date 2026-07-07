package se.fk.github.rtfmanuellbff;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.fk.github.rtfmanuellbff.integration.RtfManuellClient;
import se.fk.github.rtfmanuellbff.model.BackendPatchRequest;
import se.fk.github.rtfmanuellbff.model.BackendUpdateErsattning;
import se.fk.github.rtfmanuellbff.model.PatchErsattningRequest;
import se.fk.rimfrost.regel.rtf.manuell.jaxrsspec.controllers.generatedsource.model.GetDataResponse;

@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RtfManuellBffController
{
   private static final Logger LOGGER = LoggerFactory.getLogger(RtfManuellBffController.class);

   @RestClient
   RtfManuellClient backendClient;

   @GET
   @Path("/task/{handlaggningId}")
   public Response getTask(
         @PathParam("handlaggningId") String handlaggningId,
         @HeaderParam("Authorization") String authorization)
   {
      LOGGER.debug("GET /api/task/{}", handlaggningId);
      GetDataResponse response = backendClient.getTask(handlaggningId, authorization);
      return Response.ok(response).build();
   }

   @POST
   @Path("/{handlaggningId}/patchErsattningar")
   public Response patchErsattningar(
         @PathParam("handlaggningId") String handlaggningId,
         @Valid PatchErsattningRequest body,
         @HeaderParam("Authorization") String authorization)
   {
      LOGGER.debug("POST /api/{}/patchErsattningar", handlaggningId);
      BackendPatchRequest backendBody = new BackendPatchRequest(
            body.ersattningar().stream()
                  .map(e -> new BackendUpdateErsattning(
                        e.getErsattningId().toString(),
                        e.getBeslutsutfall().toString(),
                        e.getAvslagsanledning(),
                        true))
                  .toList());
      backendClient.patchErsattningar(handlaggningId, backendBody, authorization);
      backendClient.done(handlaggningId, authorization);
      return Response.noContent().build();
   }

   // uppgiftstyp is accepted in the path for FE compatibility but the backend exposes a single endpoint
   @GET
   @Path("/uppgiftsbeskrivning/{uppgiftstyp}")
   public Response getUppgiftsbeskrivning()
   {
      LOGGER.debug("GET /api/uppgiftsbeskrivning");
      JsonNode data = backendClient.getUppgiftsbeskrivning();
      return Response.ok(data).build();
   }
}
