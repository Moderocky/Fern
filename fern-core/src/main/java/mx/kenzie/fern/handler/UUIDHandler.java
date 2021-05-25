package mx.kenzie.fern.handler;

import java.util.UUID;

public class UUIDHandler implements ValueHandler<UUID> {
    @Override
    public boolean matches(String string) {
        return string.matches("[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}");
    }
    
    @Override
    public UUID parse(String string) {
        return UUID.fromString(string);
    }
}
