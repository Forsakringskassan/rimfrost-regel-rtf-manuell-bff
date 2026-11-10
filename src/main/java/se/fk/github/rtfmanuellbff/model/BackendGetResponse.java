package se.fk.github.rtfmanuellbff.model;

import se.fk.rimfrost.regel.rtf.manuell.jaxrsspec.controllers.generatedsource.model.GetDataResponse;

// The regeltjänst wraps its data together with informationsaccess data (FRMM-FR-09.1)
public record BackendGetResponse(GetDataResponse data,BackendInformationsaccess informationsaccess){}
