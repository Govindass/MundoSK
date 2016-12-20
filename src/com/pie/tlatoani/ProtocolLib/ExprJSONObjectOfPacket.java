package com.pie.tlatoani.ProtocolLib;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.VariableString;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedServerPing;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;
import com.pie.tlatoani.Mundo;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Created by Tlatoani on 5/4/16.
 */
public class ExprJSONObjectOfPacket extends SimpleExpression<JSONObject> {
    private PacketInfoConverter<JSONObject> singleConverter = null;
    private PacketInfoConverter<JSONObject[]> pluralConverter = null;
    private Expression<Number> index;
    private Expression<PacketContainer> packetContainerExpression;
    private boolean isSingle;

    public static Map<String, PacketInfoConverter<JSONObject>> singleConverters = new LinkedHashMap<>();
    public static Map<String, PacketInfoConverter<JSONObject[]>> pluralConverters = new LinkedHashMap<>();

    static {

        //Converters

        registerSingleConverter("chatcomponent", new PacketInfoConverter<JSONObject>() {
            @Override
            public JSONObject get(PacketContainer packet, Integer index) {
                Mundo.debug(this, "Packet :" + packet);
                Mundo.debug(this, "ChatComponents :" + packet.getChatComponents());
                WrappedChatComponent chatComponent = packet.getChatComponents().readSafely(index);
                String fromjson = chatComponent.getJson();
                Mundo.debug(this, "Fromjson: " + fromjson);
                JSONParser parser = new JSONParser();
                JSONObject tojson = null;
                try {
                    tojson = (JSONObject) parser.parse(fromjson);
                } catch (ParseException | ClassCastException e) {
                    Mundo.debug(ExprJSONObjectOfPacket.class, e);
                }
                Mundo.debug(this, "Tojson " + tojson);
                return tojson;
            }

            @Override
            public void set(PacketContainer packet, Integer index, JSONObject value) {
                WrappedChatComponent chatComponent = WrappedChatComponent.fromJson(value.toString());
                packet.getChatComponents().writeSafely(index, chatComponent);
            }
        });

        registerSingleConverter("serverping", new PacketInfoConverter<JSONObject>() {
            @Override
            public JSONObject get(PacketContainer packet, Integer index) {
                try {
                    return (JSONObject) (new JSONParser()).parse(packet.getServerPings().readSafely(0).toJson());
                } catch (ParseException | ClassCastException e) {
                    Mundo.reportException(ExprJSONObjectOfPacket.class, e);
                    return null;
                }
            }

            @Override
            public void set(PacketContainer packet, Integer index, JSONObject value) {
                packet.getServerPings().writeSafely(0, WrappedServerPing.fromJson(value.toJSONString()));
            }
        });

        registerSingleConverter("datawatcher", new PacketInfoConverter<JSONObject>() {
            @Override
            public JSONObject get(PacketContainer packet, Integer index) {
                JSONObject jsonObject = new JSONObject();
                WrappedDataWatcher dataWatcher = packet.getDataWatcherModifier().readSafely(index);
                jsonObject.put("entity", dataWatcher.getEntity());
                if (dataWatcher != null) {
                    dataWatcher.forEach(new Consumer<WrappedWatchableObject>() {
                        int i = 0;

                        @Override
                        public void accept(WrappedWatchableObject wrappedWatchableObject) {
                            jsonObject.put("" + i, wrappedWatchableObject.getValue());
                            i++;
                        }
                    });
                }
                return jsonObject;
            }

            @Override
            public void set(PacketContainer packet, Integer index, JSONObject value) {
                List<WrappedWatchableObject> wrappedWatchableObjects = new ArrayList<WrappedWatchableObject>();
                Entity entity = (Entity) value.get("entity");
                value.forEach(new BiConsumer() {
                    @Override
                    public void accept(Object o, Object o2) {
                        try {
                            String key = (String) o;
                            int i = Integer.parseInt(key);
                            WrappedWatchableObject watchableObject = new WrappedWatchableObject(i, o2);
                            wrappedWatchableObjects.add(watchableObject);

                        } catch (ClassCastException | NumberFormatException e) {}
                    }
                });
                WrappedDataWatcher dataWatcher = new WrappedDataWatcher(wrappedWatchableObjects);
                dataWatcher.setEntity(entity);
                packet.getDataWatcherModifier().writeSafely(index, dataWatcher);
            }
        });

        registerSingleConverter("watchablecollection", new PacketInfoConverter<JSONObject>() {
            @Override
            public JSONObject get(PacketContainer packet, Integer index) {
                JSONObject jsonObject = new JSONObject();
                Collection<WrappedWatchableObject> wrappedWatchableObjects = packet.getWatchableCollectionModifier().readSafely(index);
                if (wrappedWatchableObjects != null) {
                    wrappedWatchableObjects.forEach(new Consumer<WrappedWatchableObject>() {
                        int i = 0;

                        @Override
                        public void accept(WrappedWatchableObject wrappedWatchableObject) {
                            jsonObject.put("" + i, wrappedWatchableObject.getValue());
                            i++;
                        }
                    });
                }
                return jsonObject;
            }

            @Override
            public void set(PacketContainer packet, Integer index, JSONObject value) {
                List<WrappedWatchableObject> wrappedWatchableObjects = new ArrayList<WrappedWatchableObject>();
                value.forEach(new BiConsumer() {
                    @Override
                    public void accept(Object o, Object o2) {
                        try {
                            String key = (String) o;
                            int i = Integer.parseInt(key);
                            WrappedWatchableObject watchableObject = new WrappedWatchableObject(i, o2);
                            wrappedWatchableObjects.add(watchableObject);

                        } catch (ClassCastException | NumberFormatException e) {}
                    }
                });
                packet.getWatchableCollectionModifier().writeSafely(index, wrappedWatchableObjects);
            }
        });
    }

    public static void registerSingleConverter(String name, PacketInfoConverter<JSONObject> converter) {
        singleConverters.put(name, converter);
    }

    public static void registerPluralConverter(String name, PacketInfoConverter<JSONObject[]> converter) {
        pluralConverters.put(name, converter);
    }

    public static PacketInfoConverter<JSONObject> getSingleConverter(String name) {
        return singleConverters.get(name);
    }

    public static PacketInfoConverter<JSONObject[]> getPluralConverter(String name) {
        return pluralConverters.get(name);
    }

    public static String getConverterNamesPattern(Boolean isSingle) {
        String result = "";
        int i = 0;
        for (String name : isSingle ? singleConverters.keySet() : pluralConverters.keySet()) {
            i++;
            result += i + "¦" + name + "|";
        }
        return result.substring(0, result.length() - 1);
    }

    public static String getConverterNameByIndex(int index, Boolean isSingle) {
        int i = 0;
        for (String name : isSingle ? singleConverters.keySet() : pluralConverters.keySet()) {
            i++;
            if (i == index) return name;
        }
        return null;
    }

    @Override
    protected JSONObject[] get(Event event) {
        PacketContainer packet = packetContainerExpression.getSingle(event);
        int index = this.index.getSingle(event).intValue();
        Mundo.debug(this, "Packet before calling function :" + packet);
        return isSingle ? new JSONObject[]{singleConverter.get(packet, index)} : pluralConverter.get(packet, index);
    }

    @Override
    public boolean isSingle() {
        return isSingle;
    }

    @Override
    public Class<JSONObject> getReturnType() {
        return JSONObject.class;
    }

    @Override
    public String toString(Event event, boolean b) {
        return "%string% pjson %number% of %packet%";
    }

    @Override
    public boolean init(Expression<?>[] expressions, int i, Kleenean kleenean, SkriptParser.ParseResult parseResult) {
        String string;
        boolean isSingle = i == 0;
        if (expressions[0] == null) {
            string = getConverterNameByIndex(parseResult.mark, isSingle);
        } else if (expressions[0] instanceof Literal<?>) {
            string = ((Literal<String>) expressions[0]).getSingle();
        } else if (expressions[0] instanceof VariableString) {
            String fullstring = ((VariableString) expressions[0]).toString();
            string = fullstring.substring(1, fullstring.length() - 1);
        } else {
            Skript.error("The string '" + expressions[0] + "' is not a literal string! Only literal strings can be used in the pjson expression!");
            return false;
        }
        index = (Expression<Number>) expressions[1];
        packetContainerExpression = (Expression<PacketContainer>) expressions[2];
        singleConverter = getSingleConverter(string.toLowerCase());
        pluralConverter = getPluralConverter(string.toLowerCase());
        if (singleConverter == null && pluralConverter == null) {
            Skript.error("The string " + string + " is not a valid packetinfo!");
            return false;
        }
        return true;
    }

    public void change(Event event, Object[] delta, Changer.ChangeMode mode){
        PacketContainer packet = packetContainerExpression.getSingle(event);
        int index = this.index.getSingle(event).intValue();
        Mundo.debug(this, "Packet before calling function :" + packet);
        if (isSingle) {
            singleConverter.set(packet, index, ((JSONObject) delta[0]));
        } else {
            JSONObject[] deltaJSON = new JSONObject[delta.length];
            for (int i = 0; i < delta.length; i++) {
                deltaJSON[i] = (JSONObject) delta[i];
            }
            pluralConverter.set(packet, index, deltaJSON);
        }
    }

    public Class<?>[] acceptChange(final Changer.ChangeMode mode) {
        if (mode == Changer.ChangeMode.SET) {
            return CollectionUtils.array(JSONObject.class);
        }
        return null;
    }
}