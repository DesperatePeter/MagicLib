package org.magiclib;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.thoughtworks.xstream.XStream;
import org.magiclib.bounty.*;
import org.magiclib.kotlin.MagicKotlinModPlugin;
import org.magiclib.plugins.MagicAutoTrails;
import org.magiclib.plugins.MagicCampaignTrailPlugin;
import org.magiclib.terrain.MagicAsteroidBeltTerrainPlugin;
import org.magiclib.terrain.MagicAsteroidFieldTerrainPlugin;
import org.magiclib.util.*;

/**
 * Master ModPlugin for MagicLib. Handles all the loading of data and scripts.
 */
public class Magic_modPlugin extends BaseModPlugin {

    ////////////////////////////////////////
    //                                    //
    //       ON APPLICATION LOAD          //
    //                                    //
    ////////////////////////////////////////


    @Override
    public void onApplicationLoad() throws ClassNotFoundException {
        data.scripts.Magic_modPlugin.onApplicationLoad();

        MagicSettings.loadModSettings();

        if (MagicSettings.modSettings == null) {
            String message = System.lineSeparator()
                    + System.lineSeparator() + "Malformed modSettings.json detected"
                    + System.lineSeparator() + System.lineSeparator();
            throw new ClassNotFoundException(message);
        }


        //dev-mode pre-loading the bounties to throw a crash if the JSON is messed up on merge
        if (Global.getSettings().isDevMode()) {
            MagicBountyLoader.loadBountiesFromJSON(false);
//            if (!Global.getSettings().getModManager().isModEnabled("vayrasector") || VayraModPlugin.UNIQUE_BOUNTIES == false) {
//                MagicBountyHVB.convertHVBs(false);
//            }
            if (MagicBountyLoader.JSONfailed) {
                String message = System.lineSeparator()
                        + System.lineSeparator() + "Malformed MagicBounty_data.json detected"
                        + System.lineSeparator() + System.lineSeparator();
                throw new ClassNotFoundException(message);
            }
        }

        //gather interference data
        MagicInterference.loadInterference();

        //gather trail data
        MagicAutoTrails.getTrailData();

        //gather mod's system themes
        MagicVariables.loadThemesBlacklist();
        MagicVariables.verbose = Global.getSettings().isDevMode();
        MagicVariables.bounty_test_mode = MagicSettings.getBoolean(MagicVariables.MAGICLIB_ID, "bounty_board_test_mode");
    }

    @Override
    public void onDevModeF8Reload() {
        data.scripts.Magic_modPlugin.onDevModeF8Reload();

        MagicSettings.loadModSettings();
        //gather interference data
        MagicInterference.loadInterference();

        //gather trail data
        MagicAutoTrails.getTrailData();

        //Check for other bounty systems
        MagicVariables.checkBountySystems();

        //gather mod's system themes
        MagicVariables.loadThemesBlacklist();
        MagicVariables.verbose = Global.getSettings().isDevMode();
        MagicVariables.bounty_test_mode = MagicSettings.getBoolean(MagicVariables.MAGICLIB_ID, "bounty_board_test_mode");
    }

    ////////////////////////////////////////
    //                                    //
    //            ON GAME LOAD            //
    //                                    //
    ////////////////////////////////////////

    @Override
    public void onGameLoad(boolean newGame) {
        data.scripts.Magic_modPlugin.onGameLoad(newGame);

//        MagicAutoTrails.getTrailData();
        MagicIncompatibleHullmods.clearData();

        //Add industry item wrangler
        SectorAPI sector = Global.getSector();
        if (sector != null) {
            sector.addTransientListener(new MagicIndustryItemWrangler());
            sector.addTransientScript(new MagicCampaignTrailPlugin());
        }

        MagicVariables.checkBountySystems();

        if (MagicVariables.getMagicBounty()) {
            if (newGame) {
                //add all bounties on a new game
                MagicBountyLoader.loadBountiesFromJSON(false);
                //convert the HVBs if necessary
                if (!MagicVariables.getHVB()) MagicBountyHVB.convertHVBs(false);
            } else {
                if (MagicSettings.getBoolean(MagicVariables.MAGICLIB_ID, "bounty_board_reloadAll")) {
                    //force cleanup of all the bounties that have not been taken
                    MagicBountyLoader.clearBountyData();
                }
                //only add new bounties if there are any on a save load
                MagicBountyLoader.loadBountiesFromJSON(!Global.getSettings().isDevMode());
                if (!MagicVariables.getHVB()) MagicBountyHVB.convertHVBs(!Global.getSettings().isDevMode());
            }

            MagicBountyCoordinator.onGameLoad();
            MagicBountyCoordinator.getInstance().configureBountyListeners();

            Global.getSector().registerPlugin(new MagicBountyCampaignPlugin());
        }

        MagicKotlinModPlugin.INSTANCE.onGameLoad(newGame);
    }

    /**
     * Define how classes are named in the save xml, allowing class renaming without
     * breaking saves.
     *
     * @param x
     */
    @Override
    public void configureXStream(XStream x) {
        super.configureXStream(x);
        data.scripts.Magic_modPlugin.configureXStream(x);

        x.alias("MagicBountyBarEvent", MagicBountyBarEvent.class);
        x.alias("MagicBountyActiveBounty", ActiveBounty.class);
        x.alias("MagicBountyBattleListener", MagicBountyBattleListener.class);
        x.alias("MagicBountyIntel", MagicBountyIntel.class);
        x.alias("MagicBountyFleetEncounterContext", MagicBountyFleetEncounterContext.class);
        x.alias("MagicBountyFleetInteractionDialogPlugin", MagicBountyFleetInteractionDialogPlugin.class);
        x.alias("MagicCampaignPlugin", MagicBountyCampaignPlugin.class);

        // Keep the Magic replacements out of the save file.
        // The game will automatically swap to the Magic replacements on load because `terrain.json` replaces the vanilla ones.
        x.alias("AsteroidBeltTerrainPlugin", MagicAsteroidBeltTerrainPlugin.class);
        x.alias("AsteroidFieldTerrainPlugin", MagicAsteroidFieldTerrainPlugin.class);
    }

    //    //debugging magic bounties
//    
//    private static final Logger LOG = Global.getLogger(Magic_modPlugin.class);
//    @Override
//    public void onNewGameAfterEconomyLoad() {
//        for(String b : MagicBountyData.BOUNTIES.keySet()){
//            LOG.error(" ");
//            LOG.error("Testing the "+b+" bounty");
//            LOG.error(" ");
//            
//            bountyData data = MagicBountyData.getBountyData(b);
//            for(int i=0; i<10; i++){
//                SectorEntityToken location = MagicCampaign.findSuitableTarget(
//                        data.location_marketIDs,
//                        data.location_marketFactions,
//                        data.location_distance,
//                        data.location_themes,
//                        data.location_entities,
//                        data.location_defaultToAnyEntity,
//                        data.location_prioritizeUnexplored,
//                        true);
//                if(location!=null){
//                    LOG.error(location.getName()+ " is suitable in "+ location.getStarSystem().getName() +" at a distance of "+(int)location.getStarSystem().getLocation().length());
//                } else {
//                    LOG.error("CANNOT FIND SUITABLE LOCATION");
//                }
//            }
//        }
//        LOG.debug("end of bounty list");
//    }
}
