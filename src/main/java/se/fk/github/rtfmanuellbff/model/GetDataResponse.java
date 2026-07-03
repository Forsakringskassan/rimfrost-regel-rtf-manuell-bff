package se.fk.github.rtfmanuellbff.model;

import java.util.List;

public record GetDataResponse(String handlaggningId,Kund kund,List<Ersattning>ersattningar){}
