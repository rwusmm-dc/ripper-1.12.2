# AI System Integration Guide

This document explains how to integrate the AI system with your existing Havook mod.

## Step 1: Update Dependencies ✅

Already done in `build.gradle`. GSON is now included.

## Step 2: Integrate with Event Handlers

### Update ClientTickHandler.java

Add AI tick to your client tick handler:

```java
package com.rayferric.havook.handler;

import com.rayferric.havook.Havook;
import com.rayferric.havook.ai.AIManager;  // ADD THIS
import com.rayferric.havook.gui.LongMessageSenderGui;
import com.rayferric.havook.util.ChatUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;

public class ClientTickHandler {
	private static GuiScreen nextGui = null;

	@SubscribeEvent
	public void onClientTick(ClientTickEvent event) {
		if (event.phase != Phase.START)
			return;

		if (nextGui != null) {
			Minecraft.getMinecraft().displayGuiScreen(nextGui);
			nextGui = null;
		}
		
		AIManager.tick();  // ADD THIS LINE
	}

	public static void queueGui(GuiScreen gui) {
		nextGui = gui;
	}
}
```

### Create AIEventHandler.java

Create a new handler for AI-specific events:

```java
package com.rayferric.havook.handler;

import com.rayferric.havook.ai.AIManager;
import com.rayferric.havook.ai.GameStateCollector;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.entity.player.EntityPlayer;

public class AIEventHandler {
    
    @SubscribeEvent
    public void onChatReceived(ClientChatReceivedEvent event) {
        // Parse chat message to extract player name and message
        String message = event.getMessage().getUnformattedText();
        
        // Simple parsing - adjust based on your chat format
        String[] parts = message.split(": ", 2);
        if (parts.length == 2) {
            String playerName = parts[0];
            String chatMessage = parts[1];
            
            GameStateCollector.logChat(message);
            AIManager.onChatReceived(playerName, chatMessage);
        }
    }
}
```

### Register Event Handler

In your proxy class (likely ClientProxy.java):

```java
@SubscribeEvent
public void preInit(FMLPreInitializationEvent event) {
    MinecraftForge.EVENT_BUS.register(new AIEventHandler());
    // ... rest of your code
}
```

## Step 3: Add AI Commands

Update your command system to handle `.ai` commands. Depending on how your commands work:

If you have a command parser, add:

```java
if (message.startsWith(".ai")) {
    String[] parts = message.substring(4).trim().split(" ");
    if (AICommandHandler.handleCommand(parts)) {
        return; // Command handled
    }
}
```

## Step 4: Integrate with Feature System

In your feature execution code, update to check AI decisions:

```java
// When an AI action is ready to execute
AIAction action = aiManager.getLastAction();
if (action != null) {
    switch (action.type) {
        case ENABLE_KILL_AURA:
            // Tell your Kill Aura feature to activate
            killAuraFeature.enable();
            if (action.target != null) {
                killAuraFeature.setTargetPlayer(action.target);
            }
            break;
            
        case DISABLE_KILL_AURA:
            killAuraFeature.disable();
            break;
            
        case FOLLOW:
            movementFeature.startFollowing(action.target);
            break;
            
        case FLEE:
            movementFeature.flee();
            break;
            
        case ENABLE_AUTO_EAT:
            autoEatFeature.enable();
            break;
            
        case DISABLE_AUTO_EAT:
            autoEatFeature.disable();
            break;
            
        // ... etc
    }
}
```

## Step 5: Hook into Damage Events

For tracking when the player is attacked:

```java
@SubscribeEvent
public void onLivingDamage(LivingDamageEvent event) {
    if (event.getEntity() == Minecraft.getMinecraft().player) {
        if (event.getSource().getTrueSource() instanceof EntityPlayer) {
            EntityPlayer attacker = (EntityPlayer) event.getSource().getTrueSource();
            AIManager.onPlayerDamaged(attacker.getName());
        }
    }
}
```

## Step 6: Set Up Chat Sending

Update your chat sending to include AI responses:

```java
// In your chat sending method
public static void sendChatMessage(String message) {
    Minecraft.getMinecraft().player.sendChatMessage(message);
    GameStateCollector.logChat("Vibebot: " + message);
}
```

## Step 7: Configure API Key

Users can now configure the AI with:

```
.ai api=gemini:sk-your-key-here:gemini-pro
.ai on
```

## Testing the Integration

1. **Test API Configuration:**
   ```
   .ai status
   ```
   Should show your provider and model.

2. **Test Enable/Disable:**
   ```
   .ai off
   .ai on
   ```

3. **Test Commands:**
   ```
   .ai personality=friendly
   .ai logs
   .ai memory
   ```

4. **Test in Game:**
   - Stand near an NPC or other player
   - Watch the logs for AI decisions
   - Check `havook_ai_logs/` for monologue entries

## File Structure Reference

Your AI system is now in:
```
src/main/java/com/rayferric/havook/ai/
├── AIManager.java ← Main entry point
├── AICommandHandler.java ← For .ai commands  
├── GameStateCollector.java ← Collects game state
├── ResponseParser.java ← Parses AI responses
├── SystemPromptBuilder.java ← Builds prompts
├── AIMonologueLogger.java ← Logs decisions
│
├── llm/
│   ├── LLMConnector.java ← Makes API calls
│   └── APIKeyManager.java ← Manages keys
│
├── memory/
│   └── PlayerMemory.java ← Tracks player reputation
│
└── reflex/
    └── ReflexLayer.java ← Immediate reactions
```

## Configuration Files

After running with AI, you'll have:
```
havook_ai_config.txt ← Stores your last configuration
havook_ai_logs/ ← Daily log files for debugging
```

## Common Integration Issues

### Issue: "API not configured" error

**Solution:** User needs to run:
```
.ai api=gemini:YOUR_API_KEY:gemini-pro
.ai on
```

### Issue: GSON not found at compile time

**Solution:** Run:
```
gradlew clean build
```

### Issue: AI commands not working

**Solution:** Make sure your command handler calls `AICommandHandler.handleCommand()` for `.ai` prefix.

### Issue: No chat responses

**Solution:** 
1. Check `.ai status`
2. Check logs: `.ai logs`
3. Verify player messages are being logged via `AIManager.onChatReceived()`

## Next Steps

1. ✅ AI Core System is ready
2. ⏳ Integrate with your event handlers (see Step 2-6 above)
3. ⏳ Test each component
4. ⏳ Deploy to test server
5. ⏳ Gather player feedback and refine

## Support

For detailed AI behavior documentation, see `AI_SYSTEM_README.md`

For API setup, see the README's "Setup & Configuration" section.
