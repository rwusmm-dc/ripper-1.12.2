# Havook AI Bot System

## Overview

Fully agentic AI bot system for Minecraft 1.12.2. The AI makes ALL decisions - no hardcoded responses or triggers. Every action is decided by an LLM based on current game state, personality, and memory.

## Architecture

```
GameStateCollector → SystemPromptBuilder → LLMConnector → ResponseParser → Execute
      ↓                                                          ↑
   Game State                                              Validate Action
      ↓
 ReflexLayer (immediate reactions, no AI needed)
```

## Key Features

### 1. **Agentic Decision Making**
- AI decides everything: what to say, when to act, when to stay silent
- Personality system (Friendly, Neutral, Cautious, Aggressive, Silent, Sarcastic)
- Dynamic objective setting based on context

### 2. **Low-Latency Reflex Layer**
- Sub-100ms immediate reactions to critical situations:
  - Health < 4: FLEE immediately
  - Being attacked: ENABLE_KILL_AURA
  - Multiple armored players: RUN AWAY
  - Hunger < 4: ENABLE_AUTO_EAT

### 3. **Player Memory System**
- Tracks reputation: HOSTILE, SUSPICIOUS, NEUTRAL, FRIENDLY, TRUSTED
- Records player interactions and betrayals
- Never forgets who betrayed you
- Updates reputation based on behavior

### 4. **Context Management**
- Sliding window: keeps last 30 chat messages
- Summarization: old messages summarized to prevent token explosion
- Game state: health, hunger, position, nearby players, inventory
- Recent actions: last 5 actions logged

### 5. **Action Validation**
- Validates AI responses before execution
- Prevents invalid actions (FOLLOW non-existent players, GO_TO without coords)
- Gives feedback to AI when validation fails

### 6. **Cooldown System**
- Chat cooldown: 2000ms (don't spam chat)
- Action cooldown: 500ms (prevent action spam)
- AI cooldown: 3000ms (prevent rapid LLM calls)

### 7. **Debug Logging**
- Monologue logging: internal AI reasoning (never shown to players)
- JSONL format for easy parsing
- Timestamped entries with state, action, reasoning

## Setup & Configuration

### Step 1: Install Dependencies

Add GSON to your `build.gradle`:

```gradle
dependencies {
    minecraft 'net.minecraftforge:forge:1.12.2-14.23.5.2854'
    implementation 'com.google.code.gson:gson:2.10.1'
}
```

### Step 2: Add API Key

Choose your AI provider and get an API key:
- **Gemini**: https://ai.google.dev/
- **ChatGPT**: https://platform.openai.com/
- **Claude**: https://console.anthropic.com/
- **OpenRouter**: https://openrouter.ai/

### Step 3: Enable AI in Game

```
.ai on
.ai api=gemini:sk-your-api-key-here:gemini-pro
.ai personality=friendly
```

## Commands

### Basic Control
- `.ai on` - Enable AI
- `.ai off` - Disable AI
- `.ai status` - Show current status

### Configuration
- `.ai api=provider:key:model` - Set API key and model
- `.ai model=gpt-4` - Change model
- `.ai personality=friendly` - Change personality
  - Options: `friendly`, `neutral`, `cautious`, `aggressive`, `silent`, `sarcastic`

### Debugging
- `.ai logs` - Show latest AI decisions
- `.ai memory` - Show player relationships
- `.ai memory clear` - Clear all player memory

## How It Works

### Tick Flow (Every ~250ms)

```
1. Collect game state
2. Check reflex layer
   ├─ If critical (health < 4) → FLEE immediately
   └─ Skip LLM, execute reflex
3. If no reflex needed → Call LLM
4. Parse LLM response
5. Validate action
6. Execute response (chat + action)
7. Log monologue (debug)
```

### AI Response Format

The AI must output in this exact format:

```
CHAT: Your message here

ACTION: ACTION_TYPE target param1 param2
```

Examples:

```
CHAT: Sure, I'll follow you!
ACTION: FOLLOW PlayerName
```

```
CHAT: 
ACTION: NONE
```

```
CHAT: Get away from me!
ACTION: ENABLE_KILL_AURA PlayerName
```

## Available Actions

### Movement
- `FOLLOW player` - Follow a specific player
- `GO_TO x y z` - Move to coordinates
- `RUN_AWAY_FROM player` - Sprint away
- `STOP_MOVING` - Stop all movement
- `HIDE` - Find cover
- `MOVE_TO_LIGHT` - Move to well-lit area

### Combat
- `ENABLE_KILL_AURA player` - Attack specific player
- `DISABLE_KILL_AURA` - Stop attacking
- `TARGET player` - Focus on player
- `FLEE` - Run from combat

### Features
- `ENABLE_AUTO_EAT` - Start eating
- `DISABLE_AUTO_EAT` - Stop eating
- `ENABLE_ANTI_FALL` - Turn on cliff prevention
- `DISABLE_ANTI_FALL` - Turn off cliff prevention

### Special
- `NONE` - No action

## Personality Types

### Friendly
- Cooperative and helpful
- But maintains independence
- Won't be TOO helpful

### Neutral
- Professional and businesslike
- Helps if it benefits you
- Declines if it doesn't

### Cautious
- Suspicious of everyone
- Assumes traps
- Deflects personal questions

### Aggressive
- Confident and dominant
- Stands ground
- Doesn't back down (but avoids unnecessary fights)

### Silent
- Speaks rarely
- Only responds to direct questions
- Focused on own business

### Sarcastic
- Witty and humorous
- Makes jokes
- Doesn't take things seriously

## Behavior Rules

1. **Self-Preservation First**
   - Never intentionally die
   - Reflex layer overrides everything if health critical

2. **Blend In**
   - Make occasional typos
   - Don't respond to every message
   - Have consistency in personality

3. **Safety**
   - Never share exact coordinates
   - Use vague descriptions: "near spawn", "at the oak tree"
   - Don't reveal vulnerabilities

4. **Combat Assessment**
   - Fully geared player = RUN
   - Naked player = FIGHT
   - Multiple threats = FLEE

5. **Memory**
   - Remember betrayals permanently
   - Build trust over time
   - Relationships shape behavior

6. **Bot Accusations**
   - Act confused or offended
   - Deflect with humor
   - Don't admit being AI

## File Structure

```
src/main/java/com/rayferric/havook/ai/
├── AIManager.java - Main orchestrator
├── AIAction.java - Action model
├── AIResponse.java - Response model
├── AICommandHandler.java - CLI commands
├── GameStateCollector.java - Game state gathering
├── ResponseParser.java - Parse AI output
├── SystemPromptBuilder.java - Generate system prompt
├── AIMonologueLogger.java - Debug logging
│
├── llm/
│   ├── LLMConnector.java - API calls
│   └── APIKeyManager.java - Key management
│
├── memory/
│   └── PlayerMemory.java - Player reputation tracking
│
└── reflex/
    └── ReflexLayer.java - Immediate reactions
```

## Integration Points

### In Your Event Handlers

**ClientTickHandler.java:**
```java
@SubscribeEvent
public void onClientTick(ClientTickEvent event) {
    if (event.phase != Phase.START) return;
    AIManager.tick(); // Add this line
    // ... rest of code
}
```

**ChatHandler.java:**
```java
@SubscribeEvent
public void onChat(ClientChatEvent event) {
    // Extract player name and message
    AIManager.onChatReceived(playerName, message);
    // ... rest of code
}
```

**Damage Event:**
```java
@SubscribeEvent
public void onEntityDamaged(LivingDamageEvent event) {
    if (event.entity == player && event.source.getTrueSource() instanceof EntityPlayer) {
        AIManager.onPlayerDamaged(((EntityPlayer)event.source.getTrueSource()).getName());
    }
}
```

### In Your Feature System

When AI decides to execute an action, integrate with your existing features:

```java
switch (action.type) {
    case ENABLE_KILL_AURA:
        killAuraFeature.enable();
        killAuraFeature.setTarget(action.target);
        break;
    case FOLLOW:
        movementFeature.follow(action.target);
        break;
    // ... etc
}
```

## Debug Logging

AI decisions are logged to `havook_ai_logs/ai_YYYY-MM-DD.jsonl`

Each entry contains:
```json
{
  "timestamp": "12:34:56",
  "reasoning": "Player attacked me, need to defend",
  "state": {
    "health": 12.5,
    "food": 15,
    "position": "[100,64,200]",
    "nearby_players": 2
  },
  "action": "ENABLE_KILL_AURA",
  "target": "PlayerName",
  "chat": "Back off!",
  "response_time_ms": 1250
}
```

## Performance Considerations

### Latency Budget
- Reflex layer: < 1ms (no AI)
- Game state collection: ~5ms
- System prompt building: ~10ms
- LLM call: 1-5 seconds (async recommended)
- Response parsing: < 1ms
- Action execution: < 1ms

### Preventing Context Explosion
- Chat history: Limited to 30 messages
- Older messages: Summarized automatically
- Action history: Limited to 5 last actions
- Player memory: Per-player, trimmed of old interactions

### Cooldown Strategy
- AI calls: 3 seconds minimum (prevents spam)
- Chat responses: 2 seconds minimum
- Actions: 500ms minimum
- Prevents player noticing rapid response patterns

## Troubleshooting

### AI not responding
- Check `.ai status` - is it enabled?
- Check API key: `.ai status` shows if configured
- Check logs: `.ai logs` shows errors

### AI makes invalid decisions
- Check monologue logs: `havook_ai_logs/`
- Common: Following non-existent players, invalid coordinates
- Response parser should catch these

### High latency
- LLM taking > 3 seconds? Check API provider
- Consider using faster models (GPT-3.5, Gemini Flash)
- Or implement full async calls

### Memory keeps growing
- Chat history capped at 30 messages
- Action history capped at 5 actions
- Player records persist (intentional for trust)
- To clear: `.ai memory clear`

## Future Enhancements

- [ ] Async LLM calls (don't block game)
- [ ] Vision integration (screenshot → AI analysis)
- [ ] Advanced memory (location safety ratings, resource gathering)
- [ ] Multi-AI conversations (AI to AI communication)
- [ ] Streaming responses (faster token output)
- [ ] Custom training on server-specific behavior

## Example Scenarios

### Scenario 1: Player asks for coordinates
```
Player: "where are you?"

AI reasoning: This might be a trap. Never share exact coords.
Response:
  CHAT: I'm somewhere safe lol
  ACTION: NONE
```

### Scenario 2: Being attacked by geared player
```
Event: Taking damage from DiamondSword player

AI reflex: Health 12, attacker has armor
Response (immediate, no LLM):
  ACTION: FLEE

Then AI processes:
  CHAT: Alright, you win, not worth it
  ACTION: DISABLE_KILL_AURA
```

### Scenario 3: Multiple players, one hostile
```
Nearby: Player1 (friendly), Player2 (hostile)

AI reasoning: Player1 is friendly (from memory), Player2 attacked me before (betrayal)
Response:
  CHAT: Let's get out of here, they're dangerous
  ACTION: FOLLOW Player1
```

---

**Made with ❤️ for Minecraft AI gameplay**
