# SmartBuild — User Manual

For **students** and **teachers**. SmartBuild is a landscape Android app for practising
Computer Systems Servicing (CSS NC II) through lessons and hands-on simulations.

---

## 1. Requirements

- Android phone or tablet, **Android 7.0 or newer**, ARM processor (almost all real devices).
- About **400 MB free storage** for installing (the app file is about 146 MB).
- **Internet connection** at all times. When the connection drops, a "connection lost" window
  covers the app until it returns.
- The app runs in **landscape** (turn the phone sideways).

---

## 2. Install

1. Get the `SmartBuild` APK file from your teacher (or install from Google Play once
   published).
2. Open the file. If Android asks, allow **Install unknown apps** for your file manager or
   browser.
3. Tap **Install**, then **Open**.

If you already have an older SmartBuild installed and Android says "App not installed", uninstall
the old one first. Your progress is saved online and comes back after you sign in.

---

## 3. Accounts

### Sign up

1. On the Log In screen tap **Sign Up**.
2. Enter your email and a password of **at least 8 characters**, then tap **REGISTER**.
   (**Go Back** returns to Log In.)
3. If you are asked to check your email, open the confirmation email **on the same phone** and
   tap the link. The app opens and you can log in.

### Log in

Enter your email and password, then tap **LOG IN**. If you see "email not confirmed", tap the
link in the confirmation email first.

### Forgot password

1. On the Log In screen tap **Forgot Password?**, enter your email and tap **SUBMIT**.
2. Open the reset email **on the same phone** and tap the link.
3. The app opens **Reset your password**. Enter the new password, tap **SUBMIT**, then log in
   with it.

Reset links expire; if the form says the session expired, request a new email.

### Profile

Tap the profile icon at the top right of Home:

- **Sign Out** — returns to the Log In screen. Your progress stays saved online.
- **Delete Account** — permanently deletes your account and your progress (asks for
  confirmation).
- **Change Password** — not available yet; sign out and use **Forgot Password?** instead.

---

## 4. Home

- **Module cards** — swipe left and right. Each card shows the module and your progress.
  Tap a card to open it.
- **Search** (top bar) — opens **Component Search**.
- **Help** (top bar) — a short "how to use" guide.
- **Profile** (top bar) — see section 3.
- The first time Home opens after launching the app, it shows **"Preparing simulations…"**
  while the 3D engine loads. Wait until it says the simulation is ready.

### Opening a module

Tap a card to expand it. You will see a progress ring and two buttons:

| Button | What it is |
|---|---|
| **Guided Simulation** | Practice with step-by-step instructions, highlights and tips |
| **Scenario Assessment** | The test. A short scenario first, often a problem already planted, and **no hints**. Unlocks after you finish the Guided Simulation |

Module 0 is a lesson, so it shows **Continue Lesson** / **Review Lesson** instead.

---

## 5. Progress

| Event | Progress |
|---|---|
| Moving through a module | Increases as you go, up to **99%** |
| Finishing the Guided Simulation | At least **50%**, and the Scenario Assessment unlocks |
| Finishing the Scenario Assessment | **100%** |
| **Retake** (on a module's last page) | Resets that module to 0% so you can start again |

Progress never goes down on its own (except Retake). It is saved on the phone and online, so
signing in on another phone shows the same progress.

---

## 6. The modules

### Module 0 — Introduction to Computer Systems Servicing

A 21-slide lesson: career path, safety and quality, and hardware basics.

- **Next** / **Prev** move between slides; **Help** explains the current slide.
- The last slide shows your completion. Tap **Proceed to Home** to finish (100%).
- **Back** before the end saves how far you got.

### Module 1 — Installing and Configuring Computer Systems

A 3D computer on a workbench, plus quiz phases.

| Phase | What you do |
|---|---|
| 01 ESD-Safe Preparation | Answer multiple-choice questions |
| 02 Safe Disassembly | Take apart a complete PC in the correct order |
| 03 Hardware Assembly | Build the PC from the parts tray |
| 04–06 BIOS, OS installation, drivers | Answer multiple-choice questions |
| Capstone (Assessment) | Disassemble, then rebuild, with no hints |

3D controls:

- **Drag with one finger** on empty space to rotate the view; **pinch** or the **zoom
  buttons** to zoom.
- **Install:** drag a part from the tray onto the correct bay (the yellow highlight shows where
  in Guided mode).
- **Remove:** tap a part that is installed.
- When you remove the motherboard, it comes out **with the RAM, CPU fan and CPU still on it**.
  Remove those three next.
- A wrong move shows a window explaining what you did, what was expected, and why.
- **Next** unlocks when the current phase is finished.

### Module 2 — Setting Up Computer Networks

Three stations, in order:

1. **Cable intro** — straight-through vs crossover cables.
2. **Crimp lab** — strip the cable, arrange the 8 wires (T568B), crimp, then test with the LED
   tester.
3. **Network topology** — place the devices and connect them with cables, then validate.

Use **Back** at the top left (or the phone's back gesture) to return to the previous station
or leave the module. **Reset** starts the current station over.

### Module 3 — Setting Up Computer Servers

1. **Client–server topology** — connect the PC and server with the correct cable and set IP
   addresses, then test with `ping`.
2. **File server setup** — on the simulated server desktop, add the file server role, create
   folders and groups, set folder permissions and share the folder; then check access from the
   PC's File Explorer.

### Module 4 — Maintaining Computer Systems and Networks

A simulated Windows desktop service visit:

1. Open the service ticket and **Accept** it.
2. Set the wallpaper.
3. Move junk folders to the **Recycle Bin**.
4. Run **Disk Cleanup**.
5. In **CMD**, type `ping 192.168.1.1`, then `ping 8.8.8.8`.
6. Open the **Action Center** and close the ticket.

---

## 7. Component Search

An encyclopedia of 40 CSS parts and tools in 8 categories (core hardware, tools and ESD,
network devices, cabling, servers and storage, internet and cloud, peripherals, maintenance).

- Type in the search box or pick a category.
- Tap a part to see its overview, how it works, CSS use, common issues and technician tips.
- **Pinch** or **double-tap** the picture to zoom.
- Works without downloading anything extra.

---

## 8. For teachers

- **Suggested order:** Module 0 → 1 → 2 → 3 → 4; in each module do the Guided Simulation
  before the Scenario Assessment.
- **Checking progress:** each student's Home shows their percentages. Progress is also stored
  in the school's Supabase project (table `module_progress`), which the system administrator
  can view or export — see [SUPABASE](./SUPABASE.md).
- **Answer keys** for every module: [MODULE_REFERENCE](./MODULE_REFERENCE.md).
- **Shared devices:** have each student sign out when done. Progress is kept per account, so
  the next student never sees another student's progress.

---

## 9. FAQ

**The app says "connection lost" and I can't do anything.**
Connect to Wi-Fi or mobile data. The window closes by itself when the internet returns.

**The Scenario Assessment button does nothing.**
Finish the Guided Simulation first.

**My progress is stuck at 99%.**
99% is the maximum before the Scenario Assessment. Finish the Assessment to reach 100%.

**I left a lab halfway and it started over.**
Labs do not save your place inside a station. Finish a module in one sitting when you can.

**A module shows an error with "Try again".**
Tap **Try again**. If it keeps happening, close the app completely and open it again.

**I didn't receive the confirmation or reset email.**
Check the spam folder and wait a few minutes. Ask your teacher if it still doesn't arrive.

**Can I use the app on a computer?**
Not directly. It needs an Android device with an ARM processor.

More technical problems: [TROUBLESHOOTING](./TROUBLESHOOTING.md).
