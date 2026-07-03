package se.fk.github.rtfmanuellbff.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record RawGetDataResponse(@JsonProperty("handlaggning_id")String handlaggningId,RawKund kund,List<RawErsattning>ersattningar){}
