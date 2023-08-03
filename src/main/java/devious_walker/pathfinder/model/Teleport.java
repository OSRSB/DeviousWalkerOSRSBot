package devious_walker.pathfinder.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Value;
import net.runelite.api.coords.WorldPoint;

@Value
@AllArgsConstructor
public class Teleport
{
	WorldPoint destination;
	int radius;
	@Getter
	Runnable handler;
}
