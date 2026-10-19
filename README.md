# Multi-Turn Attack Surface Mapper

A tool to detect sophisticated jailbreaks and attack patterns in AI conversations.

## Setup

1.  **Install Dependencies**:
    ```bash
    pip install -r requirements.txt
    python -m spacy download en_core_web_sm
    ```

2.  **Run the App**:
    ```bash
    streamlit run app.py
    ```

## Usage

*   **Conversation Input**: Type messages in the bottom left or use the sidebar to load examples.
*   **Graph Analysis**: View the conversation graph on the right. Nodes are color-coded by risk.
*   **Threat Monitor**: See real-time risk scores and specific attack pattern alerts.
*   **Export**: Export the analysis to JSON via the sidebar.

## Detection Modules

*   **Persona Drift**: Detects role-playing leading to harmful requests.
*   **Obfuscation Chain**: Detects split concepts (e.g., ingredients for a weapon).
*   **Refusal Erosion**: Detects persistence breaking down defenses.
*   **Topic Escalation**: Detects subtle shifts to sensitive topics.
