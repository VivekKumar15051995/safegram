---
name: SafeGram Enterprise EHS
colors:
  surface: '#f8f9ff'
  surface-dim: '#cbdbf5'
  surface-bright: '#f8f9ff'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#eff4ff'
  surface-container: '#e5eeff'
  surface-container-high: '#dce9ff'
  surface-container-highest: '#d3e4fe'
  on-surface: '#0b1c30'
  on-surface-variant: '#3e4942'
  inverse-surface: '#213145'
  inverse-on-surface: '#eaf1ff'
  outline: '#6e7a72'
  outline-variant: '#bec9c0'
  surface-tint: '#006c48'
  primary: '#005f3f'
  on-primary: '#ffffff'
  primary-container: '#0d7a53'
  on-primary-container: '#a6ffd0'
  inverse-primary: '#7cd9aa'
  secondary: '#565e74'
  on-secondary: '#ffffff'
  secondary-container: '#dae2fd'
  on-secondary-container: '#5c647a'
  tertiary: '#764900'
  on-tertiary: '#ffffff'
  tertiary-container: '#965f00'
  on-tertiary-container: '#ffe9d4'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#98f6c5'
  primary-fixed-dim: '#7cd9aa'
  on-primary-fixed: '#002113'
  on-primary-fixed-variant: '#005236'
  secondary-fixed: '#dae2fd'
  secondary-fixed-dim: '#bec6e0'
  on-secondary-fixed: '#131b2e'
  on-secondary-fixed-variant: '#3f465c'
  tertiary-fixed: '#ffddb8'
  tertiary-fixed-dim: '#ffb95f'
  on-tertiary-fixed: '#2a1700'
  on-tertiary-fixed-variant: '#653e00'
  background: '#f8f9ff'
  on-background: '#0b1c30'
  surface-variant: '#d3e4fe'
typography:
  display:
    fontFamily: Inter
    fontSize: 32px
    fontWeight: '800'
    lineHeight: 40px
    letterSpacing: -0.02em
  headline-lg:
    fontFamily: Inter
    fontSize: 26px
    fontWeight: '700'
    lineHeight: 32px
    letterSpacing: -0.015em
  headline-lg-mobile:
    fontFamily: Inter
    fontSize: 22px
    fontWeight: '700'
    lineHeight: 28px
    letterSpacing: -0.01em
  headline-md:
    fontFamily: Inter
    fontSize: 18px
    fontWeight: '700'
    lineHeight: 24px
    letterSpacing: -0.01em
  body-lg:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '500'
    lineHeight: 24px
  body-md:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 20px
  label-lg:
    fontFamily: Inter
    fontSize: 13px
    fontWeight: '700'
    lineHeight: 16px
    letterSpacing: 0.02em
  label-md:
    fontFamily: Inter
    fontSize: 12px
    fontWeight: '600'
    lineHeight: 16px
    letterSpacing: 0.01em
  label-sm:
    fontFamily: Inter
    fontSize: 10px
    fontWeight: '800'
    lineHeight: 12px
    letterSpacing: 0.06em
rounded:
  sm: 0.125rem
  DEFAULT: 0.25rem
  md: 0.375rem
  lg: 0.5rem
  xl: 0.75rem
  full: 9999px
spacing:
  gutter: 1rem
  gutter-sm: 0.75rem
  margin: 1rem
  margin-tablet: 1.5rem
  space-xs: 0.25rem
  space-sm: 0.5rem
  space-md: 0.75rem
  space-lg: 1rem
  space-xl: 1.5rem
  space-2xl: 2rem
---

## Brand & Style

This design system is engineered for industrial field professionals, safety officers, and operational plant managers operating in demanding, dynamic, and safety-critical environments. The tone is uncompromisingly authoritative, ultra-reliable, and pragmatic. It projects institutional vigilance while eliminating cognitive friction in high-stress reporting situations.

The aesthetic blends **Modern Industrial Utility** with **Crisp High-Contrast Functionalism**. The UI eliminates superficial decorative flourishes in favor of immediate visual legibility, tactile feedback, and instantaneous classification of hazards (Near Miss, Unsafe Act/Condition, Audits, and CAPA). The interface commands confidence under harsh field lighting, supports rapid single-handed thumb interactions, and uses disciplined color coding to communicate status and severity without ambiguity.

## Colors

The palette balances operational safety greens with standard compliance alert signals and an industrial slate scaffold:

- **Primary (`#0D7A53` - Safety Forest Emerald):** Anchors primary actions, verified compliance states, system approvals, and primary navigation headers. A high-luminance accent (`#10B981`) is reserved for positive trends and success confirmation badges.
- **Secondary (`#0F172A` - Industrial Dark Slate):** Drives foundational typography, structural high-contrast chrome, app bars, and deep-tonal status modules.
- **Tertiary (`#F59E0B` - Alert Amber):** Communicates moderate risks, pending audits, open observations, and warnings requiring attention.
- **Danger (`#EF4444` - Severe Hazard Red):** Strictly reserved for immediate stop-work conditions, critical severity incidents, overdue CAPA actions, and emergency disclosures.
- **Surface & Backgrounds (`#F8FAFC`, `#FFFFFF`, `#F1F5F9`):** Pure and off-white layered backdrops designed for glare reduction, outdoor legibility, and high structural contrast against text and alert borders.

## Typography

This design system uses **Inter** across all UI tiers for maximum optical clarity, tall x-height, and distinction between ambiguous glyphs (e.g., `0` vs `O`, `1` vs `l`) in technical identifiers and equipment tag codes.

- **Display & Headlines:** Used for quick situational scans, incident metric counters, and module titles. Dense line-heights keep mobile viewports information-efficient.
- **Body Styles:** Set in medium and regular weights with generous line-heights to support fatigue-free reading of hazard narratives, audit checklist steps, and CAPA resolution logs.
- **Labels & Badges:** Use condensed tracking, medium-to-bold weights, and uppercase transformations where appropriate for severity indicators (e.g., `HIGH SEVERITY`, `CLOSED CAPA`) to command instant visual prioritization.

## Layout & Spacing

The layout is built on an adaptive fluid grid model optimized for one-handed mobile ergonomics and tablet inspection clipboards:

- **Mobile Viewports (<600px):** Single-column stack with `1rem` (16px) standard horizontal margins, a 48px minimum touch target matrix, and primary actions pinned within the bottom thumb-reach zone.
- **Tablet / Industrial Rugged Displays (600px–1024px):** 2-column or dual-pane inspection views (checklist on the left, media/action logging on the right) with `1.5rem` margins and `1rem` gutters.
- **Vertical Rhythm:** Strict 4px/8px micro-spacing baseline. Compact spacing tokens (`space-sm`, `space-md`) consolidate inspection questions, while macro tokens (`space-xl`, `space-2xl`) separate multi-step incident escalation workflows.

## Elevation & Depth

Visual hierarchy uses **structured structural containment and crisp boundaries** rather than deep blurred shadows, preventing visual muddiness on low-tier mobile panels under direct sunlight:

- **Level 0 (Base Canvas):** Background surface `#F8FAFC`.
- **Level 1 (Cards, Form Groups, Tile Modules):** Surface `#FFFFFF` enclosed by a crisp, high-precision border (`1px solid #E2E8F0`). Flat or paired with an ultra-subtle ambient shadow: `0 1px 2px 0 rgba(15, 23, 42, 0.05)`.
- **Level 2 (Active Inspections, Open Incidents, Floating Sheets):** `#FFFFFF` with a directional micro-shadow: `0 4px 6px -1px rgba(15, 23, 42, 0.08), 0 2px 4px -2px rgba(15, 23, 42, 0.04)`.
- **Level 3 (Modal Overlays, Bottom Emergency Action Sheets):** `#FFFFFF` elevated with `0 20px 25px -5px rgba(15, 23, 42, 0.12)` accompanied by a 40% `#0F172A` dimming backdrop scrim.
- **Severity Accent Trim:** Critical hazard modules use an inset 4px solid left-border (in Red `#EF4444`, Amber `#F59E0B`, or Emerald `#0D7A53`) to establish hierarchy without vertical bulk.

## Shapes

The shape scale is set to **Level 1 (Soft)** with standard 4px (0.25rem) to 8px (0.5rem) radii. This creates an engineered, utilitarian appearance suited to technical documentation and regulatory enterprise software. 

Cards, text inputs, action buttons, and hazard dialogue panels use tight 4px to 8px corners to preserve screen real estate and maximize internal data density. Pill shapes are strictly forbidden for actionable buttons and are reserved exclusively for compact, non-interactive status tags and numeric risk score capsules.

## Components

### Buttons
- **Primary Action (Report Hazard / Submit CAPA):** Solid `#0D7A53` background with white text, 8px corner radius, bold label, and 48px standard touch height. Active state uses `#095C3E`.
- **Critical / Stop-Work Action:** Solid `#EF4444` background with crisp white typography.
- **Secondary / Actionable Neutral:** `#FFFFFF` background, 1.5px border in `#CBD5E1`, text in `#0F172A`.
- **Touch Ergonomics:** Always fill container width on mobile checkout and confirmation dialogs.

### Chips & Badges
- **Severity Badges:** High-contrast pill containers featuring a colored indicator dot (6px) alongside bold text.
  - *Critical / Danger:* `#FEF2F2` background, `#991B1B` text, `#EF4444` indicator.
  - *Warning / Medium:* `#FFFBEB` background, `#92400E` text, `#F59E0B` indicator.
  - *Safe / Resolved:* `#ECFDF5` background, `#065F46` text, `#10B981` indicator.
- **Filter Chips:** 32px height, 6px border radius, `#F1F5F9` background with `#475569` text; swaps to `#0D7A53` with pure white text when active.

### Cards & Incident Tiles
- **Structure:** Crisp white background `#FFFFFF`, 8px radius, `1px solid #E2E8F0`.
- **Left Accent Band:** Incidents display a 4px solid left border denoting category severity (e.g., Unsafe Condition vs Near Miss).
- **Header Structure:** Top row splits incident identification code (e.g., `NM-2024-089`) and timestamp, followed by an actionable hazard title and field location breadcrumb.

### Input Fields & Selectors
- **Form Controls:** 44px min-height, 6px radius, neutral border `#CBD5E1`, filled `#FFFFFF`.
- **Focused State:** 2px high-visibility stroke `#0D7A53` with zero blur halo.
- **Industrial Features:** Integrated quick-tap camera capture row for immediate photo/video hazard evidence, and oversized dropdown selectors for PPE compliance checklists.

### Checkboxes & Radio Buttons
- **Touch Boundary:** 24x24px visible box centered within a 48x48px interactive touch area.
- **State Styling:** Checked state fills with `#0D7A53` and a 2px white checkmark; validation error states flash `#EF4444`.

### EHS-Specific Modules
- **Risk Matrix Selector (5x5):** Grid cells with immediate contrast state updates and explicit numeric hazard severity indexing.
- **CAPA Milestone Tracker:** Stepper component using filled safety-green circles for completed phases, bold amber for in-progress remediations, and muted slate for future milestones.