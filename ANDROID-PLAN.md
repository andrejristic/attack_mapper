# Attack Mapper Mobile — plan za Android (APK)

**Cilj:** Android aplikacija koja analizira AI razgovor i otkriva jailbreak napade.
Sve radi **na telefonu, bez interneta i bez slanja podataka**. Prvo se objavljuje
kao potpisan APK (GitHub Releases + javni repo `andrejristic/APK`), a kasnije ide
na Google Play.

**Polazno stanje:** `app.py` (~400 linija, Streamlit). Zavisi od
`sentence-transformers` (PyTorch), spaCy-ja i Streamlita, a nijedno od toga ne
radi na Androidu. Zato se aplikacija **prepisuje u Kotlin**. Logika detekcije su
prosta pravila, oko 150 linija.

**Redosled aplikacija:** Attack Mapper → Divide Reader → PSEEK. Ovo je prva, pa
služi i za proveru celog procesa (CI, potpisivanje, objavljivanje).

---

## Faza 0: priprema

### Andrej
- [x] **0.1 Prazan repo `APK`:** github.com → **+** → **New repository** →
      Owner `andrejristic`, ime `APK`, **Public**, ništa ne štiklirati
      (bez README, .gitignore i licence) → **Create repository**.
- [x] **0.2 Odluke:** vidi sekciju „Odluke“ na dnu.
- [ ] **0.5 Tajne za potpisivanje** (može i **na kraju**, pre prvog potpisanog
      releasa): u repou `attack_mapper` → **Settings** → **Secrets and variables** →
      **Actions** → **New repository secret**, upisati 4 tajne:
      `ANDROID_KEYSTORE_BASE64`, `ANDROID_KEYSTORE_PASSWORD`, `ANDROID_KEY_ALIAS`,
      `ANDROID_KEY_PASSWORD`. Vrednosti su u
      `/sdcard-storage/11CODE/attack_mapper/ANDROID-TAJNE.txt` (korak 0.4).
      Dok tajne nisu upisane, CI pravi nepotpisani test APK.
- [ ] **0.6 Rezervna kopija** keystore fajla i lozinki (password manager + Google Drive).
      Ako se ključ izgubi, APK verzija se više ne može ažurirati.

> Token nije potreban: sve tri aplikacije su u javnim repoima, a njihovi
> Releases se čitaju bez tokena.

### Claude
- [ ] **0.4** Instalirati JDK i generisati **jedan upload ključ** (`keytool`) za sve tri
      aplikacije. Keystore i lozinke idu u `/sdcard-storage/11CODE/attack_mapper/`
      (`upload-keystore.jks` + `ANDROID-TAJNE.txt`), nikad u git.

> **Pravilo za fajlove:** repo se razvija u `~/Git/attack_mapper`; sve što se
> generiše (APK, ključevi, tajne) ide u `/sdcard-storage/11CODE/attack_mapper/`.

## Faza 1: logika u Kotlinu
- [ ] 1.1 Android projekat u `android/`: Kotlin, Jetpack Compose, minSdk 26 (Android 8+),
      targetSdk po trenutnom zahtevu Google Play-a.
- [ ] 1.2 Prenos modela podataka: `TurnNode`, `ConversationGraph`.
- [ ] 1.3 Prenos 4 detektora i ukupnog rizika: persona drift, refusal erosion,
      topic escalation, obfuscation chain.
- [ ] 1.4 Prenos ankera za nameru (5 grupa) i osetljivih tema (4 grupe).
- [ ] 1.5 **Test podudarnosti:** isti razgovori kroz Python original i Kotlin verziju
      moraju dati iste rezultate (pragovi 0.25 / 0.4 / 0.5 / 0.6).

## Faza 2: embeddinzi na telefonu
- [ ] 2.1 Model `all-MiniLM-L6-v2`, kvantizovan za ARM (~23 MB, Apache-2.0 licenca,
      uz navođenje autora u aplikaciji).
- [ ] 2.2 ONNX Runtime Android za pokretanje modela.
- [ ] 2.3 WordPiece tokenizer u Kotlinu (`vocab.txt`).
- [ ] 2.4 Test: kosinusna sličnost u Kotlinu mora biti u okviru ±0.01 od Python
      `sentence-transformers`.
- [ ] 2.5 Embeddinzi ankera se računaju jednom i keširaju, pa je pokretanje brzo.
- [ ] 2.6 Umesto spaCy-ja: prepoznavanje entiteta preko liste ključnih reči i regexa.
      Logika za prekursore iz originala ostaje ista.

## Faza 3: interfejs
- [ ] 3.1 **Razgovor:** unos poruka sa izborom User/AI.
- [ ] 3.2 **Graf:** potezi obojeni po riziku, kao vertikalna vremenska linija.
- [ ] 3.3 **Monitor pretnji:** ukupni rizik, 4 detektora, upozorenja.
- [ ] 3.4 **Test primeri:** benigni razgovor, jailbreak u jednom potezu, napad kroz
      više poteza (preuzeti iz `app.py`).
- [ ] 3.5 **Izvoz u JSON** preko Android deljenja.
- [ ] 3.6 Svetla i tamna tema; jezik SR/EN sa **prekidačem u interfejsu**.

## Faza 4: mobilna prednost
- [ ] 4.1 **Share target:** tekst razgovora iz bilo koje aplikacije (ChatGPT, Claude…)
      ide pravo u Attack Mapper.
- [ ] 4.2 Parser koji prepozna ko govori u nalepljenom tekstu
      („User:“ / „Assistant:“, „You said“…).
- [ ] 4.3 Istorija analiza, čuva se samo lokalno.

## Faza 5: CI i objavljivanje
- [ ] 5.1 `.github/workflows/build-apk.yml`: testovi, build, potpis, **APK i AAB**
      (AAB je za kasniji Google Play).
- [ ] 5.2 Tag `v1.0.0` pravi **GitHub Release** sa `attack-mapper-1.0.0.apk`.
- [ ] 5.3 Build samo za `arm64-v8a` (svi moderni telefoni). APK će imati oko 35–45 MB.

## Faza 6: repo `APK`
- [ ] 6.1 README sa tabelom: aplikacija, verzija, datum, link za preuzimanje.
- [ ] 6.2 Workflow koji jednom na sat (i ručno, dugmetom) prođe kroz sve javne
      `andrejristic` repoe, pronađe release sa `.apk` fajlom i osveži tabelu.
- [ ] 6.3 Uputstvo „Kako instalirati APK“ (dozvola za instalaciju iz nepoznatih izvora), SR i EN.

## Vodič za Andreja (Claude piše na kraju)
- [ ] `VODIC-SR.md` u repou i kopija u `/sdcard-storage/11CODE/attack_mapper/`, sa
      svim koracima koje radi Andrej, korak po korak:
      repo `APK` (0.1), **4 tajne za potpisivanje (0.5)** sa objašnjenjem šta je svaka,
      rezervna kopija ključa (0.6), pravljenje releasa, instalacija APK-a na Chromebooku
      (Android apps + ADB sideloading) i na telefonu, test checklista (7.2) i Google Play koraci.

## Faza 7: test na telefonu
- [ ] 7.1 Andrej: preuzima APK sa linka iz repoa `APK` i instalira ga.
- [ ] 7.2 Andrej: prolazi checklistu (3 primera, share iz druge aplikacije, izvoz, tema).
- [ ] 7.3 Claude: popravlja prijavljeno i izdaje `v1.0.1`.

## Kasnije: Google Play (nije deo prve verzije)
- [ ] Andrej: na Play Console napraviti aplikaciju, uključiti **Play App Signing**,
      otpremiti AAB iz CI-ja.
- [ ] Claude: tekstovi za prodavnicu (SR/EN), screenshotovi, **politika privatnosti**
      („ne prikupljamo podatke, sve je lokalno“), **Data safety** formular.
- [ ] **Zatvoreno testiranje: 12 testera tokom 14 dana**, pa produkcija.

---

## Odluke (potvrdio Andrej, 2026-11-25)
1. **Package name:** `ca.zeravatech.attackmapper`
2. **Licenca:** GPL-3.0-or-later (kao ostali repoi)
3. **Jezik:** srpski i engleski; podrazumevano po jeziku sistema, **uz prekidač SR/EN u interfejsu**
4. **Ikonica:** chat oblačić (LLM razgovor) sa grafom od 3–4 povezana čvora (potezi); poslednji čvor crven = otkriven napad. Tamnoplava pozadina, beli oblačić.
5. **Prva verzija:** samo prenos postojećeg ponašanja; poboljšanja detekcije idu u v1.1
6. **Repo `APK`:** napravljen (javan, prazan) ✅
