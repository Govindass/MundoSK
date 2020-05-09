package com.pie.tlatoani.WorldManagement;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.classes.Converter;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.registrations.Converters;
import com.pie.tlatoani.Core.Registration.Registration;
import com.pie.tlatoani.WorldCreator.WorldCreatorData;
import com.pie.tlatoani.WorldManagement.WorldLoader.*;
import org.bukkit.World;
import org.bukkit.WorldCreator;

/**
 * Created by Tlatoani on 8/8/17.
 */
public class WorldManagementMundo {
    
    public static void load() {
        Converters.registerConverter(World.class, WorldCreator.class, new Converter<World, WorldCreator>() {
            @Override
            public WorldCreator convert(World world) {
                WorldCreator worldCreator = new WorldCreator(world.getName());
                worldCreator.copy(world);
                worldCreator.type(world.getWorldType());
                worldCreator.generateStructures(world.canGenerateStructures());
                worldCreator.generatorSettings("");
                return worldCreator;
            }
        });
        Registration.registerEffect(EffUnloadWorld.class, "unload %world% [save %-boolean%]")
                .document("Unload World", "Before 1.4", "Unloads the specified world. You can specify whether or not to save before unloading (this defaults to true).");
        Registration.registerEffect(EffDeleteWorld.class, "delete %world%")
                .document("Delete World", "Before 1.4", "Deletes the specified world. The specified world must be loaded in order to be deleted.");
        Registration.registerEffect(EffDuplicateWorld.class, "duplicate %world% (with|using) name %string%")
                .document("Duplicate World", "Before 1.4", "Creates a copy of the specified world using the specified string as a name. The specified world must be loaded in order for this to work.");

        loadWorldLoader();
    }
    
    private static void loadWorldLoader() {
        Registration.registerEffect(EffLoadWorldAutomatically.class, "[(1¦don't|1¦do not)] load %world% automatically", "[(1¦don't|1¦do not)] autoload %world%")
                .document("Load World Automatically", "1.8", "Tells MundoSK whether it should load the specified world automatically on server start. "
                        + "This is useful for simple and straightforward world management without the need for a world management plugin. "
                        + "Don't run this effect with the main world, as Bukkit will already load that world automatically, and this effect can't be used to enable/disable that behavior.");
        Registration.registerExpression(ExprAllAutomaticCreators.class, WorldCreatorData.class, ExpressionType.SIMPLE, "[all] auto[matic ]creators")
                .document("All Automatic Creators", "1.8", "An expression for all of the world creators that MundoSK is currently set to automatically run on server start.")
                .changer(Changer.ChangeMode.ADD, WorldCreatorData.class, "1.8", "Specifies that given creator should be used as an automatic creator.")
                .changer(Changer.ChangeMode.REMOVE, WorldCreatorData.class, "1.8", "Specifies that the world with the worldname of the given creator should not be loaded automatically.")
                .changer(Changer.ChangeMode.REMOVE, String.class, "1.8", "Specifies that the world with the given worldname should not be loaded automatically")
                .changer(Changer.ChangeMode.DELETE, "1.8", "Specifies that no worlds should be loaded automatically.");
        Registration.registerPropertyExpression(ExprAutomaticCreator.class, WorldCreatorData.class, "string", "auto[matic ]creator [for world [named]] %")
                .document("Automatic Creator", "1.8", "An expression for the automatic creator (if there is one) that MundoSK is currently set to run for the world with the specified name.")
                .changer(Changer.ChangeMode.SET, WorldCreatorData.class, "1.8", "Specifies an automatic creator for the specified world.")
                .changer(Changer.ChangeMode.DELETE, "1.8", "Specifies that the specified world should not be loaded automatically.");
    }
}
