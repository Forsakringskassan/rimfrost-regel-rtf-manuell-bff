package se.fk.github.rtfmanuellbff.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import se.fk.rimfrost.regel.rtf.manuell.jaxrsspec.controllers.generatedsource.model.UpdateErsattning;

import java.util.List;

public record PatchErsattningRequest(@NotNull @NotEmpty @Valid List<UpdateErsattning>ersattningar){}
