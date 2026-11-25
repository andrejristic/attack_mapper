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

## Android app (APK)

The same analysis runs as an Android app (phones and Chromebooks), fully on the device:
no internet permission, nothing leaves the device.

- **Download:** latest APK under [Releases](https://github.com/andrejristic/attack_mapper/releases)
  (all apps: [andrejristic/APK](https://github.com/andrejristic/APK))
- **Code:** [`android/`](android/) (Kotlin + Jetpack Compose). The detectors are a line-for-line
  port of `app.py`; embeddings come from the same `all-MiniLM-L6-v2` model (ONNX, float16-stored weights, fp32 compute).
- **Parity:** CI runs the original `app.py` analysis code and fails if the Android port
  disagrees on any intent, detector score or alert level ([`tools/reference/`](tools/reference/)).
- **Extras:** share a conversation from any app (or select text → *Attack Mapper*), paste a
  transcript, local history, JSON export, Serbian / English switch.
- **Plan & guide:** [ANDROID-PLAN.md](ANDROID-PLAN.md), [VODIC-SR.md](VODIC-SR.md)

## License

GPL-3.0-or-later, see [LICENSE](LICENSE) and [NOTICE](NOTICE).
