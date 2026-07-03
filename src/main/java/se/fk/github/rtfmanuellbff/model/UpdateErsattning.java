package se.fk.github.rtfmanuellbff.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateErsattning(
      @NotBlank String ersattningId,
      @NotNull String beslutsutfall,
      String avslagsanledning
) {}
