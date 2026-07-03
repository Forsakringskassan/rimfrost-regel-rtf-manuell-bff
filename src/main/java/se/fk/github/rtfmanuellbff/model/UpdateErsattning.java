package se.fk.github.rtfmanuellbff.model;

import jakarta.validation.constraints.NotBlank;

public record UpdateErsattning(@NotBlank String ersattningId,@NotBlank String beslutsutfall,String avslagsanledning){}
