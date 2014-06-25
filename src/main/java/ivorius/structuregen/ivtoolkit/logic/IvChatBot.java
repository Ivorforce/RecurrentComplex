/*
 * Copyright (c) 2014, Lukas Tenbrink.
 * http://lukas.axxim.net
 *
 * You are free to:
 *
 * Share — copy and redistribute the material in any medium or format
 * Adapt — remix, transform, and build upon the material
 * The licensor cannot revoke these freedoms as long as you follow the license terms.
 *
 * Under the following terms:
 *
 * Attribution — You must give appropriate credit, provide a link to the license, and indicate if changes were made. You may do so in any reasonable manner, but not in any way that suggests the licensor endorses you or your use.
 * NonCommercial — You may not use the material for commercial purposes, unless you have a permit by the creator.
 * ShareAlike — If you remix, transform, or build upon the material, you must distribute your contributions under the same license as the original.
 * No additional restrictions — You may not apply legal terms or technological measures that legally restrict others from doing anything the license permits.
 */

package ivorius.structuregen.ivtoolkit.logic;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public abstract class IvChatBot
{
    public Random random;

    public List<IvChatLine> sendQueue = new ArrayList<>();

    public IvChatBot(Random rand)
    {
        this.random = rand;
    }

    public String update()
    {
        this.updateIdle();

        if (sendQueue.size() > 0)
        {
            IvChatLine currentLine = sendQueue.get(0);

            currentLine.delay--;

            if (currentLine.delay <= 0)
            {
                sendQueue.remove(0);
                return currentLine.lineString;
            }
        }

        return null;
    }

    public void addMessageToSendQueue(String message, int delay)
    {
        sendQueue.add(new IvChatLine(delay, message));
    }

    public void addMessageToSendQueue(String message, int minDelay, int maxDelay)
    {
        sendQueue.add(new IvChatLine(random.nextInt(maxDelay - minDelay + 1) + minDelay, message));
    }

    public void addMessageToSendQueue(String message)
    {
        this.addMessageToSendQueue(message, 10, 80);
    }

    public void addMessagesToSendQueue(String[] messages)
    {
        for (String s : messages)
        {
            addMessageToSendQueue(s);
        }
    }

    // Overridable

    public abstract void updateIdle();

    public abstract void receiveChatMessage(String message);
}
