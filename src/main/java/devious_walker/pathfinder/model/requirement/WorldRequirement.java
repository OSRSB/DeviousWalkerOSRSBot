package devious_walker.pathfinder.model.requirement;

import lombok.Value;

import static net.runelite.rsb.methods.MethodProvider.methods;

@Value
public class WorldRequirement implements Requirement
{
    boolean memberWorld;

    @Override
    public Boolean get()
    {
        return !memberWorld || methods.worldHopper.isCurrentWorldMembers();
    }
}