/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.world.gen.feature.structure.generic;

import com.google.gson.annotations.SerializedName;

/**
 * Created by lukas on 22.02.15.
 */
public class Metadata
{
    @SerializedName("authors")
    public String authors = "";

    @SerializedName("weblink")
    public String weblink = "";

    @SerializedName("comment")
    public String comment = "";
}
