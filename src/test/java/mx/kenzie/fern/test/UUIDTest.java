package mx.kenzie.fern.test;

import mx.kenzie.fern.Fern;
import org.junit.Test;

import java.util.UUID;

public class UUIDTest {
    
    private final UUID a = UUID.randomUUID(), b = UUID.randomUUID();
    
    @Test
    public void write() {
        class Thing {
            final int number = 1;
            final UUID uuid = a;
            final UUID[] uuids = {a, b};
        }
        final String result = Fern.out(new Thing(), null);
        assert result.equals("uuids [ \"" + a + "\" \"" + b + "\" ] uuid \"" + a + "\" number 1") : result;
    }
    
    @Test
    public void read() {
        class Thing {
            int number;
            UUID uuid;
            UUID[] uuids;
        }
        final Thing thing = new Thing();
        try (final Fern fern = Fern.in("uuids [ \"" + a + "\" \"" + b + "\" ] uuid \"" + a + "\" number 1")) {
            fern.toObject(thing);
        }
        assert thing.number == 1;
        assert thing.uuid.equals(a);
        assert thing.uuids != null;
        assert thing.uuids.length == 2;
        assert thing.uuids[0].equals(a);
        assert thing.uuids[1].equals(b);
    }
}
