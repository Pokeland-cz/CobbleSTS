package com.kingpixel.cobblests.command;

import ca.landonjw.gooeylibs2.api.UIManager;
import ca.landonjw.gooeylibs2.api.button.GooeyButton;
import ca.landonjw.gooeylibs2.api.page.GooeyPage;
import ca.landonjw.gooeylibs2.api.template.types.ChestTemplate;
import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.CobblemonItems;
import com.cobblemon.mod.common.api.pokemon.labels.CobblemonPokemonLabels;
import com.cobblemon.mod.common.api.storage.pc.PCBox;
import com.cobblemon.mod.common.api.storage.pc.PCStore;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingpixel.cobblests.CobbleSTS;
import com.kingpixel.cobblests.utils.STSUtil;
import com.kingpixel.cobbleutils.Model.ItemModel;
import com.kingpixel.cobbleutils.util.AdventureTranslator;
import com.kingpixel.cobbleutils.util.Utils;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.List;

public class CommandSTSSellPage implements Command<ServerCommandSource> {
    @Override
    public int run(CommandContext<ServerCommandSource> context) {
        ServerPlayerEntity player = context.getSource().getPlayer();
        if (player == null) {
            return 0;
        }

        int page = IntegerArgumentType.getInteger(context, "page");
        PCStore store = Cobblemon.INSTANCE.getStorage().getPC(player);
        List<PCBox> boxes = store.getBoxes();
        if(boxes.size() < page || page < 1) {
            context.getSource().sendError(Text.literal("Invalid page (" + page + "), must be 1 to " + boxes.size()));
            return 0;
        }

        PCBox box = boxes.get(page - 1);
        if (CobbleSTS.config.isHavecooldown() && !CobbleSTS.manager.hasCooldownEnded(player)) {
            context.getSource().sendMessage(Text.literal(CobbleSTS.language.getPrefix() + " " + CobbleSTS.manager.formatTime(player)));
            return 1;
        }

        UIManager.openUIForcefully(player, confirmUI(box, page));
        return 1;
    }

    private static GooeyPage confirmUI(PCBox box, int page) {
        GooeyButton fill = GooeyButton.builder()
                .display(Utils.parseItemId(CobbleSTS.language.getFill()))
                .with(DataComponentTypes.CUSTOM_NAME, AdventureTranslator.toNative(""))
                .build();

        ItemModel itemConfirm = CobbleSTS.language.getConfirm();
        GooeyButton confirm = GooeyButton.builder()
                .display(itemConfirm.getItemStack())
                .onClick(action -> {
                    box.forEach(pokemon -> {
                        if(canSell(pokemon)) {
                            STSUtil.Sell(pokemon, true, action.getPlayer(), false);
                        }
                    });
                    UIManager.closeUI(action.getPlayer());
                })
                .build();

        GooeyButton pageButton = GooeyButton.builder()
                .display(new ItemStack(CobblemonItems.PC))
                .with(DataComponentTypes.ITEM_NAME,
                        AdventureTranslator.toNative(CobbleSTS.language.getPcpage() + " " + page))
                .with(DataComponentTypes.LORE, new LoreComponent(List.of()))
                .build();

        ItemModel itemCancel = CobbleSTS.language.getCancel();
        GooeyButton cancel = GooeyButton.builder()
                .display(itemCancel.getItemStack())
                .onClick(action -> UIManager.closeUI(action.getPlayer()))
                .build();

        ChestTemplate template = ChestTemplate.builder(3)
                .fill(fill)
                .set(1, 2, confirm)
                .set(1, 4, pageButton)
                .set(1, 6, cancel)
                .build();

        return GooeyPage.builder().title(AdventureTranslator.toNative(CobbleSTS.language.getTitleconfirm())).template(template).build();
    }

    private static boolean canSell(Pokemon pokemon) {
        if(CobbleSTS.config.getBlacklisted().contains(pokemon.showdownId())) {
            return false;
        }

        if(pokemon.getShiny() && !CobbleSTS.config.isAllowshiny()) {
            return false;
        }

        if((pokemon.isLegendary() || pokemon.isMythical() || pokemon.hasLabels(CobblemonPokemonLabels.PARADOX) || pokemon.isUltraBeast()) && !CobbleSTS.config.isAllowlegendary()) {
            return false;
        }

        return pokemon.heldItem().isEmpty();
    }
}
