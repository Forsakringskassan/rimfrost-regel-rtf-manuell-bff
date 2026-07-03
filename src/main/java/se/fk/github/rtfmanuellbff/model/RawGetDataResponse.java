package se.fk.github.rtfmanuellbff.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record RawGetDataResponse(
      @JsonProperty("handlaggning_id") String handlaggningId,
      @JsonProperty("kund") RawKund kund,
      @JsonProperty("ersattningar") List<RawErsattning> ersattningar
) {}
