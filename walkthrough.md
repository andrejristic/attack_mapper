# Attack Surface Mapper - Walkthrough

## Overview
I have built a **Multi-Turn Attack Surface Mapper** that detects complex jailbreak attempts and accumulating risk in AI conversations.

## Features
1.  **Conversation Graph**: Tracks turns with semantic similarity edges.
2.  **Analysis Pipeline**:
    *   **Embeddings**: `sentence-transformers` (all-MiniLM-L6-v2)
    *   **Intent**: Semantic matching against pre-defined anchors (e.g., "role_assignment", "direct_request").
    *   **Entities**: Spacy-based extraction (PERSON, ORG, CHEMICALS via keywords).
    *   **Sensitivity**: Cosine similarity to dangerous topics (bioweapon, exploit).
3.  **Attack Detectors**:
    *   `detect_persona_drift`: Finds "roleplay" followed by "sensitive requests".
    *   `detect_refusal_erosion`: Finds patterns of repeated refusals leading to compliance.
    *   `detect_topic_escalation`: Detects sudden or gradual risk score increases.
    *   `detect_obfuscation_chain`: Finds combinations of dangerous keywords across turns (e.g., "ammonia" + "bleach").
4.  **Visualization**: Interactive Plotly graph and risk timeline.

## How to Run
1.  **Install Dependencies**:
    ```bash
    pip install -r requirements.txt
    python -m spacy download en_core_web_sm
    ```
2.  **Run Application**:
    ```bash
    streamlit run app.py
    ```
3.  **Test**:
    *   Click "Load ATTACK (Multi-Turn)" in the sidebar.
    *   Observe the "Threat Scores" panel turn RED (>0.7).
    *   See the Graph nodes color-coded by risk.

## Verification Results
*   **Benign Case**: Low risk, green nodes.
*   **Single-Turn Jailbreak**: Caught by sensitivity scorer, but simple structure.
*   **Multi-Turn Attack**: Flags "Persona Drift" and "Obfuscation Chain" (ammonia+bleach), resulting in HIGH RISK.
