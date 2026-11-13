package se.fk.github.rtfmanuellbff;

import se.fk.github.rtfmanuellbff.generated.model.Anstallning;
import se.fk.github.rtfmanuellbff.generated.model.Beslutsutfall;
import se.fk.github.rtfmanuellbff.generated.model.Ersattning;
import se.fk.github.rtfmanuellbff.generated.model.GetTaskResponse;
import se.fk.github.rtfmanuellbff.generated.model.Kon;
import se.fk.github.rtfmanuellbff.generated.model.Kund;
import se.fk.rimfrost.regel.rtf.manuell.jaxrsspec.controllers.generatedsource.model.GetDataResponse;

import java.util.List;

// Maps the regeltjänst model to the BFF's own API model. Both share class names, so the
// regeltjänst types are fully qualified.
final class GetTaskResponseMapper
{
   private GetTaskResponseMapper()
   {
   }

   static GetTaskResponse toGetTaskResponse(GetDataResponse data)
   {
      return new GetTaskResponse()
            .handlaggningId(data.getHandlaggningId())
            .kund(toKund(data.getKund()))
            .ersattningar(data.getErsattningar() == null
                  ? List.of()
                  : data.getErsattningar().stream()
                        .map(GetTaskResponseMapper::toErsattning)
                        .toList());
   }

   private static Kund toKund(se.fk.rimfrost.regel.rtf.manuell.jaxrsspec.controllers.generatedsource.model.Kund kund)
   {
      if (kund == null)
      {
         return null;
      }
      return new Kund()
            .fornamn(kund.getFornamn())
            .efternamn(kund.getEfternamn())
            .kon(kund.getKon() == null ? null : Kon.valueOf(kund.getKon().name()))
            .anstallning(toAnstallning(kund.getAnstallning()));
   }

   private static Anstallning toAnstallning(
         se.fk.rimfrost.regel.rtf.manuell.jaxrsspec.controllers.generatedsource.model.Anstallning anstallning)
   {
      if (anstallning == null)
      {
         return null;
      }
      return new Anstallning()
            .organisationsnummer(anstallning.getOrganisationsnummer())
            .organisationsnamn(anstallning.getOrganisationsnamn())
            .arbetstidProcent(anstallning.getArbetstidProcent())
            .anstallningsdag(anstallning.getAnstallningsdag())
            .sistaAnstallningsdag(anstallning.getSistaAnstallningsdag());
   }

   private static Ersattning toErsattning(
         se.fk.rimfrost.regel.rtf.manuell.jaxrsspec.controllers.generatedsource.model.Ersattning ersattning)
   {
      return new Ersattning()
            .ersattningId(ersattning.getErsattningId())
            .ersattningstyp(ersattning.getErsattningstyp())
            .omfattningProcent(ersattning.getOmfattningProcent())
            .belopp(ersattning.getBelopp())
            .berakningsgrund(ersattning.getBerakningsgrund())
            .beslutsutfall(ersattning.getBeslutsutfall() == null
                  ? null
                  : Beslutsutfall.valueOf(ersattning.getBeslutsutfall().name()))
            .avslagsanledning(ersattning.getAvslagsanledning())
            .from(ersattning.getFrom())
            .tom(ersattning.getTom());
   }
}
