# Attack Mapper za Android — vodič za Andreja

Ovde je sve što radiš ti, korak po korak. Ostalo (kod, testovi, build, objavljivanje) radi Claude.

**Gde su fajlovi:**
- Kod: repo [andrejristic/attack_mapper](https://github.com/andrejristic/attack_mapper), folder `android/`
- Ključ i tajne: SD kartica → `11CODE/attack_mapper/` (`upload-keystore.p12` + `ANDROID-TAJNE.txt`)
- Svi APK linkovi: [andrejristic/APK](https://github.com/andrejristic/APK)

---

## 1. Upis 4 tajne za potpisivanje (oko 10 minuta)

**Zašto:** Android instalira samo potpisane aplikacije, a svaka nova verzija mora biti potpisana
**istim ključem** kao prethodna. APK pravi GitHub, pa GitHub mora da ima ključ. Ključ ne sme u kod
(repo je javan), zato ide u **GitHub Secrets**: šifrovano skladište koje posle upisa ne vidi niko,
ni ti ni posetioci. Koristi ga samo build, u trenutku potpisivanja.

| Ime tajne | Šta je |
|---|---|
| `ANDROID_KEYSTORE_BASE64` | sam fajl ključa, pretvoren u tekst (dugačka linija, oko 5900 znakova) |
| `ANDROID_KEYSTORE_PASSWORD` | lozinka fajla ključa |
| `ANDROID_KEY_ALIAS` | ime ključa u fajlu: `zeravatech-upload` |
| `ANDROID_KEY_PASSWORD` | lozinka ključa (ista kao prethodna) |

**Koraci:**
1. Otvori fajl `11CODE/attack_mapper/ANDROID-TAJNE.txt` sa SD kartice. Sve 4 vrednosti su u njemu.
2. Na github.com otvori **andrejristic/attack_mapper**.
3. Klikni **Settings** (zupčanik, poslednja stavka u meniju repoa; na uskom ekranu je pod **⋯**).
4. Levo: **Secrets and variables** → **Actions**.
5. Klikni zeleno dugme **New repository secret**.
6. **Name:** tačno ime iz tabele (velika slova, bez razmaka). **Secret:** vrednost iz fajla
   (cela linija, bez razmaka na početku i kraju). Klikni **Add secret**.
7. Ponovi korake 5–6 za sve 4 tajne.

Na kraju na stranici vidiš 4 imena. Vrednosti se više ne prikazuju, to je normalno.

> Isti ključ važi i za **Divide Reader** i **PSEEK**. Kad dođu na red, iste 4 tajne upišeš i u njihove repoe.

## 2. Rezervna kopija ključa (5 minuta). Obavezno!

1. Sačuvaj **oba** fajla, `upload-keystore.p12` i `ANDROID-TAJNE.txt`, na dva mesta,
   na primer u password manager i na Google Drive.
2. Tek posle toga ih obriši sa SD kartice.

Ako se ključ izgubi, nijedna buduća verzija APK-a ne može da se instalira preko stare.
Korisnici bi morali da obrišu aplikaciju i instaliraju je ponovo.
(Na Google Play-u Google može da zameni izgubljen *upload* ključ, ali za APK van Play-a ne može.)

## 3. Pokretanje zvaničnog izdanja v1.0.0

Kad su tajne upisane, izaberi jedno od dva:
- **Najlakše:** javi Claudeu „tajne upisane“. On napravi tag `v1.0.0`, a GitHub sam napravi potpisani
  APK i AAB i objavi ih u **Releases**.
- **Sam, na GitHubu:** repo → **Releases** → **Draft a new release** → *Choose a tag* → upiši
  `v1.0.0` → **Create new tag** → naslov `Attack Mapper 1.0.0` → **Publish release**.
  Za oko 10 minuta build sam doda APK i AAB u taj release.

Za najviše sat vremena link se pojavi i u repou [APK](https://github.com/andrejristic/APK).
Odmah: repo APK → **Actions** → **update-apk-index** → **Run workflow**.

## 4. Instalacija i test (oko 30 minuta)

Test verzija (`…-test.apk`) se instalira **pored** zvanične, pa ne moraš ništa da brišeš.

**Telefon:** otvori repo [APK](https://github.com/andrejristic/APK) na telefonu → dodirni link →
otvori preuzeti fajl → dozvoli „Instaliraj nepoznate aplikacije“ za browser → **Instaliraj**.
Ako Play Protect upozori: **Više detalja → Ipak instaliraj**.

**Chromebook:** pogledaj uputstvo u repou [APK](https://github.com/andrejristic/APK#chromebook).
⚠️ Prvo uključivanje ADB-a na Chromebooku briše uređaj (Powerwash). Ako to ne želiš, testiraj na telefonu.

### Checklista za test
Prođi redom i javi Claudeu šta ne radi (najbolje sa screenshotom):

- [ ] Aplikacija se otvori, a gore se kratko vidi „Učitavam model na uređaju…“
- [ ] Prekidač **SR / EN** gore desno menja sav tekst
- [ ] ⋮ → **Primer: benigni razgovor** → tab **Pretnje** pokazuje zeleno „BEZBEDNO“
- [ ] ⋮ → **Primer: napad (više poteza)** → crvena ili narandžasta uzbuna, komponente popunjene,
      a tab **Mapa** pokazuje graf i liniju rizika
- [ ] ⋮ → **Primer: jailbreak (jedan potez)** → rezultat ima smisla
- [ ] Tab **Razgovor**: ručno dodaj nekoliko poteza (Korisnik / AI) i vidi kako se rezultat menja
- [ ] **Deljenje:** u ChatGPT/Claude aplikaciji (ili bilo gde) označi tekst razgovora →
      **Podeli** → **Attack Mapper** → proveri uloge (dodirom ih menjaš) → **Analiziraj**
- [ ] **Označavanje teksta:** označi tekst bilo gde → u meniju izaberi **Attack Mapper**
- [ ] ⋮ → **Nalepi razgovor** radi sa tekstom u formatu `User: … / Assistant: …`
- [ ] ⋮ → **Istorija** prikazuje prethodne analize, otvara ih i briše
- [ ] ⋮ → **Izvezi JSON** otvara meni za deljenje
- [ ] Svetla i tamna tema sistema (obe treba da budu čitljive)
- [ ] Na Chromebooku, u širokom prozoru, tri kolone stoje jedna pored druge

## 5. Google Play (kasnije, nije deo v1.0)

1. Na [Play Console](https://play.google.com/console) (nalog andrej.s.ristic@gmail.com):
   **Create app** → ime *Attack Mapper*, jezik, *App*, *Free*.
2. Uključi **Play App Signing** (predlog je podrazumevan).
3. Otpremi `.aab` fajl iz GitHub Release-a (ne APK) u **Testing → Closed testing**.
4. **Zatvoreno testiranje: najmanje 12 testera tokom 14 dana**, pa zahtev za produkciju.
5. Claude priprema tekstove za prodavnicu (SR/EN), screenshotove, politiku privatnosti
   i odgovore za **Data safety** (aplikacija ne prikuplja i ne šalje podatke).

---

## Šta je Claude uradio (ukratko)
- Kotlin verzija logike iz `app.py` (4 detektora, iste težine i pragovi) i isti model
  `all-MiniLM-L6-v2` na uređaju (~45 MB, ista preciznost kao original), bez interneta
- CI test pokreće **originalni** kod iz `app.py` i pada ako Android verzija bilo gde odstupa
- **Popravljena greška u `app.py`:** desktop verzija je padala na prvoj poruci
  (`np.max` na PyTorch tensoru)
- Interfejs: Razgovor / Mapa / Pretnje (na širokom ekranu tri kolone), prekidač SR/EN,
  deljenje i označavanje teksta, nalepljivanje, istorija, izvoz u JSON, ikonica
- Repo [APK](https://github.com/andrejristic/APK) sa linkovima koji se sami osvežavaju
- Licenca GPL-3.0 (`LICENSE`, `NOTICE`)
