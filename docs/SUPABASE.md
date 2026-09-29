# SmartBuild — Supabase integration

**Para kanino:** developer / client na magse-setup o magme-maintain ng project.  
**Related:** [BACKEND.md](./BACKEND.md)

---

## 1. Ano ang ginagamit

| Supabase feature | Gamit sa SmartBuild |
|------------------|---------------------|
| **Auth** | Sign up, sign in, sign out, password reset |
| **PostgREST (Database API)** | Read/write `module_progress` |
| **Functions** | Naka-install sa SDK; hindi required para sa core progress |

Android client: Kotlin Supabase SDK (`auth-kt`, `postgrest-kt`) + **Ktor**.

---

## 2. Secrets / config

File: `SmartBuild/secrets.properties` (**huwag i-commit**)

```properties
SUPABASE_URL=https://YOUR_PROJECT.supabase.co
SUPABASE_ANON_KEY=YOUR_ANON_OR_PUBLISHABLE_KEY
# Optional deep link:
# SUPABASE_AUTH_SCHEME=smartbuild
# SUPABASE_AUTH_HOST=auth
```

- Gradle → `BuildConfig.SUPABASE_*`
- Environment variables (same names) override the file
- **Anon key only** sa APK — **never** `service_role`

Copy from: `secrets.properties.example`

---

## 3. Auth deep links

Default: `smartbuild://auth`

Sa Supabase Dashboard → **Authentication → URL Configuration**, allow:

- `smartbuild://auth`
- `smartbuild://auth/confirm` (kung may email confirm)
- `smartbuild://auth/reset`

App uses **PKCE** flow (mas reliable sa email apps kaysa implicit hash tokens).

**Optional https bounce page:** `SmartBuild/web/auth-reset.html` — static page na nire-redirect
ang browser sa `smartbuild://auth/reset` (kasama ang query/hash). I-host ito (hal. GitHub Pages
o Vercel) kung may desktop mail client na nagbubukas ng blank page, at idagdag ang https URL
nito sa redirect allow-list.

> Kapag pinalitan ang scheme/host, i-update din ang `AndroidManifest.xml` at
> `web/auth-reset.html` — hard-coded doon ang `smartbuild://auth`. Tingnan ang
> [MAINTENANCE_GUIDE §9](./MAINTENANCE_GUIDE.md#9-change-the-auth-deep-link-schemehost).

---

## 4. Database table

### `public.module_progress`

| Column | Type | Meaning |
|--------|------|---------|
| `user_id` | uuid → `auth.users` | Student |
| `module_id` | int (`0`–`4`) | Module number |
| `percent` | real | 0–100 |
| `guided_done` | boolean | Guided finished |
| `assessment_done` | boolean | Assessment finished |
| `updated_at` | timestamptz | Last write |
| **Primary key** | `(user_id, module_id)` | Upsert target |

### SQL (run once sa SQL Editor)

```sql
create table if not exists public.module_progress (
  user_id uuid not null references auth.users(id) on delete cascade,
  module_id int not null,
  percent real not null default 0,
  guided_done boolean not null default false,
  assessment_done boolean not null default false,
  updated_at timestamptz not null default now(),
  primary key (user_id, module_id)
);

alter table public.module_progress enable row level security;

create policy "Users manage own progress"
  on public.module_progress
  for all
  using (auth.uid() = user_id)
  with check (auth.uid() = user_id);
```

Pagkatapos magbago ng schema: **Settings → API → Reload schema** (iwas `PGRST204`).

---

## 5. Paano naka-integrate ang app

| Operation | Trigger | Code path |
|-----------|---------|-----------|
| **Pull** | Home open / resume | `ModuleProgressStore.pullFromRemote` → `ModuleProgressRepository.fetchAll` |
| **Push** | Progress / Guided / Assessment / Retake | `ModuleProgressRepository.upsert` (`onConflict = user_id,module_id`) |
| **Bind account** | Login / Home | Prefs file per `user_id` — walang halo sa ibang student |

### Progress rules (enforced sa app)

- Percent **hindi bumababa**
- Max **99%** hanggang Assessment (o Module 0 complete)
- Assessment → **100%** + `assessment_done = true`
- Guided → `guided_done = true`, percent ≥ 50%, ≤ 99%

---

## 6. Common Supabase problems

| Symptom | Fix |
|---------|-----|
| Empty URL/key crash / no auth | Fill `secrets.properties`, rebuild |
| Email not confirmed | Turn off confirm **or** confirm via mail |
| Reset password walang session | Check URL allow-list + PKCE APK |
| Upsert `PGRST204` | Reload API schema |
| 401 / 403 on progress | Must be signed in; check RLS policy |
| Progress missing after reinstall | Sign in again; pull remote |

---

## 7. Checklist bago i-demo

- [ ] Supabase project live  
- [ ] `module_progress` + RLS created  
- [ ] Schema reloaded  
- [ ] `secrets.properties` filled  
- [ ] Auth redirect URLs set  
- [ ] Test: sign up → finish one Guided → row appears sa Table Editor  
- [ ] `delete-user` Edge Function deployed (section 8)  

---

## 8. Edge Function: `delete-user`

**Profile → Delete Account** in the app calls
`supabase.functions.invoke("delete-user")` (`AuthViewModel.deleteAccount()`), then signs out.
Deleting an Auth user needs the `service_role` key, so it must run server-side.

The function's source is **not in the repositories** — it lives only in the Supabase project.
Check it under **Edge Functions** in the dashboard. If it is missing (or you move to a new
project), deploy this reference implementation:

`supabase/functions/delete-user/index.ts`

```ts
import { createClient } from "npm:@supabase/supabase-js@2";

Deno.serve(async (req) => {
  const authHeader = req.headers.get("Authorization");
  if (!authHeader) return new Response("Unauthorized", { status: 401 });

  const url = Deno.env.get("SUPABASE_URL")!;
  const userClient = createClient(url, Deno.env.get("SUPABASE_ANON_KEY")!, {
    global: { headers: { Authorization: authHeader } },
  });
  const { data: { user }, error } = await userClient.auth.getUser();
  if (error || !user) return new Response("Unauthorized", { status: 401 });

  const admin = createClient(url, Deno.env.get("SUPABASE_SERVICE_ROLE_KEY")!);
  const { error: deleteError } = await admin.auth.admin.deleteUser(user.id);
  if (deleteError) return new Response(deleteError.message, { status: 500 });

  return new Response(JSON.stringify({ ok: true }), {
    headers: { "Content-Type": "application/json" },
  });
});
```

Deploy with the Supabase CLI:

```powershell
supabase login
supabase link --project-ref <your-project-ref>
supabase functions deploy delete-user
```

`SUPABASE_URL`, `SUPABASE_ANON_KEY` and `SUPABASE_SERVICE_ROLE_KEY` are provided to Edge
Functions automatically. Because `module_progress.user_id` references `auth.users` with
`on delete cascade`, the user's progress rows are removed with the account.

---

## 9. Moving to a new Supabase project

1. Create the project (any region close to the users).
2. Run the SQL in section 4, then **Settings → API → Reload schema**.
3. Deploy the `delete-user` function (section 8).
4. **Authentication → URL Configuration:** add the redirect URLs from section 3.
5. **Authentication → Providers → Email:** decide whether email confirmation is required.
6. Update `SmartBuild/secrets.properties` with the new URL and anon key, then rebuild the APK.
7. Existing student accounts and progress stay in the old project unless migrated.

Transferring the **existing** project instead (keeps all accounts): Project Settings →
General → **Transfer project** to the client's organization, or invite the client as Owner of
the current organization.
