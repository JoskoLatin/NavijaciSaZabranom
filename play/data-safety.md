# Data Safety formular — upute za popunjavanje (Play Console)

Ovo su odgovori za **App content → Data safety** u Google Play Console, usklađeni s [politikom privatnosti](politika-privatnosti.md). Cilj: prijaviti točno ono što aplikacija stvarno radi.

> **Važno:** Google traži da prijaviš i podatke koje skupljaju SDK-ovi (Firebase), ne samo tvoj kod. Prije slanja provjeri i službenu Firebase tablicu: https://firebase.google.com/docs/android/play-data-disclosure

## 1. Prikuplja li aplikacija podatke? → **DA**

## 2. Vrste podataka (Data types)

### Personal info → Email address
- **Collected:** Da · **Shared:** Ne
- **Purpose:** Account management (prijava/registracija)
- **Linked to user identity:** Da
- **Required or optional:** Required (nužno za račun)
- **Processed ephemerally:** Ne

### Personal info → User IDs
- **Collected:** Da · **Shared:** Ne
- **Purpose:** App functionality + Analytics (identifikator računa uz statistiku)
- **Linked to user identity:** Da
- **Required or optional:** Required

### App activity → App interactions
*(odabir kluba koji korisnik prati — bilježi se za statistiku)*
- **Collected:** Da · **Shared:** Ne
- **Purpose:** Analytics
- **Linked to user identity:** Da (vezano uz identifikator računa)
- **Required or optional:** Required

### Calendar → Calendar events → **NE prijavljuje se kao prikupljanje**
*(aplikacija traži dozvole `READ_CALENDAR` i `WRITE_CALENDAR` za „Dodaj cijelu sezonu u kalendar")*
- **Collected:** **NE** · **Shared:** **NE**
- Termini se upisuju **lokalno u kalendar na uređaju**; ništa se ne šalje nama ni trećima.
- Čitanje kalendara koristi se **samo** da se provjeri je li korisnik termin u međuvremenu obrisao (da se ne stvore duplikati).
- Prema Play pravilima podatak koji **ne napušta uređaj nije „prikupljanje"**, pa u formularu za Calendar **ne označavaš ništa**.
- Ako te konzola pita za obrazloženje dozvole: *„Upis termina utakmica u korisnikov kalendar na njegov zahtjev; podaci ostaju na uređaju."*

### Photos and videos → Photos → **NE prijavljuje se kao prikupljanje**
*(profilna slika na Profil tabu)*
- **Collected:** **NE** · **Shared:** **NE**
- Slika se bira **sistemskim biračem slika** (bez dozvole za galeriju) i kopira **u internu memoriju aplikacije**; ne šalje se nikamo.

### (Provjeri kroz Firebase tablicu) Device or other IDs / Approximate location
Firebase Auth/Firestore mogu za rad koristiti IP adresu (gruba lokacija) i identifikatore uređaja. Prema Firebase tablici obično se prijavljuju kao **Collected, Not shared, App functionality**. Otvori gornji Firebase link i za Authentication + Firestore prepiši što navode — ako navode, dodaj te vrste.

## 3. Security practices
- **Is all of the user data encrypted in transit?** → **DA** (HTTPS)
- **Do you provide a way for users to request that their data be deleted?** → **DA** (zahtjev emailom; opiši u politici — već je opisano)
- Independent security review: nije obavezno (ostavi neoznačeno ako ga nema)

## 4. Ostalo u konzoli
- **Privacy policy URL:** unesi javni URL na kojem hostaš `politika-privatnosti.html`
- **Target audience / Content rating:** ispuni upitnik; aplikacija **nije za djecu** (dob 16+), pa nemoj označavati dječju publiku
- **Data deletion:** u odjeljku o brisanju navedi da se briše na zahtjev putem kontakt emaila

## Sažetak (što točno prijavljuješ)
| Podatak | Skuplja | Dijeli | Svrha | Vezano uz identitet |
|---|---|---|---|---|
| Email adresa | Da | Ne | Upravljanje računom | Da |
| Identifikator računa (uid) | Da | Ne | Funkcionalnost + statistika | Da |
| Interakcija (odabir kluba) | Da | Ne | Statistika | Da |
| (Firebase: IP/uređaj — provjeri) | vjer. Da | Ne | Funkcionalnost | ovisno |
| **Kalendar (termini utakmica)** | **Ne** | **Ne** | ostaje na uređaju | — |
| **Profilna slika** | **Ne** | **Ne** | ostaje na uređaju | — |
