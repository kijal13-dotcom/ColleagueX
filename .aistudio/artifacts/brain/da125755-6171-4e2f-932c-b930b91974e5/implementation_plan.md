# Interactive Skill Endorsements & Peer Networking with Offline Persistence

A comprehensive implementation plan to expand the colorful skill pills in `UserProfile` into an interactive endorsement and peer networking system, complete with endorsement counters, verification notes, and offline-first Room database persistence.

---

### User Review & Critical Decisions

> [!IMPORTANT]
> The initial preference questions were dismissed without selection, so recommended defaults have been established below to maximize capability without disrupting existing user profiles:

- **Primary Focus**: Skill Endorsements & Peer Networking. Tapping any skill pill opens an interactive endorsement dialog showcasing peer endorsers, verification badges, and an "Endorse Skill" action button.
- **Interaction Model**: A polished Material 3 Modal Bottom Sheet and Dialog that displays skill mastery level, endorser avatars, and personal endorsement notes.
- **Persistence Strategy**: Dedicated Room database table (`SkillEndorsementEntity`) with foreign keys to user IDs and reactive Kotlin `Flow` queries for instant offline access and updates.

---

### 1. Overview & Core Concept

- **What It Does**: Transforms the static and clickable skill pills in `UserProfile` into an active peer validation network. Users can view who endorsed each skill, write short peer recommendations, and endorse colleagues with one tap, even when offline.
- **Target Audience / Persona**: Professionals, engineers, and product leaders seeking authentic verification of peers' technical and leadership capabilities within WorkCircle.
- **Key Value**: Builds authentic social proof and workplace trust. Offline Room caching guarantees seamless browsing and endorsement recording even without network connectivity.

---

### 2. User Experience & Visual Design

#### Key User Flows
1. **Browse Skills**: When viewing a user's profile, the professional skills appear as vibrant, themed pills categorized by domain (Engineering, Product, Design, Leadership, AI).
2. **Open Endorsement Sheet**: Tapping a skill pill triggers a subtle tactile animation and opens the **Skill Endorsement Bottom Sheet**.
3. **Endorse a Colleague**: A prominent "Endorse this Skill" button lets authenticated or local users add their endorsement, instantly incrementing the count and caching to Room.
4. **View Peer Endorsers**: Lists verified colleagues who have endorsed the skill, complete with their avatars, job titles, and optional peer commendations.
5. **Offline Indicator**: A subtle status banner confirms that endorsements are stored locally and will sync when online.

#### Visual Identity & Theme
- **Aesthetic Direction**: High-trust corporate editorial with playful, clean pastel accents (Material 3 professional styling).
- **Color Palette**:
  - Primary Surface: Clean white card (`#FFFFFF`) with subtle slate borders (`#E2E8F0`).
  - Domain Accents:
    - *Engineering*: Indigo (`#4338CA`) on Soft Blue (`#EEF2FF`).
    - *Product & Strategy*: Amber (`#B45309`) on Warm Sand (`#FEF3C7`).
    - *Leadership & Team*: Emerald (`#047857`) on Mint (`#ECFDF5`).
    - *AI & Intelligence*: Violet (`#6D28D9`) on Lavender (`#F5F3FF`).
- **Typography & Hierarchy**:
  - Skill Titles: Bold 14sp Sans with 0.2sp letter spacing.
  - Endorsement Counters: SemiBold 11sp pill badges (`#1E293B`).
  - Peer Quotes: Italic 12sp secondary slate (`#64748B`).

#### Interactive Feedback & Motion
- Spring animations on tag click (`animateFloatAsState`).
- Instant optimistic UI update on endorse action.
- Toast feedback confirming Room database persistence.

---

### 3. Key Product Decisions & Trade-Offs

- **Decision 1: Modal Bottom Sheet vs. Inline Accordion**
  - *Chosen Approach*: Modal Bottom Sheet (`ModalBottomSheet` in Jetpack Compose).
  - *Why*: Keeps the profile card compact and uncluttered while offering rich room for endorser avatars, notes, and action buttons on mobile screens.
  - *Alternatives Considered*: Inline expandable cards would push lower profile sections off-screen and clutter the scroll hierarchy.

- **Decision 2: Dedicated Room Entity vs. Comma-Separated User Column**
  - *Chosen Approach*: New `SkillEndorsementEntity` Room table with indices on `targetUserId` and `skillName`.
  - *Why*: Provides clean relational modeling, prevents string-parsing errors, and enables queries like "all skills endorsed for user X" or "top endorsed skills".
  - *Alternatives Considered*: Storing endorsement counts in JSON within `UserEntity` leads to migration fragility and lack of referential integrity.

---

### 4. Technical Architecture & Data Strategy

#### System & Component Diagram

```
┌──────────────────────────────────────────────────────────┐
│                   UserProfile / ProfileScreen            │
│  ┌────────────────────────────────────────────────────┐  │
│  │  SkillPill (Engineering, AI, Product, Leadership)  │  │
│  └────────────────────────┬───────────────────────────┘  │
└───────────────────────────┼──────────────────────────────┘
                            │ (onSkillClick)
                            ▼
┌──────────────────────────────────────────────────────────┐
│             SkillEndorsementBottomSheet                  │
│  ┌───────────────────┐    ┌───────────────────────────┐  │
│  │ Endorsement Count │    │ "Endorse Skill" CTA Button│  │
│  └───────────────────┘    └─────────────┬─────────────┘  │
│  ┌──────────────────────────────────────┼─────────────┐  │
│  │ Endorsers List (Avatars, Names, Verification Badge)│  │
│  └────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────┼────────────────┘
                                          │
                                          ▼
┌──────────────────────────────────────────────────────────┐
│                   WorkCircleViewModel                    │
│   - endorseSkill(targetUserId, skillName, note)          │
│   - observeSkillEndorsements(targetUserId, skillName)     │
└─────────────────────────────┬────────────────────────────┘
                              │
                              ▼
┌──────────────────────────────────────────────────────────┐
│            WorkCircleRepository & WorkCircleDao          │
│   - Flow<List<SkillEndorsementEntity>>                   │
│   - suspend fun insertEndorsement(endorsement)           │
└─────────────────────────────┬────────────────────────────┘
                              │
                              ▼
┌──────────────────────────────────────────────────────────┐
│                  Room SQLite Database                    │
│   - skill_endorsements table (offline persistent)        │
└──────────────────────────────────────────────────────────┘
```

#### Data Model & Entities
```kotlin
@Entity(
    tableName = "skill_endorsements",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["targetUserId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["targetUserId", "skillName"])]
)
data class SkillEndorsementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val targetUserId: Long,
    val endorserUserId: Long,
    val endorserName: String,
    val endorserRole: String,
    val endorserPhotoUrl: String = "",
    val isEndorserVerified: Boolean = false,
    val skillName: String,
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
```

#### Interactive Component & State Mapping
- `SkillPill`: Passes `skill` string to `onSkillClick`.
- `SkillEndorsementBottomSheet`: Renders state from `viewModel.getSkillEndorsements(userId, skill)`.
- `EndorseButton`: Checks whether current user has already endorsed; toggles state reactively in Room.
- `Dismiss / Close`: Smooth sheet dismissal with back handler support.
