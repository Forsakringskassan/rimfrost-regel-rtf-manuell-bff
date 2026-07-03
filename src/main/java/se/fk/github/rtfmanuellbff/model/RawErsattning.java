package se.fk.github.rtfmanuellbff.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RawErsattning(
      @JsonProperty("ersattning_id") String ersattningId,
      String ersattningstyp,
      @JsonProperty("omfattning_procent") int omfattningProcent,
      int belopp,
      int berakningsgrund,
      String beslutsutfall,
      String from,
      String tom,
      String avslagsanledning
) {}
