package devious_walker;

import com.google.inject.Singleton;
import devious_walker.pathfinder.model.poh.HousePortal;
import devious_walker.pathfinder.model.poh.JewelryBox;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@Singleton
public class ConfigManager {
    public static boolean useTransports = true;
    public static boolean useTeleports = true;
    public static boolean avoidWilderness = false;
    public static boolean useEquipmentJewellery = true;
    public static boolean usePoh = false;
    public static boolean hasMountedDigsitePendant = false;
    public static boolean hasMountedGlory = false;
    public static boolean hasMountedMythicalCape = false;
    public static boolean hasMountedXericsTalisman = false;
    public static JewelryBox hasJewelryBox = JewelryBox.NONE;
    public static boolean useMinigameTeleports = true;
    public static Set<HousePortal> housePortals = Set.of();
}
