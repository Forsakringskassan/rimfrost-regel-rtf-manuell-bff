package se.fk.github.rtfmanuellbff.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RawErsattning(
      @JsonProperty("ersattning_id") String ersattningId,
      @JsonProperty("ersattningstyp") String ersattningstyp,
      @JsonProperty("omfattning_procent") int omfattningProcent,
      @JsonProperty("belopp") int belopp,
      @JsonProperty("berakningsgrund") int berakningsgrund,
      @JsonProperty("beslutsutfall") String beslutsutfall,
      @JsonProperty("from") String from,
      @JsonProperty("tom") String tom,
      @JsonProperty("avslagsanledning") String avslagsanledning
) {}
