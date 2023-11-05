package org.limc.liboardmc;

import org.bukkit.Location;

public class LocationContainer {

    private Location value;
    public LocationContainer() {
        value = null;
    }

    public Location getValue() { return value; }
    public void setValue(Location newValue) { value = newValue; }

}
