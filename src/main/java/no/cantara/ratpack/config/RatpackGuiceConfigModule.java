package no.cantara.ratpack.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import ratpack.server.ServerConfig;

public class RatpackGuiceConfigModule extends AbstractModule {

    private final ServerConfig serverConfig;

    public RatpackGuiceConfigModule(ServerConfig serverConfig) {
        this.serverConfig = serverConfig;
    }

    @Override
    protected void configure() {
        ObjectNode rootNode = serverConfig.getRootNode();
        processJsonConfiguration("", rootNode);
    }

    private void processJsonConfiguration(String nodeName, JsonNode node) {
        if (node.isValueNode()) {
            if (node.isBoolean()) {
                bind(boolean.class).annotatedWith(Names.named(nodeName)).toInstance(node.booleanValue());
            }
            if (node.isNumber()) {
                bind(long.class).annotatedWith(Names.named(nodeName)).toInstance(node.longValue());
                bind(int.class).annotatedWith(Names.named(nodeName)).toInstance(node.intValue());
            }
            if (node.isTextual()) {
                String text = node.textValue();
                bind(String.class).annotatedWith(Names.named(nodeName)).toInstance(text);
                try {
                    long longValue = Long.parseLong(text);
                    bind(long.class).annotatedWith(Names.named(nodeName)).toInstance(longValue);
                    int intValue = Integer.parseInt(text);
                    bind(int.class).annotatedWith(Names.named(nodeName)).toInstance(intValue);
                } catch (NumberFormatException e) {
                }
                bind(boolean.class).annotatedWith(Names.named(nodeName)).toInstance(node.booleanValue());
            }
            return;
        }
        if (node.isObject()) {
            String childName;
            if (nodeName.isEmpty()) {
                childName = "";
            } else if (nodeName.endsWith(".")) {
                childName = nodeName;
            } else {
                childName = nodeName + ".";
            }
            node.fields().forEachRemaining(entry -> processJsonConfiguration(childName + entry.getKey(), entry.getValue()));
        }
    }
}
