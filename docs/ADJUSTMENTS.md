# SmartBuild — Adjustments (ano ang binago namin)

**Para kanino:** clients / panelists — buod ng product changes (hindi internal engine names).  
**Related:** [WORKFLOW.md](./WORKFLOW.md)

---

## 1. Architecture (locked)

| Module | Experience now |
|--------|----------------|
| **0** | Intro lesson slides |
| **1** | Interactive **3D** PC assemble / disassemble |
| **2–4** | Hands-on Android labs (Guided + Scenario Assessment) |

Shell (auth, home, search, progress) = **Kotlin + Jetpack Compose** + **Supabase**.

---

## 2. Modules 2–4 — playground-first

Dati: maraming “click Continue / checklist” feel.  
**Ngayon:**

- **M2** — real crimp steps (strip, pins, crimp, LED) + topology with add/remove cables  
- **M3** — Packet Tracer–style canvas (drag, zoom, cables) + interactive Server Manager (hindi Continue-only)  
- **M4** — Windows desktop repair (drag wallpaper / junk, cleanup, CMD)

Guided = coaches. Assessment = scenario brief + faulted starts where designed.

---

## 3. Module 4 — Windows desktop (malaking polish)

| Before (rough) | After |
|----------------|--------|
| Header + big DO THIS + footer Reset/Validate kumain ng screen | **Full-screen** desktop; floating guide |
| Hardware Tools / air blower sa loob ng Windows | **Tinanggal** — software repair lang sa OS |
| Start button walang silbi | **Working Start menu** (Ticket, Pictures, Disk Cleanup, CMD, Action Center) |
| Ping = tap button | **Literal type** sa CMD + Enter; guide nagsasabi kung ano i-type |
| Dual progress chrome | Slim guide chips (Exit / n/6) |

**Service order ngayon:** Accept ticket → wallpaper → Recycle → Disk Cleanup → `ping` gateway → `ping 8.8.8.8` → close ticket.

---

## 4. Feedback & coaching

- Shared **Correct / Incorrect** style dialogs sa labs  
- Wrong cable / wrong slot / incomplete steps → immediate message  
- Assessment scenarios aligned sa manuscript (identify & correct faults)

---

## 5. Progress & accounts

- Progress **per signed-in user** (walang halo kapag magpalit ng account)  
- Sync sa Supabase `module_progress`  
- Guided unlocks Assessment; Assessment → 100%

---

## 6. Repo / app size cleanup

- Tinanggal ang unused lab prototypes at duplicate assets na wala na sa learner path  
- Slimmer embedded simulation pack for Modules 0–1  
- Dead preview routes removed  
- Demo APK handoff path: project root `SmartBuild-V2-demo.apk` (rebuild as needed)

---

## 7. Documentation split (this folder)

| Doc | Topic |
|-----|--------|
| [BACKEND.md](./BACKEND.md) | Ano ang nangyayari sa cloud (plain language) |
| [SUPABASE.md](./SUPABASE.md) | Auth, table, SQL, sync, troubleshooting |
| [PLAYSTORE.md](./PLAYSTORE.md) | Play readiness + rental pricing |
| [WORKFLOW.md](./WORKFLOW.md) | Learner flow per module |
| [ADJUSTMENTS.md](./ADJUSTMENTS.md) | This change log |

Handover set (English): [HANDOVER](./HANDOVER.md), [ARCHITECTURE](./ARCHITECTURE.md),
[SETUP_AND_BUILD](./SETUP_AND_BUILD.md), [ANDROID_APP](./ANDROID_APP.md),
[MODULE_REFERENCE](./MODULE_REFERENCE.md),
[MAINTENANCE_GUIDE](./MAINTENANCE_GUIDE.md), [TROUBLESHOOTING](./TROUBLESHOOTING.md),
[KNOWN_ISSUES](./KNOWN_ISSUES.md), [RELEASE](./RELEASE.md), [USER_MANUAL](./USER_MANUAL.md),
[CHANGELOG](./CHANGELOG.md). Buong listahan: [README](./README.md).

---

## 8. Still optional / later

- Device smoke checklist after every new APK ([RELEASE §4](./RELEASE.md#4-pre-release-checklist))  
- Play Store signing + AAB + privacy policy (see [RELEASE](./RELEASE.md) at PLAYSTORE)  
- Optional: gate Assessment behind payment code (not enabled — full APK demo given)  

---

## 9. Client revisions — 29 Sep 2026

1. **Module 3** — hindi na nagfo-force exit pag nag-type ng path sa PC File Explorer (Guided at Assessment).  
2. **Module 2** — may Back / exit button na sa lahat ng station.  
3. **Module 1 disassembly** — pag tinanggal ang motherboard, kasama nang lumalabas ang RAM, CPU fan at CPU; doon na sila tatanggalin isa-isa.  
4. **Module 0** — tinanggal ang Search button (nasa Home pa rin ang Component Search).  

Details at files: [CHANGELOG](./CHANGELOG.md#29-sep-2026--client-revision-round). Kasama na ito sa official **V2.1** APK.
