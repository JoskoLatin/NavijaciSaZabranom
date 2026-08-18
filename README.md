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

## Licenca

Izvorni kod objavljen je pod **GNU General Public License v3.0** — vidi [LICENSE](LICENSE).
Riječ je o copyleft licenci: tko distribuira izmijenjenu verziju, mora i nju objaviti pod
istom licencom i učiniti izvorni kod dostupnim.

### Dodatno dopuštenje (GPL v3, članak 7)

Aplikacija se povezuje s Googleovim vlasničkim bibliotekama (Google Play services, Firebase
SDK, Android SDK), koje nisu GPL-kompatibilne. Bez izričitog dopuštenja distribucija
prevedene aplikacije bila bi u sukobu s GPL-om, pa nositelj autorskih prava daje sljedeće
dodatno dopuštenje:

> Kao iznimku prema članku 7. GNU GPL-a v3, nositelj autorskih prava daje dopuštenje za
> povezivanje ovog programa s Googleovim vlasničkim bibliotekama (Google Play services,
> Firebase SDK i Android SDK) te za distribuciju tako nastaloga kombiniranog djela. Ovo
> dopuštenje vrijedi i za izmijenjene verzije programa, pod uvjetom da se zadrži.

> As an additional permission under section 7 of the GNU GPL v3, the copyright holder grants
> permission to link this program with Google's proprietary libraries (Google Play services,
> Firebase SDK and the Android SDK), and to distribute the resulting combined work. This
> permission also applies to modified versions of the program, provided this permission is
> retained.

### Sadržaj koji nije obuhvaćen licencom

Licenca se odnosi **samo na izvorni kod ovog projekta**. Ne obuhvaća:

- **Logotip HNS-a** (`app/src/main/res/drawable-nodpi/ic_hns.png`) — zaštićeni znak
  Hrvatskog nogometnog saveza, korišten radi prepoznavanja. Nositelj projekta nije njegov
  vlasnik i ne daje pravo na njegovo korištenje.
- **Grbovi klubova** — ne nalaze se u repozitoriju; aplikacija ih dohvaća s javnih HNS-ovih
  adresa u trenutku prikaza. Prava pripadaju klubovima.
- **Podatke o rasporedima** — vlasništvo su izvora navedenih u odjeljku *Izvori podataka*.

Aplikacija nije povezana s HNS-om, UEFA-om ni bilo kojim klubom niti od njih odobrena.

### Uvjeti korištenja izvora podataka

Opći uvjeti korištenja HNS-ovih web stranica (čl. 9) zabranjuju pohranu i kopiranje sadržaja
za bilo koju svrhu **osim osobne uporabe**. Aplikacija je građena u skladu s tim: podatke
dohvaća sam uređaj, za svog korisnika, i nigdje ne postoji središnja kopija podataka.
Tko projekt koristi drukčije — osobito za poslužiteljsko prikupljanje ili preprodaju
podataka — dužan je sam ishoditi suglasnost nositelja prava.

Made in Vodice.
