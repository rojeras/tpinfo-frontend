# Verifiering av tpinfo-frontend

Följande steg kan användas för att verifiera tpinfo-frontend (hippo och statistik) efter det att kodförändringar genomförts.

## hippo

1. Gå till hippo
2. Välj datum *2022-01-09* i datumlisten.
3. 
   1. Antal *Tjänsteplattformar* skall vara 12/12
   2. Antal *Tjänstekonsumenter* > 653
   3. Antal *Logiska adresser* > 17 179
   4. Versionsnumret i *Om hippo*-knappen är 0.0.0 i utvecklingsmiljön.
4. Välj tjänsteplattformskombinationen *NTJP-PROD -> SLL-PROD*
   1. Knappen *Visa statistik* skall nu vara aktiverad (grön)   
   1. Antal *Tjänsteplattformar* skall vara 1/12
   2. Antal *Tjänstekonsumenter* > 653
   3. Antal *Logiska adresser* > 1544
5. Sök fram och välj tjänstedomänen *ehr:accesscontrol*
6. Välj (klicka på) tjänstekontraktet *AssertCareEngagement*
   1. Både kontrakt och domän ska nu vara valda.
7. Återställ tjänstedomänen
8. Återställ tjänstekontraktet
9. Sök fram och välj den logiska adressen *Capio CV Kista AB*
   1. Det skall nu finnas två tjänstekonsumenter i listan.
   2. Titta i datumlistan. Den skall avslutas med datumen:
      * 2021-01-13
      * 2019-06-13
10. Klicka på knappen *Om hippo* 
    1. Användarinformation skall visa
11. Stäng Användarinformationsfönstret

## Statistik

1. Gå till statistikvyn
2. Välj *Startdatum*: 2021-12-01
3. Verifiera att det tidigaste valbara slutdatumet nu är: 2021-12-01
4. Välj *Slutdatum*: 2021-12-15
   1. *Totalt antal anrop för detta urval* ska vara 14 722 737
5. I ruan *Visa* välj *Infektionsrapportering*
   1. *Totalt antal anrop* ska nu vara: 575 716
6. Klicka på *Visa samtliga*
   1. Det skall nu enbart finnas ett *Journalsystem* i kolumnen längst till vänster; *SLL TakeCare*.
7. Klicka upprepade gånger i rutan *Tekniska termer*
   1. Notera hur kolumnrubriker samt vissa objekt ändrar text. Ex blir "SLL TakeCare" nu "Region Stockholm --TakeCare --CGM-X (SE2321000016-8DSP)"
8. Välj Plattform *SLL-QA*
9. Välj *Journalen* i Visa-rutan
   1. Det skall nu visas att 953 anrop genomförts.
10. Klicka på *Visa samtliga*
11. Klick i rutan *Visa utveckling över tid*
    1. Den högsta spetsen skall visas för 2021-12-14.

