# SmartBuild — Module Reference

Detailed reference for every module: what the student does, what counts as a pass, how Guided
and Assessment differ, and the **answer keys**. Intended for teachers, panelists and
developers. For the student-facing guide see [USER_MANUAL](./USER_MANUAL.md).

Common rules for all modules:

- **Guided Simulation** shows yellow highlights and "DO THIS" coaching.
- **Scenario Assessment** shows a scenario brief (Modules 2–4), hides all coaching, and often
  starts with planted faults. Grading rules are the same as Guided.
- Every correct or incorrect action shows a **Correct / Incorrect** dialog with an explanation.
- Progress is capped at 99% until the Assessment is passed (then 100%).
- Modules 1–4 share one flow (Godot `ModuleFlow`, SMARTBUILD Figma layout):
  **Lesson pages (swipe) → Guided Simulation → Be Ready for Assessment → Assessment Simulation
  → Completion.** There is no quiz: a module is passed only by finishing the Assessment
  Simulation. The module card's **Guided Simulation** button opens the lessons; **Scenario
  Assessment** opens "Be Ready for Assessment".

---

## Module 0 — Introduction to Computer Systems Servicing

| | |
|---|---|
| Screen | Compose `ModulePage`; slide visuals from the Godot support content (`SmartBuild-Godot/modules/module_0/`) |
| Modes | Lesson only (no Assessment). Home shows **Continue Lesson** / **Review Lesson** |
| Length | 21 slides |
| Pass | Reach slide 21 and tap **Proceed to Home** → 100% |

| Slides | Phase | Topics |
|---|---|---|
| 1–6 | Career path | What CSS NC II is, career paths (hardware, networking, servers, support) |
| 7–10 | Safety & quality | ESD, workplace safety, tools, quality practices |
| 11–20 | Hardware basics | Computer types, components, memory vs storage, peripherals, networking basics |
| 21 | Complete | Achievement checklist, progress bar to 100%, Proceed to Home / Retake |

Controls: **Back** (top left), **Help** (context help for the current slide), **swipe** left / right
(or mouse-drag on desktop) to change slides, and a page counter at the bottom. Swipes are ignored
when they start on buttons, 3D viewers or scrolling lists.

---

## Module 1 — Installing and Configuring Computer Systems

| | |
|---|---|
| Screen | Compose `ModulePage` → Godot `ModuleFlow` (`core/components/lesson/`); 3D bench in `SimulationPage` (`core/simulation/`) |
| Content source | Lessons: `SmartBuild-Godot/scripts/content/module_1_content.gd`; simulations: `scripts/module_content_registry.gd` |
| Guided | Lesson pages 1–13 (swipe), then the Guided Simulation (pages 14–15) with yellow hints |
| Assessment | Opens on page 16 (Be Ready), then the 25-step Assessment Simulation with no hints |
| Pass (Guided) | Finish the Guided Simulation (disassembly + assembly) → Guided saved, ≥ 50% |
| Pass (Assessment) | Finish the Assessment Simulation → 100%; leave from the Congratulations page |

### Pages

| # | Page | What the student does |
|---|---|---|
| 1 | Ready to Assemble and Disassemble a Computer? | Reads the intro |
| 2 | Tools and Equipment | Screwdrivers, anti-static wrist strap, clean workspace |
| 3–4 | ESD-Safe Preparation Steps | Six step cards with illustrations |
| 5 | Before You Start, Follow These Safety Precautions! | Taps each precaution (not graded) |
| 6–7 | Input and Output Device Setup | Eight step cards |
| 8–9 | BIOS Configuration Steps | Eight step cards |
| 10–11 | Operating System Install Steps | Eight step cards |
| 12–13 | Drivers and System Testing Steps | Eight step cards |
| 14 | Guided Simulation · Disassemble | Tears down a complete PC on the 3D bench (11 steps) |
| 15 | Guided Simulation · Assemble | Builds the PC on the 3D bench (14 steps) → Guided saved |
| 16 | Be Ready for Assessment! | Reads, then swipes to begin |
| 17 | Assessment Simulation | Disassemble then rebuild (25 steps), no hints → Assessment saved |
| 18 | Congratulations! | **Proceed to Home** / **Retake Module** |

Lesson pages swipe left/right. Simulations are one-way: swiping unlocks only after the
simulation is finished (or use the Continue button on the "done" dialog).

### How the 3D bench works

- **Remove** a part: tap it inside the case.
- The 3D bench is **full screen**. Small floating buttons at the top right open the
  **Steps** (current instruction, feedback, assessment reset), **Parts** (the tray) and
  **Info** (objectives, pitfalls) panels; all start hidden and slide in over the 3D view.
  Opening or closing a panel never changes the current step. **Back** and **Help** sit at the
  top left; **Zoom + / −** at the top centre.
- **Install** a part: open **Parts** and drag the part onto its bay in the case (the tray
  slides away as soon as the drag starts).
- Rotate the view with one finger; zoom with **Zoom + / −** or pinch.
- A wrong tap or drop is refused, and the Incorrect dialog explains what was expected and why.
- When the **motherboard** is removed, it comes out of the case with the RAM, CPU fan and CPU
  still attached; the student removes those next from the board in front of the case.
- The case side panel is hidden, so "remove / close the side panel" steps complete
  automatically.

### Answer key — Phase 02 disassembly (TESDA order)

| # | Remove | Tap in the case |
|---|---|---|
| 1 | Side panel | (automatic) |
| 2 | 24-pin ATX power | 24-pin connector on the board |
| 3 | 4-pin / EPS 12V CPU power | CPU power connector |
| 4 | SATA power | SATA power on the drives |
| 5 | Power supply | PSU bay |
| 6 | Optical drive | Optical bay |
| 7 | Hard disk (HDD) | Storage bay |
| 8 | Motherboard | Board (comes out with RAM, CPU fan, CPU) |
| 9 | RAM | DIMM slot on the pulled board |
| 10 | CPU fan | Cooler on the pulled board |
| 11 | CPU | CPU socket on the pulled board |

### Answer key — Phase 03 assembly

| # | Install | Onto |
|---|---|---|
| 1 | PSU | PSU bay |
| 2 | Motherboard | Board mount |
| 3 | CPU | CPU socket |
| 4 | CPU Fan | Cooler mount (over the CPU) |
| 5 | Fan Cable | CPU fan header |
| 6 | RAM | DIMM slot |
| 7 | HDD | Storage bay |
| 8 | Optical Drive | Optical bay |
| 9 | GPU | PCIe x16 slot |
| 10 | 24-pin ATX | ATX socket |
| 11 | CPU Power | EPS / CPU power socket |
| 12 | SATA Power | Drive power port |
| 13 | Front Panel | Front-panel header |
| 14 | Case Cover | (automatic) |

The Assessment capstone is Phase 02 followed by Phase 03 (25 steps) with no hints.

### Lesson step order (no quiz)

The former multiple-choice phases (ESD preparation, BIOS, OS install, drivers and testing) are
now lesson pages of step cards in the order below. They are read, not graded.

| Topic | Steps, in order |
|---|---|
| ESD-Safe Preparation | Wear anti-static wrist strap → Lay out the anti-static mat → Stage screwdriver and fasteners → Inspect delivered components → Confirm compatibility → Organize the workspace |
| BIOS Configuration | Connect display and input → Power on the system → Enter BIOS/UEFI setup → Verify CPU detection → Verify RAM detection → Verify storage detection → Set boot priority → Save and exit |
| OS Installation | Insert bootable USB → Boot from the USB → Choose install options → Prepare partitions → Format the target volume → Run OS installation → Create user account → Finish first-run setup |
| Drivers and Testing | Install motherboard drivers → Install graphics drivers → Install network drivers → Install baseline applications → Test the display → Test internet access → Test storage access → Document the build |

---

## Module 2 — Setting Up Computer Networks

| | |
|---|---|
| Lessons | Godot `ModuleFlow` — 10 Figma pages (`scripts/content/module_2_content.gd`) |
| Simulation | Compose (`screens/networklab/`, `screens/crimplab/`), opened from the flow |
| Flow | Lessons (swipe) → **Start Guided Simulation** → Compose lab → Be Ready (Godot) → **Start Assessment** → Compose lab → Congratulations (Godot) |
| Stations | 1 Cable intro → 2 Crimp lab → 3 Network topology |
| Pass | Finish all three stations → Guided / Assessment complete |

### Station 1 — Cable intro

Three cards: straight-through (T568B–T568B), crossover (T568A–T568B), and how to tell them
apart. **Continue to crimp** advances.

### Station 2 — Crimp lab (straight-through cable)

Steps: select cable → strip → untwist → order pins (End A) → insert → crimp → order pins
(End B) → insert → crimp → LED test.

| Action | How |
|---|---|
| Select | Drag the Cat 6 coil onto the mat |
| Strip | Drag the stripper along the jacket (~25 mm) |
| Untwist | Drag to untwist all four pairs |
| Order pins | Drag each colour to pins 1–8. A wrong pin is flagged immediately |
| Insert / crimp | Drag the wires into the plug, then use the crimper |
| Test | Run the LED tester — LEDs 1–8 light in order |

**Answer key — T568B (both ends):**

| Pin | 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 |
|---|---|---|---|---|---|---|---|---|
| Wire | White-Orange | Orange | White-Green | Blue | White-Blue | Green | White-Brown | Brown |

(T568A, used only for crossover End A: White-Green, Green, White-Orange, Blue, White-Blue,
Orange, White-Brown, Brown.)

### Station 3 — Network topology

| Phase | Student action | Answer |
|---|---|---|
| Place | Place devices in order, then **Lock placement** | Modem → Router → Switch → Access Point → PC |
| Cable | Add the required links, then **Lock cabling** | Modem–Router, Router–Switch, Switch–AP, Switch–PC |
| Verify | **Verify path** | Passes when placement and links are correct |

### Guided vs Assessment

| | Guided | Assessment |
|---|---|---|
| Scenario brief | — | "Broken small-office link": crossover used where straight-through is needed; incomplete topology |
| Crimp | Coach, colour key, ghost pin colours, correct wire revealed after a miss; mode chips | Forced straight-through, no colour key or reveal |
| Topology | Clean start, coach | Starts faulted (PC, Modem, Switch pre-placed in the wrong order) |

---

## Module 3 — Setting Up Computer Servers

| | |
|---|---|
| Lessons | Godot `ModuleFlow` — 14 Figma pages (`scripts/content/module_3_content.gd`) |
| Simulation | Compose (`screens/serverlab/`, `screens/packettracer/`), opened from the flow |
| Flow | Lessons (swipe) → **Start Guided Simulation** → Compose lab → Be Ready (Godot) → **Start Assessment** → Compose lab → Congratulations (Godot) |
| Stations | 1 Client–server topology → 2 File server setup |
| Pass | Finish both stations |

### Station 1 — Client–server topology (Packet Tracer style)

| Phase | Student action | Answer |
|---|---|---|
| Place | Drag Switch, Server and PC onto the workspace | All three placed |
| Cable | Choose a cable tool and connect devices | **Copper Straight-Through** for PC–Switch and Server–Switch (both links green). Crossover or PC–Server direct links are wrong |
| IP | Tap each device → IP Configuration | PC0 `192.168.10.20` / `255.255.255.0`; Server0 `192.168.10.10` / `255.255.255.0` |
| Test | PC0 → Command Prompt | `ping 192.168.10.10` → replies → **Next: file server** |

The CMD also accepts `ipconfig`. Unknown commands show the Windows "not recognized" message.

### Station 2 — File server setup

Server desktop icons / Start menu: Server Manager, File Explorer, Computer Management,
Folder Security, Advanced Sharing. **Switch to PC** toggles to the client desktop.

| # | Checklist item | Answer |
|---|---|---|
| 1 | Install role | Server Manager → Next → tick **File Server only** (not DNS/DHCP) → Install |
| 2 | Create folders | File Explorer → New folder → `HR`, then `IT` |
| 3 | Create groups | Computer Management → `HR_Users`, `IT_Users` |
| 4 | NTFS least privilege | Folder Security → HR folder: **HR_Users**; IT folder: **IT_Users** |
| 5 | Share | Advanced Sharing → Share DeptShares; Change for HR_Users and IT_Users → Apply |
| 6 | Allow test | Switch to PC → File Explorer → `\\FileServer\DeptShares\HR` → Go → access granted |
| 7 | Deny test | `\\FileServer\DeptShares\IT` → Go → access denied |

After all seven, **Finish module** appears.

### Guided vs Assessment

| | Guided | Assessment |
|---|---|---|
| Scenario brief | — | "Broken client–server + over-permission" |
| Topology | Clean start | PC0 and Server0 pre-placed, joined by a **crossover** cable with no switch — student must add a switch and use straight-through |
| File server | Clean start | Everything configured **except** the IT folder is granted to HR_Users (over-permission). Fix Folder Security, then redo the allow and deny tests |
| Coaching | DO THIS line, highlighted icons | None |

---

## Module 4 — Maintaining Computer Systems and Networks

| | |
|---|---|
| Lessons | Godot `ModuleFlow` — 21 Figma pages (`scripts/content/module_4_content.gd`) |
| Simulation | Compose (`screens/maintenancelab/MaintenanceLabScreen.kt`), opened from the flow |
| Flow | Lessons (swipe) → **Start Guided Simulation** → Compose lab → Be Ready (Godot) → **Start Assessment** → Compose lab → Congratulations (Godot) |
| Setting | Full-screen Windows desktop service visit |
| Pass | Complete the service order and close the ticket → **Finish module** |

### Service order (answer key)

| # | Task | How |
|---|---|---|
| 1 | Accept the ticket | Open **Ticket #SB-4401** on the desktop → Accept |
| 2 | Set wallpaper | Start → Pictures → drag a wallpaper onto the desktop |
| 3 | Remove junk | Drag **Temp** and **OldReports** into the Recycle Bin |
| 4 | Disk Cleanup | Start → Disk Cleanup → Run Disk Cleanup |
| 5 | Test the network | Start → CMD → type `ping 192.168.1.1` (gateway), then `ping 8.8.8.8` |
| 6 | Close the ticket | Action Center → Close ticket (only accepted once the other tasks are done) |

### Guided vs Assessment

| | Guided | Assessment |
|---|---|---|
| Scenario brief | — | "Ticket #SB-4401 — slow PC, no internet" |
| Start | Clean desktop | Ticket already accepted, Action Center open with warnings |
| Coaching | Floating DO THIS guide | Hidden |

---

## Summary table

| Module | Screen | Guided stations / pages | Assessment start state |
|---|---|---|---|
| 0 | Compose `ModulePage` (Godot-rendered slides) | 21 slides | — (lesson only) |
| 1 | Compose `ModulePage` (Godot-rendered 3D bench) | 12 pages (6 phases) | Complete PC; disassemble then rebuild (25 steps) |
| 2 | Compose lab | Cable intro, crimp, topology | Forced straight-through; topology misordered |
| 3 | Compose lab | Topology, file server | Crossover PC–Server, no switch; IT folder open to HR |
| 4 | Compose lab | Windows service ticket | Ticket open, warnings in Action Center |
