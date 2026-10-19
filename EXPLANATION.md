# Attack Surface Mapper: Technical Explanation

## Overview
The Attack Surface Mapper is a security tool designed to detect "jailbreak" attempts in Large Language Model (LLM) conversations. Unlike traditional filters that look for single "bad words," this tool analyzes the **entire conversation history** to detect sophisticated attacks that unfold over multiple turns.

## Core Technology
The tool uses a graph-based approach where every message (turn) is a node, and the relationships between them (semantic similarity, sequence) are edges.

### 1. The Conversation Graph
*   **Nodes**: Represent individual user or assistant messages.
*   **Edges**:
    *   **Sequential**: Connects turn 1 -> turn 2 -> turn 3.
    *   **Semantic**: Connects turns that are topically similar, helping visualize how a topic evolves or persists.

### 2. Detection Modules
The tool runs four specific detection algorithms in parallel for every new message:

#### A. Persona Drift Detector
*   **Goal**: Detect when a user tries to trick the AI into adopting a role (e.g., "Act as a chemistry teacher") to bypass restrictions.
*   **Logic**: It looks for a "Role Assignment" intent followed by a dangerous request later in the chat. If the AI accepts the role and then receives a risky question, the score spikes.

#### B. Refusal Erosion Detector
*   **Goal**: Detect when a user refuses to take "no" for an answer and tries to wear the AI down.
*   **Logic**: It counts how many times the AI has refused. If the AI refuses 2+ times but eventually gives a "Compliance" response, this flags a critical failure (High Alert).

#### C. Topic Escalation Detector
*   **Goal**: Detect "Frog Boiling" - starting with innocent questions and slowly moving to dangerous ones.
*   **Logic**: It compares the risk score of the start of the conversation vs. the end. A sudden jump or a gradual significant rise triggers this alert.

#### D. Obfuscation Chain Detector
*   **Goal**: Detect "Split Attacks" where a user asks for ingredients of a weapon separately across different messages.
*   **Logic**: It remembers entities from *all* previous turns.
    *   Turn 1: "Tell me about **ammonia**." (Safe)
    *   Turn 2: "Tell me about **bleach**." (Safe)
    *   **Detector**: Notices {ammonia + bleach} = **Mustard Gas Precursor**. Flags the combination as high risk.

## Scoring System
The tool assigns a **Risk Score (0.0 - 1.0)** to the conversation:
*   **0.0 - 0.3 (Green)**: Safe. Normal conversation.
*   **0.3 - 0.7 (Yellow)**: Suspicious. Monitor closely.
*   **0.7 - 1.0 (Red)**: High Alert. Attack currently in progress or successful.

## Visualization
*   **Graph View**: Shows the structural relationship of the chat. Red nodes indicate where the attack happened.
*   **Risk Timeline**: A line chart showing the exact moment the conversation crossed the safety threshold.
