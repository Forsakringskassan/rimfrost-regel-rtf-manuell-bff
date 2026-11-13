package se.fk.github.rtfmanuellbff.integration;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import se.fk.github.rtfmanuellbff.model.BackendGetResponse;
import se.fk.github.rtfmanuellbff.model.BackendPatchRequest;
import se.fk.github.rtfmanuellbff.model.BackendUppgiftsbeskrivning;

@RegisterRestClient(configKey = "backend")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface RtfManuellClient
{
   @GET
   @Path("/{handlaggningId}")
   BackendGetResponse getTask(@PathParam("handlaggningId") String handlaggningId);

   @PATCH
   @Path("/{handlaggningId}")
   void patchErsattningar(
         @PathParam("handlaggningId") String handlaggningId,
         BackendPatchRequest body);

   @POST
   @Path("/{handlaggningId}/done")
   void done(@PathParam("handlaggningId") String handlaggningId);

   @GET
   @Path("/utokadUppgiftsbeskrivning")
   BackendUppgiftsbeskrivning getUppgiftsbeskrivning();
}
