package se.fk.github.rtfmanuellbff.model;

import java.util.UUID;

public record BackendInformationsaccess(UUID uppgiftId,UUID regelId,String regelVersion,BackendAnvandare anvandare){}
