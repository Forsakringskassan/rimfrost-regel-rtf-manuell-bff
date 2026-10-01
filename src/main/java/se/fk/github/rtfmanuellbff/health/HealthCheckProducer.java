package se.fk.github.rtfmanuellbff.health;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.Readiness;
import se.fk.rimfrost.framework.bff.health.UpstreamHealthCheck;

@ApplicationScoped
public class HealthCheckProducer
{
   @Readiness
   @Produces
   HealthCheck backendHealthCheck()
   {
      return UpstreamHealthCheck.fromConfig("backend", "quarkus.rest-client.backend.url", 2000);
   }
}
