package se.fk.github.rtfmanuellbff.model;

public record Ersattning(String ersattningId,String ersattningstyp,int omfattningProcent,int belopp,int berakningsgrund,String beslutsutfall,String from,String tom,String avslagsanledning){}
