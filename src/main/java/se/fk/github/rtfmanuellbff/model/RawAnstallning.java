package se.fk.github.rtfmanuellbff.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RawAnstallning(String anstallningsdag,@JsonProperty("arbetstid_procent")int arbetstidProcent,@JsonProperty("sista_anstallningsdag")String sistaAnstallningsdag,String organisationsnamn,String organisationsnummer){}
