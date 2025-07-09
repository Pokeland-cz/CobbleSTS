package com.kingpixel.fabric.cobblests;

import com.kingpixel.cobblests.CobbleSTS;
import com.kingpixel.cobblests.utils.SalesLogger;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;


public class CobbleSTSFabric implements ModInitializer {

  @Override
  public void onInitialize() {
    CobbleSTS.init();
    SalesLogger.init(FabricLoader.getInstance().getGameDir());
  }
}