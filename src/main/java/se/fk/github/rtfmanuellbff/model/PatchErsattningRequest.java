package se.fk.github.rtfmanuellbff.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record PatchErsattningRequest(
      @NotNull @NotEmpty @Valid List<UpdateErsattning> ersattningar
) {}
