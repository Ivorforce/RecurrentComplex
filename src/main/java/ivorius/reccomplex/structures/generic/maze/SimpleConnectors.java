/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.structures.generic.maze;

import org.apache.commons.lang3.tuple.Pair;

/**
 * Created by lukas on 16.04.15.
 */
public class SimpleConnectors
{
    public static class Leaf extends Connector
    {
        public Leaf(String id)
        {
            super(id);
        }

        @Override
        public boolean accepts(Connector c)
        {
            return c == null;
        }
    }

    public static class Hermaphrodite extends Connector
    {
        public Hermaphrodite(String id)
        {
            super(id);
        }

        @Override
        public boolean accepts(Connector c)
        {
            return c == null || c == this;
        }
    }

    public static class Gendered extends Connector
    {
        private Gendered partner;

        private Gendered(String id)
        {
            super(id);
        }

        public static Pair<Gendered, Gendered> create(String id1, String id2)
        {
            Gendered one = new Gendered(id1);
            Gendered two = new Gendered(id2);

            one.partner = two;
            two.partner = one;

            return Pair.of(one, two);
        }

        public Gendered getPartner()
        {
            return partner;
        }

        @Override
        public boolean accepts(Connector c)
        {
            return c == null || c == partner;
        }
    }
}
