# Proposed Plan: Widen Sidebar

## Context
The user requested to widen the sidebar because the items inside are too long / text is cramped.
Current width is `w-96` (24rem / 384px).

## High-Level Design
*   Increase width class in `Sidebar.tsx`.
*   Suggest bumping from `w-96` to `w-[450px]` or `w-[500px]` to give more breathing room while fitting on most desktop screens.

## Step-by-Step Implementation
1.  **Sidebar.tsx**:
    *   Change `w-96` to `w-[450px]` in the root `Card` className.

## Verification Strategy
1.  Visual check if possible (user task).