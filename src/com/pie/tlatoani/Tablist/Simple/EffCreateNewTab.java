package com.pie.tlatoani.Tablist.Simple;

import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import com.pie.tlatoani.Tablist.TabListManager;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

/**
 * Created by Tlatoani on 7/13/16.
 */
public class EffCreateNewTab extends Effect {
    private Expression<String> id;
    private Expression<Player> playerExpression;
    private Expression<String> displayName;
    private Expression<Number> ping;
    private Expression<Player> offlinePlayerExpression;

    @Override
    protected void execute(Event event) {
        SimpleTabList simpleTabList;
        if ((simpleTabList = TabListManager.getSimpleTabListForPlayer(playerExpression.getSingle(event))) != null) {
            simpleTabList.createTab(id.getSingle(event), displayName.getSingle(event), (ping == null ? 5 : ping.getSingle(event).intValue()), (offlinePlayerExpression == null ? null : offlinePlayerExpression.getSingle(event).getUniqueId()));
        }
    }

    @Override
    public String toString(Event event, boolean b) {
        return "create tab id " + id + " for " + playerExpression + " with display name " + displayName + (ping == null ? "" : " latency " + ping) + (offlinePlayerExpression == null ? "" : " head icon " + offlinePlayerExpression);
    }

    @Override
    public boolean init(Expression<?>[] expressions, int i, Kleenean kleenean, SkriptParser.ParseResult parseResult) {
        id = (Expression<String>) expressions[0];
        playerExpression = (Expression<Player>) expressions[1];
        displayName = (Expression<String>) expressions[2];
        ping = (Expression<Number>) expressions[3];
        offlinePlayerExpression = (Expression<Player>) expressions[4];
        return true;
    }
}