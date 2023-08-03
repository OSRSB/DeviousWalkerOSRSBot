package devious_walker.pathfinder;

import devious_walker.Reachable;
import net.runelite.api.Perspective;
import net.runelite.api.Tile;
import net.runelite.api.coords.Direction;
import net.runelite.api.coords.WorldPoint;

import static net.runelite.rsb.methods.MethodProvider.methods;

public class LocalCollisionMap implements CollisionMap
{
	private final boolean blockDoors;

	public LocalCollisionMap(boolean blockDoors)
	{
		this.blockDoors = blockDoors;
	}


	@Override
	public boolean n(int x, int y, int z)
	{
		WorldPoint current = new WorldPoint(x, y, z);
		if (Reachable.isObstacle(current))
		{
			return false;
		}

		Tile currentTile = methods.tiles.getTile(methods, current);
		Tile destinationTile = methods.tiles.getTile(methods, current.dy(1));

		if (currentTile != null
				&& destinationTile != null
				&& (Reachable.isDoored(currentTile, destinationTile) || Reachable.isDoored(destinationTile, currentTile))
				&& !blockDoors
		)
		{
			return !Reachable.isObstacle(destinationTile.getWorldLocation());
		}

		return Reachable.canWalk(Direction.NORTH, Reachable.getCollisionFlag(current), Reachable.getCollisionFlag(current.dy(1)));
	}

	@Override
	public boolean e(int x, int y, int z)
	{
		WorldPoint current = new WorldPoint(x, y, z);
		if (Reachable.isObstacle(current))
		{
			return false;
		}

		Tile currentTile = methods.tiles.getTile(methods, current);
		Tile destinationTile = methods.tiles.getTile(methods, current.dx(1));

		if (currentTile != null
				&& destinationTile != null
				&& (Reachable.isDoored(currentTile, destinationTile) || Reachable.isDoored(destinationTile, currentTile))
				&& !blockDoors
		)
		{
			return !Reachable.isObstacle(destinationTile.getWorldLocation());
		}

		return Reachable.canWalk(Direction.EAST, Reachable.getCollisionFlag(current), Reachable.getCollisionFlag(current.dx(1)));
	}
}
