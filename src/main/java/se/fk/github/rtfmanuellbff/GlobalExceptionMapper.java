package se.fk.github.rtfmanuellbff;

import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class GlobalExceptionMapper
{
   private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionMapper.class);

   @ServerExceptionMapper
   public Response handleWebApplicationException(WebApplicationException e)
   {
      LOGGER.error("Upstream error status={}", e.getResponse().getStatus(), e);
      return Response.status(e.getResponse().getStatus())
            .entity(Map.of("error", "Upstream error")).build();
   }

   @ServerExceptionMapper
   public Response handleProcessingException(ProcessingException e)
   {
      LOGGER.error("Backend unreachable", e);
      return Response.status(502)
            .entity(Map.of("error", "Upstream unavailable")).build();
   }
}
