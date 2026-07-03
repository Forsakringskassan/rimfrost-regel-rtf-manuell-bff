package se.fk.github.rtfmanuellbff.model;

import java.util.List;

public record BackendPatchRequest(List<BackendUpdateErsattning> ersattningar) {}
