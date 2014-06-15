package ivorius.structuregen.ivtoolkit;

import io.netty.buffer.ByteBuf;

/**
 * Created by lukas on 29.05.14.
 */
public interface IGuiActionHandler
{
    void handleAction(String context, ByteBuf buffer);
}
