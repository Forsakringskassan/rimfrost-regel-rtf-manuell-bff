package se.fk.github.rtfmanuellbff.integration;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import se.fk.github.rtfmanuellbff.model.BackendPatchRequest;
import se.fk.rimfrost.regel.rtf.manuell.jaxrsspec.controllers.generatedsource.model.GetDataResponse;

@RegisterRestClient(configKey = "backend")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface RtfManuellClient
{
   @GET
   @Path("/{handlaggningId}")
   GetDataResponse getTask(
         @PathParam("handlaggningId") String handlaggningId,
         @HeaderParam("Authorization") String authorization);

   @PATCH
   @Path("/{handlaggningId}")
   void patchErsattningar(
         @PathParam("handlaggningId") String handlaggningId,
         BackendPatchRequest body,
         @HeaderParam("Authorization") String authorization);

   @POST
   @Path("/{handlaggningId}/done")
   void done(
         @PathParam("handlaggningId") String handlaggningId,
         @HeaderParam("Authorization") String authorization);

   @GET
   @Path("/utokadUppgiftsbeskrivning")
   JsonNode getUppgiftsbeskrivning();
}
