package data.scripts.util;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.util.Misc;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Tartiflette
 */
public class MagicVariables {
    
    static float SECTOR_SIZE=0;
    static float SECTOR_HEIGT=0;
    static float SECTOR_WIDTH=0;
    
    private static void calculateSectorDimentions(){        
        float dist=0;        
        for ( StarSystemAPI s : Global.getSector().getStarSystems()){
            SECTOR_WIDTH=Math.max(SECTOR_WIDTH,Math.abs(s.getLocation().x));
            SECTOR_HEIGT=Math.max(SECTOR_HEIGT,Math.abs(s.getLocation().y));
            dist=Math.max(dist,s.getLocation().lengthSquared());
        }
        SECTOR_SIZE=(float)Math.sqrt(dist);        
    }
    
    public static float getSectorSize(){
        if(SECTOR_SIZE==0){
            calculateSectorDimentions();
        }
        return SECTOR_SIZE;
    }
    public static float getSectorSizeLY(){
        if(SECTOR_SIZE==0){
            calculateSectorDimentions();
        }
        return SECTOR_SIZE/Misc.getUnitsPerLightYear();
    }
    
    public static float getSectorHeight(){
        if(SECTOR_HEIGT==0){
            calculateSectorDimentions();
        }
        return SECTOR_HEIGT;
    }    
    public static float getSectorHeightLY(){
        if(SECTOR_HEIGT==0){
            calculateSectorDimentions();
        }
        return SECTOR_HEIGT/Misc.getUnitsPerLightYear();
    }
    
    public static float getSectorWidth(){
        if(SECTOR_WIDTH==0){
            calculateSectorDimentions();
        }
        return SECTOR_WIDTH;
    }
    
    public static float getSectorWidthLY(){
        if(SECTOR_WIDTH==0){
            calculateSectorDimentions();
        }
        return SECTOR_WIDTH/Misc.getUnitsPerLightYear();
    }
    
    public static final String VARIANT_PATH = "data/config/modFiles/magicBounty_variants/";
    public static final String AVOID_COLONIZED_SYSTEM = "theme_already_colonized";
    public static final String AVOID_OCCUPIED_SYSTEM = "theme_already_occupied";
    public static final String MAGICLIB_ID = "MagicLib";
    public static final String BOUNTY_FACTION = "ML_bounty";
    
    public static boolean verbose=false;
    public static boolean bounty_test_mode=false;
    
    public static List<String> mergedThemesBlacklist = new ArrayList<>();
    
    public static void loadThemesBlacklist(){
        mergedThemesBlacklist.clear();
        //load list from settings
        List<String> themes = MagicSettings.getList(MAGICLIB_ID, VARIANT_PATH);
        for (String s : themes){
            if(!mergedThemesBlacklist.contains(s)) mergedThemesBlacklist.add(s);
        }
        //default vanilla themes to load
        mergedThemesBlacklist.add("theme_unsafe");
        mergedThemesBlacklist.add("theme_remnant");
        mergedThemesBlacklist.add("theme_remnant_main");
        mergedThemesBlacklist.add("theme_remnant_secondary");
        mergedThemesBlacklist.add("theme_remnant_no_fleets");
        mergedThemesBlacklist.add("theme_remnant_destroyed");
        mergedThemesBlacklist.add("theme_remnant_suppressed");
        mergedThemesBlacklist.add("theme_remnant_resurgent");
    }
    
    public static List<String> presetShipIdsOfLastCreatedFleet = new ArrayList<>();
}
