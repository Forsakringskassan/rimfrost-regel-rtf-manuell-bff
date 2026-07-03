package se.fk.github.rtfmanuellbff.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RawAnstallning(
      @JsonProperty("anstallningsdag") String anstallningsdag,
      @JsonProperty("arbetstid_procent") int arbetstidProcent,
      @JsonProperty("sista_anstallningsdag") String sistaAnstallningsdag,
      @JsonProperty("organisationsnamn") String organisationsnamn,
      @JsonProperty("organisationsnummer") String organisationsnummer
) {}
