package devious_walker.pathfinder.model.requirement;

import lombok.Value;

import java.util.List;

import static net.runelite.rsb.methods.MethodProvider.methods;

@Value
public class ItemRequirement implements Requirement
{
    Reduction reduction;
    boolean equipped;
    List<Integer> ids;
    int amount;

    @Override
    public Boolean get()
    {
        switch (reduction)
        {
            case AND:
                if (equipped)
                {
                    return ids.stream().allMatch(it -> methods.equipment.getCount(it) >= amount);
                }
                else
                {
                    return ids.stream().allMatch(it -> methods.inventory.getCount(true, it) >= amount);
                }
            case OR:
                if (equipped)
                {
                    return ids.stream().anyMatch(it -> methods.equipment.getCount(it) >= amount);
                }
                else
                {
                    return ids.stream().anyMatch(it -> methods.inventory.getCount(true, it) >= amount);
                }
        }
        return false;
    }
}
