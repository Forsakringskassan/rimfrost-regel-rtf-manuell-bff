package se.fk.github.rtfmanuellbff.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RawKund(
      @JsonProperty("fornamn") String fornamn,
      @JsonProperty("efternamn") String efternamn,
      @JsonProperty("kon") String kon,
      @JsonProperty("anstallning") RawAnstallning anstallning
) {}
