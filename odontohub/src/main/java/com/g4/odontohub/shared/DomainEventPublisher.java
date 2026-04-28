package com.g4.odontohub.shared;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class DomainEventPublisher {

    private static final Map<Class<?>, List<Consumer<Object>>> handlers = new HashMap<>();

    @SuppressWarnings("unchecked")
    public static <T> void subscribe(Class<T> eventType, Consumer<T> handler) {
        handlers.computeIfAbsent(eventType, k -> new ArrayList<>())
                .add(e -> handler.accept((T) e));
    }

    public static void publish(Object event) {
        List<Consumer<Object>> eventHandlers = handlers.get(event.getClass());
        if (eventHandlers != null) {
            eventHandlers.forEach(h -> h.accept(event));
        }
    }

    public static void reset() {
        handlers.clear();
    }
}
