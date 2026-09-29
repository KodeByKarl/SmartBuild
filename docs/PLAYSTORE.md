# SmartBuild — Play Store & pricing

**Para kanino:** thesis clients / kayo na magpu-publish o magre-rent ng Play Console.  
**Related:** [BACKEND.md](./BACKEND.md)

---

## 1. Ready ba sa Play Store ngayon?

| Item | Status |
|------|--------|
| Working demo (modules 0–4) | Yes — sideload APK |
| Supabase auth + progress | Yes — kung naka-setup ang project |
| Release signing keystore | **Hindi pa** (kailangan sa Play) |
| App Bundle (AAB) | Prefer over raw APK |
| Privacy policy URL | **Required** |
| Data safety form | **Required** (account + progress) |
| Store listing (screenshots, description) | Ihahanda pa |
| Package name | `com.example.smart_build` — palitan kung may client brand |

**Verdict**

- **Demo / panel / sideload:** OK  
- **Public Play listing:** OK lang pagkatapos ng signing + AAB + listing + policy  

---

## 2. Play Console checklist (kapag mag-upload na)

1. Play Console account (Google registration fee — usually **~$25 USD**, confirm sa Google)  
2. Create app → final **applicationId**  
3. Generate **upload keystore** (backup offline)  
4. Build signed **AAB**  
5. Store listing: title, description, screenshots, icon  
6. Privacy policy (Auth + progress sync)  
7. Data safety questionnaire  
8. Content rating  
9. **Internal testing** muna → tapos Production  
10. Verify deep links (`smartbuild://auth…`) sa release build  

---

## 3. Rent / gamitin ang Dev account ninyo

Kung **inyo** ang Google Play Developer account at **rirentahan** ng client:

| Package | Kasama | Suggested PHP |
|---------|--------|----------------|
| **A — APK only** | Demo APK + install help | ₱3,000 – ₱8,000 |
| **B — Publish for them** | AAB + listing + 1 review-fix round on **your** console | ₱15,000 – ₱25,000 (+ Google $25 if new) |
| **C — Monthly rental** | App stays under **your** Play Console | ₱2,000 – ₱5,000 / month **or** ₱15k–30k / year |
| **D — Transfer to their account** | They pay Google $25; you transfer app | ₱10,000 – ₱20,000 |
| **E — Maintenance** | Small fixes after defense | ₱1,500 – ₱3,000 / month |

### DDP (down payment) example

| Milestone | Deliverable | Share |
|-----------|-------------|-------|
| DDP | Sideload APK + Guided demo | 40–50% |
| Balance | Play live **or** full access / source / transfer | 50–60% |
| Optional | Keep listing on your console | Package **C** |

### Contract tips

- Huwag ibigay ang Google password; kayo ang mag-publish (o limited Play user).  
- Full payment muna bago transfer / full source.  
- Unpaid rental → unpublish after written notice (7–14 days).  
- Client responsible for content that can ban the account.  

*Final numbers = sa written agreement ninyo.*

---

## 4. Cost breakdown

| Cost | Who pays |
|------|----------|
| Google Play registration ~$25 | Usually bill to client (Package B/D) |
| Your labor (listing) | Client — Package B |
| Ongoing hosting on your console | Client — Package C |
| Supabase free tier | Usually enough for thesis |

---

## 5. Possible Play-related problems

| Problem | Notes |
|---------|--------|
| APK too large (~146 MB as of 29 Sep 2026) | Play prefers AAB; may need Asset Delivery / size plan — see [RELEASE §5](./RELEASE.md#5-google-play-specifics) |
| Rejected listing | Missing privacy policy / Data safety / screenshots |
| Deep link broken after rename | Update Supabase redirect URLs + `applicationId` |
| Lost keystore | Cannot update same listing — guard the key file |

See also: [SUPABASE.md](./SUPABASE.md) problems table.  
