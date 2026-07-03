package se.fk.github.rtfmanuellbff;

import se.fk.github.rtfmanuellbff.model.*;

import java.util.List;

public class RtfManuellMapper
{
   public static GetDataResponse transform(RawGetDataResponse raw)
   {
      Anstallning anstallning = raw.kund().anstallning() != null
            ? transformAnstallning(raw.kund().anstallning())
            : null;

      Kund kund = new Kund(
            raw.kund().fornamn(),
            raw.kund().efternamn(),
            raw.kund().kon(),
            anstallning
      );

      List<Ersattning> ersattningar = raw.ersattningar() == null
            ? List.of()
            : raw.ersattningar().stream().map(RtfManuellMapper::transformErsattning).toList();

      return new GetDataResponse(raw.handlaggningId(), kund, ersattningar);
   }

   private static Anstallning transformAnstallning(RawAnstallning raw)
   {
      return new Anstallning(
            raw.anstallningsdag(),
            raw.arbetstidProcent(),
            raw.sistaAnstallningsdag(),
            raw.organisationsnamn(),
            raw.organisationsnummer()
      );
   }

   private static Ersattning transformErsattning(RawErsattning raw)
   {
      return new Ersattning(
            raw.ersattningId(),
            raw.ersattningstyp(),
            raw.omfattningProcent(),
            raw.belopp(),
            raw.berakningsgrund(),
            raw.beslutsutfall(),
            raw.from(),
            raw.tom(),
            raw.avslagsanledning()
      );
   }
}
