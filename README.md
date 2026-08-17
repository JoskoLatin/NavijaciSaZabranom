# Navijači sa zabranom

Android aplikacija koja podsjeća na obvezu javljanja nadležnoj policijskoj postaji na dan
utakmice praćenog kluba. Namijenjena je osobama kojima je izrečena zabrana pristupa
sportskom natjecanju, kao pomoć u poštivanju te obveze.

Aplikacija je alat za podsjećanje. **Nije službena obavijest ni zamjena za rješenje o
zabrani** — mjerodavni su rješenje i nadležna policijska postaja.

## Što radi

- Odabir kluba iz svih rangova hrvatskih seniorskih natjecanja (HNL do županijskih liga).
- Preuzimanje rasporeda, uključujući europske utakmice (Liga prvaka, Europska i
  Konferencijska liga) i raspored A reprezentacije.
- Odbrojavanje do sljedeće utakmice („Danas", „Sutra", „Za 2 dana").
- Skupni upis svih nadolazećih termina u kalendar uređaja, s podsjetnikom u 9 ujutro na
  dan utakmice. Kalendar je pouzdaniji od alarma jer neki uređaji agresivno gase
  pozadinske aplikacije.
- Obavijesti na dan utakmice i (neobavezno) večer prije.
- Prepoznavanje novih i promijenjenih termina te termina obrisanih u kalendaru.

## Izvori podataka

| Izvor | Namjena |
| --- | --- |
| `semafor.hns.family` | popis klubova i klupski rasporedi (HTML, jsoup) |
| `rezultati.hns.team` | raspored A reprezentacije (HTML, jsoup) |
| `match.uefa.com/v5/matches` | europske utakmice hrvatskih klubova (neslužbeni JSON) |

Dohvat je isključivo čitanje javno objavljenih stranica, bez slanja korisničkih podataka.

## Tehnologije

Kotlin · Jetpack Compose (Material 3) · MVVM + Repository · Hilt · Room · DataStore ·
WorkManager · Coil · jsoup · Firebase Authentication i Cloud Firestore

- `minSdk` 24, `compileSdk` / `targetSdk` 35, Java 17
- Release build koristi R8 (`minifyEnabled`) i `shrinkResources`

## Build

```bash
./gradlew assembleDebug          # debug APK
./gradlew testDebugUnitTest      # unit testovi
./gradlew lintDebug              # lint
./gradlew bundleRelease          # potpisani AAB za Play (traži keystore.properties)
```

### Firebase konfiguracija

`app/google-services.json` **nije u repozitoriju**. Projekt se bez njega kompajlira
(Google Services plugin primjenjuje se samo ako datoteka postoji), ali prijava neće
raditi. Za punu funkcionalnost napravite vlastiti Firebase projekt, uključite
Authentication (email/lozinka i Google) te Cloud Firestore, i preuzeti
`google-services.json` stavite u `app/`.

Za Google prijavu u Firebase treba dodati SHA-1 otiske debug ključa te — ako se objavljuje
na Play — i ključa Play App Signinga.

### Potpisivanje release verzije

Tajne se čitaju iz `keystore.properties` u korijenu projekta (u `.gitignore`). Predložak
je [keystore.properties.example](keystore.properties.example):

```properties
storeFile=navijaci-release.jks
storePassword=…
keyAlias=navijaci
keyPassword=…
```

Ako datoteka ne postoji, release ostaje nepotpisan, a debug build i dalje radi.
Keystore (`.jks`) i lozinke se nikad ne commitaju — bez tog ključa nije moguće objaviti
ažuriranje postojeće aplikacije na Play.

## Struktura

```
app/src/main/java/com/navijacisazabranom/app/
├── data/            auth, hns (parseri i repozitoriji), postavke, statistika
├── di/              Hilt moduli
├── kalendar/        upis termina u kalendar uređaja
├── notifikacije/    alarmi i obavijesti
├── sync/            WorkManager (raspored, indeks klubova, boot)
└── ui/screens/      login, tražilica, profil (Profil/Klub/Upute), raspored, reprezentacija
```

## Privatnost

Praćeni klub, preuzeti raspored, profilna slika i termini u kalendaru ostaju na uređaju.
Izvan uređaja idu samo podaci računa za prijavu (Firebase Authentication) i podatak o tome
koji je klub odabran, radi statistike. Detaljno: [play/politika-privatnosti.md](play/politika-privatnosti.md).
