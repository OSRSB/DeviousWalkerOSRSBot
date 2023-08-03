package devious_walker.pathfinder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

import devious_walker.GameThread;
import devious_walker.pathfinder.model.TeleportMinigame;
import devious_walker.quests.Quests;
import devious_walker.region.RegionManager;
import net.runelite.api.GameState;
import net.runelite.api.Item;
import net.runelite.api.ItemID;
import net.runelite.api.ObjectID;
import net.runelite.api.Quest;
import net.runelite.api.TileObject;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetID;
import net.runelite.api.widgets.WidgetInfo;
import static devious_walker.pathfinder.model.MovementConstants.*;
import static net.runelite.rsb.methods.MethodProvider.methods;
import static net.runelite.rsb.methods.Methods.sleep;

import devious_walker.pathfinder.model.Teleport;
import devious_walker.pathfinder.model.TeleportItem;
import devious_walker.pathfinder.model.TeleportSpell;
import devious_walker.pathfinder.model.poh.HousePortal;
import net.runelite.rsb.internal.globval.enums.MagicBook;
import net.runelite.rsb.internal.globval.enums.Spell;
import net.runelite.rsb.wrappers.RSItem;
import net.runelite.rsb.wrappers.RSObject;
import net.runelite.rsb.wrappers.RSWidget;

public class TeleportLoader
{
	private static Pattern WILDY_PATTERN = Pattern.compile("Okay, teleport to level [\\d,]* Wilderness\\.");
	private static List<Teleport> LAST_TELEPORT_LIST = new ArrayList<>();

	public static List<Teleport> buildTeleports()
	{
		List<Teleport> teleports = new ArrayList<>();
		teleports.addAll(LAST_TELEPORT_LIST);
		teleports.addAll(buildTimedTeleports());
		return teleports;
	}

	private static List<Teleport> buildTimedTeleports()
	{
		return GameThread.invokeLater(() -> {
			List<Teleport> teleports = new ArrayList<>();
			if (methods.worldHopper.isCurrentWorldMembers())
			{
				if (methods.combat.getWildernessLevel() == 0)
				{
					// Minigames
					if (RegionManager.useMinigameTeleports() && TeleportMinigame.canTeleport())
					{
						for (TeleportMinigame tp : TeleportMinigame.values())
						{
							if (tp.canUse())
							{
								teleports.add(new Teleport(tp.getLocation(), 2, () -> TeleportMinigame.teleport(tp)));
							}
						}
					}
				}
			}

			if (methods.combat.getWildernessLevel() <= 20)
			{
				for (TeleportSpell teleportSpell : TeleportSpell.values())
				{
					if (!teleportSpell.canCast() || teleportSpell.getPoint() == null)
					{
						continue;
					}

					if (teleportSpell.getPoint().distanceTo(methods.players.getMyPlayer().getLocation().getWorldLocation()) > 50)
					{
						teleports.add(new Teleport(teleportSpell.getPoint(), 5, () ->
						{
							final Spell spell = teleportSpell.getSpell();
							if (teleportSpell == TeleportSpell.TELEPORT_TO_HOUSE)
							{
								// Tele to outside
								RSWidget widget = spell.getWidget();
								if (widget == null)
								{
									return;
								}
								// TODO: what action string or maybe just click
								widget.doAction("");
							}
							else
							{
								methods.magic.castSpell(spell);
							}
						}));
					}
				}
			}

			return teleports;
		});
	}

	public static void refreshTeleports()
	{
		GameThread.invoke(() ->
		{
			List<Teleport> teleports = new ArrayList<>();
			if (methods.worldHopper.isCurrentWorldMembers())
			{
				// One click teleport items
				for (TeleportItem tele : TeleportItem.values())
				{
					if (tele.canUse() && tele.getDestination().distanceTo(methods.players.getMyPlayer().getLocation().getWorldLocation()) > 20)
					{
						switch (tele)
						{
							case ROYAL_SEED_POD:
								if (methods.combat.getWildernessLevel() <= 30)
								{
									teleports.add(itemTeleport(tele));
								}
							default:
								if (methods.combat.getWildernessLevel() <= 20)
								{
									teleports.add(itemTeleport(tele));
								}
						}
					}
				}

				if (methods.combat.getWildernessLevel() <= 20)
				{
					if (ringOfDueling())
					{
						teleports.add(new Teleport(new WorldPoint(3315, 3235, 0), 6,
							() -> jewelryTeleport("PvP Arena", RING_OF_DUELING)));
						teleports.add(new Teleport(new WorldPoint(2440, 3090, 0), 2,
							() -> jewelryTeleport("Castle Wars", RING_OF_DUELING)));
						teleports.add(new Teleport(new WorldPoint(3151, 3635, 0), 2,
							() -> jewelryTeleport("Ferox Enclave", RING_OF_DUELING)));
					}

					if (gamesNecklace())
					{
						teleports.add(new Teleport(new WorldPoint(2898, 3553, 0), 2,
							() -> jewelryTeleport("Burthorpe", GAMES_NECKLACE)));
						teleports.add(new Teleport(new WorldPoint(2520, 3571, 0), 6,
							() -> jewelryTeleport("Barbarian Outpost", GAMES_NECKLACE)));
						teleports.add(new Teleport(new WorldPoint(2964, 4382, 2), 2,
							() -> jewelryTeleport("Corporeal Beast", GAMES_NECKLACE)));
						teleports.add(new Teleport(new WorldPoint(3244, 9501, 2), 2,
							() -> jewelryTeleport("Tears of Guthix", GAMES_NECKLACE)));
						teleports.add(new Teleport(new WorldPoint(1624, 3938, 0), 1,
							() -> jewelryTeleport("Wintertodt Camp", GAMES_NECKLACE)));
					}

					if (necklaceOfPassage())
					{
						teleports.add(new Teleport(new WorldPoint(3114, 3179, 0), 2,
							() -> jewelryTeleport("Wizards' Tower", NECKLACE_OF_PASSAGE)));
						teleports.add(new Teleport(new WorldPoint(2430, 3348, 0), 2,
							() -> jewelryTeleport("The Outpost", NECKLACE_OF_PASSAGE)));
						teleports.add(new Teleport(new WorldPoint(3405, 3157, 0), 2,
							() -> jewelryTeleport("Eagle's Eyrie", NECKLACE_OF_PASSAGE)));
					}

					if (xericsTalisman())
					{
						teleports.add(new Teleport(new WorldPoint(1576, 3530, 0), 6,
												   () -> jewelryTeleport("Xeric's Lookout", XERICS_TALISMAN)));
						teleports.add(new Teleport(new WorldPoint(1752, 3566, 0), 6,
												   () -> jewelryTeleport("Xeric's Glade", XERICS_TALISMAN)));
						teleports.add(new Teleport(new WorldPoint(1504, 3817, 0), 6,
												   () -> jewelryTeleport("Xeric's Inferno", XERICS_TALISMAN)));
						if (Quests.isFinished(Quest.ARCHITECTURAL_ALLIANCE))
						{
							teleports.add(new Teleport(new WorldPoint(1640, 3674, 0), 6,
														() -> jewelryTeleport("Xeric's Heart", XERICS_TALISMAN)));
						}
					}

					if (digsitePendant())
					{
						teleports.add(new Teleport(new WorldPoint(3341, 3445, 0), 6,
								() -> jewelryTeleport("Digsite", DIGSITE_PENDANT)));
						teleports.add(new Teleport(new WorldPoint(3764, 3869, 1), 6,
								() -> jewelryTeleport("Fossil Island", DIGSITE_PENDANT)));
						if (Quests.isFinished(Quest.DRAGON_SLAYER_II))
						{
							teleports.add(new Teleport(new WorldPoint(3549, 10456, 0), 6,
								() -> jewelryTeleport("Lithkren", DIGSITE_PENDANT)));
						}
					}
				}

				if (methods.combat.getWildernessLevel() <= 30)
				{
					if (combatBracelet())
					{
						teleports.add(new Teleport(new WorldPoint(2882, 3548, 0), 2,
							() -> jewelryTeleport("Warriors' Guild", COMBAT_BRACELET)));
						teleports.add(new Teleport(new WorldPoint(3191, 3367, 0), 2,
							() -> jewelryTeleport("Champions' Guild", COMBAT_BRACELET)));
						teleports.add(new Teleport(new WorldPoint(3052, 3488, 0), 2,
							() -> jewelryTeleport("Monastery", COMBAT_BRACELET)));
						teleports.add(new Teleport(new WorldPoint(2655, 3441, 0), 2,
							() -> jewelryTeleport("Ranging Guild", COMBAT_BRACELET)));
					}

					if (skillsNecklace())
					{
						teleports.add(new Teleport(new WorldPoint(2611, 3390, 0), 6,
							() -> jewelryPopupTeleport("Fishing Guild", SKILLS_NECKLACE)));
						teleports.add(new Teleport(new WorldPoint(3050, 9763, 0), 6,
							() -> jewelryPopupTeleport("Mining Guild", SKILLS_NECKLACE)));
						teleports.add(new Teleport(new WorldPoint(2933, 3295, 0), 6,
							() -> jewelryPopupTeleport("Crafting Guild", SKILLS_NECKLACE)));
						teleports.add(new Teleport(new WorldPoint(3143, 3440, 0), 6,
							() -> jewelryPopupTeleport("Cooking Guild", SKILLS_NECKLACE)));
						teleports.add(new Teleport(new WorldPoint(1662, 3505, 0), 6,
							() -> jewelryPopupTeleport("Woodcutting Guild", SKILLS_NECKLACE)));
						teleports.add(new Teleport(new WorldPoint(1249, 3718, 0), 6,
							() -> jewelryPopupTeleport("Farming Guild", SKILLS_NECKLACE)));
					}

					if (ringOfWealth())
					{
						teleports.add(new Teleport(new WorldPoint(3163, 3478, 0), 2,
							() -> jewelryTeleport("Grand Exchange", RING_OF_WEALTH)));
						teleports.add(new Teleport(new WorldPoint(2996, 3375, 0), 2,
								() -> jewelryTeleport("Falador", RING_OF_WEALTH)));

						if (Quests.isFinished(Quest.THRONE_OF_MISCELLANIA))
						{
							teleports.add(new Teleport(new WorldPoint(2538, 3863, 0), 2,
									() -> jewelryTeleport("Miscellania", RING_OF_WEALTH)));
						}
						if (Quests.isFinished(Quest.BETWEEN_A_ROCK))
						{
							teleports.add(new Teleport(new WorldPoint(2828, 10166, 0), 2,
									() -> jewelryTeleport("Miscellania", RING_OF_WEALTH)));
						}

					}

					if (amuletOfGlory())
					{
						teleports.add(new Teleport(new WorldPoint(3087, 3496, 0), 0,
							() -> jewelryTeleport("Edgeville", AMULET_OF_GLORY)));
						teleports.add(new Teleport(new WorldPoint(2918, 3176, 0), 0,
							() -> jewelryTeleport("Karamja", AMULET_OF_GLORY)));
						teleports.add(new Teleport(new WorldPoint(3105, 3251, 0), 0,
							() -> jewelryTeleport("Draynor Village", AMULET_OF_GLORY)));
						teleports.add(new Teleport(new WorldPoint(3293, 3163, 0), 0,
							() -> jewelryTeleport("Al Kharid", AMULET_OF_GLORY)));
					}

					if (burningAmulet())
					{
						teleports.add(new Teleport(new WorldPoint(3235, 3636, 0), 5,
								() -> jewelryWildernessTeleport( "Chaos Temple", BURNING_AMULET)));
						teleports.add(new Teleport(new WorldPoint(3038, 3651, 0), 5,
								() -> jewelryWildernessTeleport("Bandit Camp", BURNING_AMULET)));
						teleports.add(new Teleport(new WorldPoint(3028, 3842, 0), 5,
								() -> jewelryWildernessTeleport( "Lava Maze", BURNING_AMULET)));
					}

					if (slayerRing())
					{
						teleports.add(new Teleport(new WorldPoint(2432, 3423, 0), 2,
								() -> slayerRingTeleport("Stronghold Slayer Cave", SLAYER_RING)));
						teleports.add(new Teleport(new WorldPoint(3422, 3537, 0), 2,
								() -> slayerRingTeleport("Slayer Tower", SLAYER_RING)));
						teleports.add(new Teleport(new WorldPoint(2802, 10000, 0), 2,
								() -> slayerRingTeleport("Fremennik Slayer Dungeon", SLAYER_RING)));
						teleports.add(new Teleport(new WorldPoint(3185, 4601, 0), 2,
								() -> slayerRingTeleport("Tarn's Lair", SLAYER_RING)));
						if (Quests.isFinished(Quest.MOURNINGS_END_PART_II))
						{
							teleports.add(new Teleport(new WorldPoint(2028, 4636, 0), 2,
								() -> slayerRingTeleport("Dark Beasts", SLAYER_RING)));
						}
					}
				}

				if (RegionManager.usePoh() && (canEnterHouse() || methods.objects.getNearest(ObjectID.PORTAL_4525) != null))
				{
					if (RegionManager.hasMountedGlory())
					{
						teleports.add(mountedPohTeleport(new WorldPoint(3087, 3496, 0), ObjectID.AMULET_OF_GLORY, "Edgeville"));
						teleports.add(mountedPohTeleport(new WorldPoint(2918, 3176, 0), ObjectID.AMULET_OF_GLORY, "Karamja"));
						teleports.add(mountedPohTeleport(new WorldPoint(3105, 3251, 0), ObjectID.AMULET_OF_GLORY, "Draynor Village"));
						teleports.add(mountedPohTeleport(new WorldPoint(3293, 3163, 0), ObjectID.AMULET_OF_GLORY, "Al Kharid"));
					}

					if (RegionManager.hasMountedDigsitePendant())
					{
						teleports.add(pohDigsitePendantTeleport(new WorldPoint(3341, 3445, 0), 1));
						teleports.add(pohDigsitePendantTeleport(new WorldPoint(3766, 3870, 1), 2));
						if (Quests.isFinished(Quest.DRAGON_SLAYER_II))
						{
							teleports.add(pohDigsitePendantTeleport(new WorldPoint(3549, 10456, 0), 3));
						}
					}

					switch (RegionManager.hasJewelryBox())
					{
						case ORNATE:
							if (Quests.isFinished(Quest.THRONE_OF_MISCELLANIA))
							{
								teleports.add(pohWidgetTeleport(new WorldPoint(2538, 3863, 0), 'j'));
							}
							teleports.add(pohWidgetTeleport(new WorldPoint(3163, 3478, 0), 'k'));
							teleports.add(pohWidgetTeleport(new WorldPoint(2996, 3375, 0), 'l'));
							if (Quests.isFinished(Quest.BETWEEN_A_ROCK))
							{
								teleports.add(pohWidgetTeleport(new WorldPoint(2828, 10166, 0), 'm'));
							}
							teleports.add(pohWidgetTeleport(new WorldPoint(3087, 3496, 0), 'n'));
							teleports.add(pohWidgetTeleport(new WorldPoint(2918, 3176, 0), 'o'));
							teleports.add(pohWidgetTeleport(new WorldPoint(3105, 3251, 0), 'p'));
							teleports.add(pohWidgetTeleport(new WorldPoint(3293, 3163, 0), 'q'));
						case FANCY:
							teleports.add(pohWidgetTeleport(new WorldPoint(2882, 3548, 0), '9'));
							teleports.add(pohWidgetTeleport(new WorldPoint(3191, 3367, 0), 'a'));
							teleports.add(pohWidgetTeleport(new WorldPoint(3052, 3488, 0), 'b'));
							teleports.add(pohWidgetTeleport(new WorldPoint(2655, 3441, 0), 'c'));
							teleports.add(pohWidgetTeleport(new WorldPoint(2611, 3390, 0), 'd'));
							teleports.add(pohWidgetTeleport(new WorldPoint(3050, 9763, 0), 'e'));
							teleports.add(pohWidgetTeleport(new WorldPoint(2933, 3295, 0), 'f'));
							teleports.add(pohWidgetTeleport(new WorldPoint(3143, 3440, 0), 'g'));
							teleports.add(pohWidgetTeleport(new WorldPoint(1662, 3505, 0), 'h'));
							teleports.add(pohWidgetTeleport(new WorldPoint(1249, 3718, 0), 'i'));
						case BASIC:
							teleports.add(pohWidgetTeleport(new WorldPoint(3315, 3235, 0), '1'));
							teleports.add(pohWidgetTeleport(new WorldPoint(2440, 3090, 0), '2'));
							teleports.add(pohWidgetTeleport(new WorldPoint(3151, 3635, 0), '3'));
							teleports.add(pohWidgetTeleport(new WorldPoint(2898, 3553, 0), '4'));
							teleports.add(pohWidgetTeleport(new WorldPoint(2520, 3571, 0), '5'));
							teleports.add(pohWidgetTeleport(new WorldPoint(2964, 4382, 2), '6'));
							teleports.add(pohWidgetTeleport(new WorldPoint(3244, 9501, 2), '7'));
							teleports.add(pohWidgetTeleport(new WorldPoint(1624, 3938, 0), '8'));
							break;
						default:
					}

					//nexus portal
					List<Teleport> nexusTeleports = getNexusTeleports();
					teleports.addAll(nexusTeleports);

					//normal house portals (remove duplicate teleports)
					RegionManager.getHousePortals().stream().
						filter(housePortal -> nexusTeleports.stream().
							noneMatch(teleport -> teleport.getDestination().equals(housePortal.getDestination()))).
						forEach(housePortal -> teleports.add(pohPortalTeleport(housePortal)));
				}
			}

			LAST_TELEPORT_LIST.clear();
			LAST_TELEPORT_LIST.addAll(teleports);
		});
	}

	public static boolean canEnterHouse()
	{
		return methods.inventory.contains(ItemID.TELEPORT_TO_HOUSE) || TeleportSpell.TELEPORT_TO_HOUSE.canCast();
	}

	public static void enterHouse()
	{
		if (TeleportSpell.TELEPORT_TO_HOUSE.canCast())
		{
			methods.magic.castSpell(MagicBook.Standard.TELEPORT_TO_HOUSE);
			return;
		}

		RSItem teleTab = methods.inventory.getItem(ItemID.TELEPORT_TO_HOUSE);
		if (teleTab != null)
		{
			teleTab.doAction("Break");
		}
	}

	public static void jewelryTeleport(String target, int... ids)
	{
		RSItem inv = methods.inventory.getItem(ids);

		if (inv != null)
		{
			if (!methods.npcChat.hasOptions())
			{
				inv.doAction("Rub");
				sleep(1200, () -> methods.npcChat.hasOptions());
				return;
			}
			methods.npcChat.selectOption(target, true);
			return;
		}

		if (!RegionManager.useEquipmentJewellery())
		{
			return;
		}

		RSItem equipped = methods.equipment.query().id(ids).first();
		if (equipped != null)
		{
			equipped.doAction(target);
		}
	}

	public static Teleport pohPortalTeleport(HousePortal housePortal)
	{
		return new Teleport(housePortal.getDestination(), 10, () ->
		{
			if (!methods.players.getMyPlayer().isIdle() || methods.client.getGameState() == GameState.LOADING)
			{
				return;
			}

			RSObject portal = methods.objects.getNearest(housePortal.getPortalName());
			if (portal != null)
			{
				// TODO:
				//portal.doAction("Enter", "Varrock", "Seers' Village", "Watchtower");
				portal.doAction("");
				return;
			}

			enterHouse();
		});
	}

	public static List<Teleport> getNexusTeleports()
	{
		List<Teleport> result = new ArrayList<>();
		int[] varbitArray = {
			6672, 6673, 6674, 6675, 6676, 6677, 6678, 6679, 6680,
			6681, 6682, 6683, 6684, 6685, 6686, 6568, 6569, 6582,
			10092, 10093, 10094, 10095, 10096, 10097, 10098,
			10099, 10100, 10101, 10102, 10103
		};

		for (int varbit : varbitArray)
		{
			int id = methods.client.getVarbitValue(varbit);
			switch (id)
			{
				case 0:
				{
					break;
				}
				case 1:
				{
					result.add(pohNexusTeleport(HousePortal.VARROCK));
					break;
				}
				case 2:
				{
					result.add(pohNexusTeleport(HousePortal.LUMBRIDGE));
					break;
				}
				case 3:
				{
					result.add(pohNexusTeleport(HousePortal.FALADOR));
					break;
				}
				case 4:
				{
					result.add(pohNexusTeleport(HousePortal.CAMELOT));
					break;
				}
				case 5:
				{
					result.add(pohNexusTeleport(HousePortal.EAST_ARDOUGNE));
					break;
				}
				case 6:
				{
					result.add(pohNexusTeleport(HousePortal.WATCHTOWER));
					break;
				}
				case 7:
				{
					result.add(pohNexusTeleport(HousePortal.SENNTISTEN));
					break;
				}
				case 8:
				{
					result.add(pohNexusTeleport(HousePortal.MARIM));
					break;
				}
				case 9:
				{
					result.add(pohNexusTeleport(HousePortal.KHARYRLL));
					break;
				}
				case 10:
				{
					result.add(pohNexusTeleport(HousePortal.LUNAR_ISLE));
					break;
				}
				case 11:
				{
					result.add(pohNexusTeleport(HousePortal.KOUREND));
					break;
				}
				case 12:
				{
					result.add(pohNexusTeleport(HousePortal.WATERBIRTH_ISLAND));
					break;
				}
				case 13:
				{
					result.add(pohNexusTeleport(HousePortal.FISHING_GUILD));
					break;
				}
				case 14:
				{
					result.add(pohNexusTeleport(HousePortal.ANNAKARL)); //wilderness
					break;
				}
				case 15:
				{
					result.add(pohNexusTeleport(HousePortal.TROLL_STRONGHOLD));
					break;
				}
				case 16:
				{
					result.add(pohNexusTeleport(HousePortal.CATHERBY));
					break;
				}
				case 17:
				{
					result.add(pohNexusTeleport(HousePortal.GHORROCK)); //wilderness
					break;
				}
				case 18:
				{
					result.add(pohNexusTeleport(HousePortal.CARRALLANGAR)); //wilderness
					break;
				}
				case 19:
				{
					result.add(pohNexusTeleport(HousePortal.WEISS));
					break;
				}
				case 20:
				{
					result.add(pohNexusTeleport(HousePortal.ARCEUUS_LIBRARY));
					break;
				}
				case 21:
				{
					result.add(pohNexusTeleport(HousePortal.DRAYNOR_MANOR));
					break;
				}
				case 22:
				{
					result.add(pohNexusTeleport(HousePortal.BATTLEFRONT));
					break;
				}
				case 23:
				{
					result.add(pohNexusTeleport(HousePortal.MIND_ALTAR));
					break;
				}
				case 24:
				{
					result.add(pohNexusTeleport(HousePortal.SALVE_GRAVEYARD));
					break;
				}
				case 25:
				{
					result.add(pohNexusTeleport(HousePortal.FENKENSTRAINS_CASTLE));
					break;
				}
				case 26:
				{
					result.add(pohNexusTeleport(HousePortal.WEST_ARDOUGNE));
					break;
				}
				case 27:
				{
					result.add(pohNexusTeleport(HousePortal.HARMONY_ISLAND));
					break;
				}
				case 28:
				{
					result.add(pohNexusTeleport(HousePortal.CEMETERY)); //wilderness
					break;
				}
				case 29:
				{
					result.add(pohNexusTeleport(HousePortal.BARROWS));
					break;
				}
				case 30:
				{
					result.add(pohNexusTeleport(HousePortal.APE_ATOLL_DUNGEON));
					break;
				}
			}
		}

		return result;
	}

	public static Teleport pohNexusTeleport(HousePortal housePortal)
	{
		WorldPoint destination = housePortal.getDestination();
		return new Teleport(destination, 10, () ->
		{
			if (!methods.players.getMyPlayer().isIdle() || methods.client.getGameState() == GameState.LOADING)
			{
				return;
			}

			RSObject nexusPortal = methods.objects.getNearest("Portal Nexus");
			if (nexusPortal == null)
			{
				enterHouse();
				return;
			}

			Widget teleportInterface = methods.client.getWidget(17, 12);
			if (teleportInterface == null || teleportInterface.isHidden())
			{
				nexusPortal.doAction("Teleport Menu");
				return;
			}

			Widget[] teleportChildren = teleportInterface.getDynamicChildren();
			if (teleportChildren == null || teleportChildren.length == 0)
			{
				//no teleports in the portal
				return;
			}

			Optional<Widget> optionalTeleportWidget = Arrays.stream(teleportChildren).
				filter(Objects::nonNull).
				filter(widget -> widget.getText() != null).
				filter(widget -> widget.getText().contains(housePortal.getNexusTarget())).
				findFirst();

			if (optionalTeleportWidget.isEmpty())
			{
				//the teleport is not in the list
				return;
			}

			Widget teleportWidget = optionalTeleportWidget.get();
			String teleportChar = teleportWidget.getText().substring(12, 13);
			methods.keyboard.sendText(teleportChar, false);
		});
	}

	public static void jewelryPopupTeleport(String target, int... ids)
	{
		RSItem inv = methods.inventory.getItem(ids);

		if (inv != null)
		{
			RSWidget baseWidget = methods.interfaces.getComponent(187, 3);
			if (baseWidget.isVisible())
			{
				RSWidget[] children = baseWidget.getComponents();
				if (children == null)
				{
					return;
				}

				for (int i = 0; i < children.length; i++)
				{
					RSWidget teleportItem = children[i];
					if (teleportItem.getText().contains(target))
					{
						methods.keyboard.sendText("" + (i + 1), false);
						return;
					}
				}
			}

			inv.doAction("Rub");
			return;
		}

		if (!RegionManager.useEquipmentJewellery())
		{
			return;
		}

		RSItem equipped = methods.equipment.query().id(ids).first();
		if (equipped != null)
		{
			equipped.doAction(target);
		}
	}

	public static Teleport pohDigsitePendantTeleport(
			WorldPoint destination,
			int action
	)
	{
		return new Teleport(destination, 10, () ->
		{
			if (!methods.players.getMyPlayer().isIdle() || methods.client.getGameState() == GameState.LOADING)
			{
				return;
			}

			if (methods.interfaces.getComponent(WidgetInfo.ADVENTURE_LOG).isVisible())
			{
				methods.keyboard.sendText("" + action, false);
				return;
			}

			RSObject digsitePendant = methods.objects.getNearest("Digsite Pendant");
			if (digsitePendant != null)
			{
				digsitePendant.doAction("Teleport menu");
				return;
			}

			enterHouse();
		});
	}

	public static Teleport itemTeleport(TeleportItem teleportItem)
	{
			return new Teleport(teleportItem.getDestination(), 5, () ->
			{
				RSItem item = methods.inventory.getItem(teleportItem.getItemId());
				if (item != null)
				{
					item.doAction(teleportItem.getAction());
				}
			});
	}


	public static Teleport pohWidgetTeleport(
			WorldPoint destination,
			char action
	)
	{
		return new Teleport(destination, 10, () ->
		{
			if (!methods.players.getMyPlayer().isIdle() || methods.client.getGameState() == GameState.LOADING)
			{
				return;
			}
			if (methods.interfaces.getComponent(590, 0) .isVisible())
			{
				methods.keyboard.sendText("" + action, false);
				return;
			}

			RSObject box = methods.objects.getNearest(to -> to.getName() != null && to.getName().contains("Jewellery Box"));
			if (box != null)
			{
				box.doAction("Teleport Menu");
				return;
			}

			enterHouse();
		});
	}

	public static Teleport mountedPohTeleport(
			WorldPoint destination,
			int objId,
			String action
	)
	{
		return new Teleport(destination, 10, () ->
		{
			if (!methods.players.getMyPlayer().isIdle() || methods.client.getGameState() == GameState.LOADING)
			{
				return;
			}

			RSObject first = methods.objects.getNearest(objId);
			if (first != null)
			{
				first.doAction(action);
				return;
			}

			enterHouse();
		});
	}

	public static void jewelryWildernessTeleport(String target, int... ids)
	{
		jewelryTeleport(target, ids);
		sleep(1000);
		if (methods.npcChat.hasOptions() && Arrays.stream(methods.npcChat.getOptions())
				.anyMatch(it -> it != null && WILDY_PATTERN.matcher(it).matches()))
		{
			methods.npcChat.selectOption(1, true, true);
		}
	}

	public static void slayerRingTeleport(String target, int... ids)
	{
		RSItem ring = methods.inventory.getItem(ids);

		if (ring == null && RegionManager.useEquipmentJewellery())
		{
			ring = methods.equipment.query().id(ids).first();
		}

		if (ring != null)
		{
			if (!methods.npcChat.hasOptions())
			{
				ring.doAction("Teleport");
				sleep(1200, methods.npcChat::hasOptions);
				return;
			}
			if (methods.npcChat.containsOption("Teleport"))
			{
				methods.npcChat.selectOption("Teleport", true);
				return;
			}
			methods.npcChat.selectOption(target, true);
		}
	}

	public static boolean ringOfDueling()
	{
		return methods.inventory.getItem(RING_OF_DUELING) != null
			|| (RegionManager.useEquipmentJewellery() && methods.equipment.query().id(RING_OF_DUELING).first() != null);
	}

	public static boolean gamesNecklace()
	{
		return methods.inventory.getItem(GAMES_NECKLACE) != null
			|| (RegionManager.useEquipmentJewellery() && methods.equipment.query().id(GAMES_NECKLACE).first() != null);
	}

	public static boolean combatBracelet()
	{
		return methods.inventory.getItem(COMBAT_BRACELET) != null
			|| (RegionManager.useEquipmentJewellery() && methods.equipment.query().id(COMBAT_BRACELET).first() != null);
	}

	public static boolean skillsNecklace()
	{
		return methods.inventory.getItem(SKILLS_NECKLACE) != null
			|| (RegionManager.useEquipmentJewellery() && methods.equipment.query().id(SKILLS_NECKLACE).first() != null);
	}

	public static boolean ringOfWealth()
	{
		return methods.inventory.getItem(RING_OF_WEALTH) != null
			|| (RegionManager.useEquipmentJewellery() && methods.equipment.query().id(RING_OF_WEALTH).first() != null);
	}

	public static boolean amuletOfGlory()
	{
		return methods.inventory.getItem(AMULET_OF_GLORY) != null
			|| (RegionManager.useEquipmentJewellery() && methods.equipment.query().id(AMULET_OF_GLORY).first() != null);
	}

	public static boolean necklaceOfPassage()
	{
		return methods.inventory.getItem(NECKLACE_OF_PASSAGE) != null
			|| (RegionManager.useEquipmentJewellery() && methods.equipment.query().id(NECKLACE_OF_PASSAGE).first() != null);
	}

	public static boolean xericsTalisman()
	{
		return methods.inventory.getItem(XERICS_TALISMAN) != null
				|| (RegionManager.useEquipmentJewellery() && methods.equipment.query().id(XERICS_TALISMAN).first() != null);
	}

	public static boolean slayerRing()
	{
		return methods.inventory.getItem(SLAYER_RING) != null
				|| (RegionManager.useEquipmentJewellery() && methods.equipment.query().id(SLAYER_RING).first() != null);
	}

	public static boolean digsitePendant()
	{
		return methods.inventory.getItem(DIGSITE_PENDANT) != null
				|| (RegionManager.useEquipmentJewellery() && methods.equipment.query().id(DIGSITE_PENDANT).first() != null);
	}

	public static boolean burningAmulet()
	{
		return methods.inventory.getItem(BURNING_AMULET) != null
				|| (RegionManager.useEquipmentJewellery() && methods.equipment.query().id(BURNING_AMULET).first() != null);
	}
}
