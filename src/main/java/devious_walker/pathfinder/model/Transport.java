package devious_walker.pathfinder.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Value;
import net.runelite.api.coords.WorldPoint;
import devious_walker.pathfinder.model.requirement.Requirements;

@Value
@AllArgsConstructor
public class Transport
{
    @Getter
    WorldPoint source;
    @Getter
    WorldPoint destination;
    int sourceRadius;
    int destinationRadius;
    Runnable handler;
    @Getter
    Requirements requirements;

    public Transport(WorldPoint source,
              WorldPoint destination,
              int sourceRadius,
              int destinationRadius,
              Runnable handler
    )
    {
        this.source = source;
        this.destination = destination;
        this.sourceRadius = sourceRadius;
        this.destinationRadius = destinationRadius;
        this.handler = handler;
        this.requirements = new Requirements();
    }
}
