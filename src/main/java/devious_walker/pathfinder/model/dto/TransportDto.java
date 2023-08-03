package devious_walker.pathfinder.model.dto;
import lombok.Value;
import net.runelite.api.coords.WorldPoint;
import devious_walker.pathfinder.TransportLoader;
import devious_walker.pathfinder.model.Transport;
import devious_walker.pathfinder.model.requirement.Requirements;

@Value
public class TransportDto
{
    WorldPoint source;
    WorldPoint destination;
    String action;
    Integer objectId;
    Requirements requirements;

    public Transport toTransport()
    {
        return TransportLoader.objectTransport(source, destination, objectId, action, requirements);
    }
}
