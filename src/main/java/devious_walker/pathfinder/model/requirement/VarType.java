package devious_walker.pathfinder.model.requirement;

import java.util.function.Function;

import static net.runelite.rsb.methods.MethodProvider.methods;

public enum VarType implements Function<Integer, Integer>
{
    VARBIT,
    VARP;

    @Override
    public Integer apply(Integer index)
    {
        switch (this)
        {
            case VARBIT:
                return methods.client.getVarbitValue(index);
            case VARP:
                return methods.client.getVarpValue(index);
        }
        return 0;
    }
}
