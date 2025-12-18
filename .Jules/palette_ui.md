# Palette's UI Journal
## 2025-05-01 - Card-ifying List Items
**Insight:** The application uses flat `LinearLayout`s for list items (`item_session_entry.xml`, `item_log_entry.xml`). While functional, they lack visual separation and touch feedback. Converting these to `MaterialCardView` containers creates a cleaner, more "card-based" UI that aligns with Material Design 3 and provides better affordance for interaction (ripples, elevation).
