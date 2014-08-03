package ivorius.ivtoolkit.network;

import io.netty.buffer.ByteBuf;

/**
 * A interface for some object types that need extra information to be communicated
 * between the server and client when their values are updated.
 */
public interface PartialUpdateHandler
{
    /**
     * Called by the server when constructing the update packet.
     * Data should be added to the provided stream.
     *
     * @param buffer The packet data stream
     */
    public void writeUpdateData(ByteBuf buffer, String context);

    /**
     * Called by the client when it receives an update packet.
     * Data should be read out of the stream in the same way as it was written.
     *
     * @param buffer The packet data stream
     */
    public void readUpdateData(ByteBuf buffer, String context);
}
