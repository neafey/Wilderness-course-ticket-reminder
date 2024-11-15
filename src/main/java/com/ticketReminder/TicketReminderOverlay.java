package com.ticketReminder;

import net.runelite.api.GameObject;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import javax.inject.Inject;
import java.awt.*;


public class TicketReminderOverlay extends Overlay {
    private final TicketReminder plugin;

    @Inject
    public TicketReminderOverlay(TicketReminder plugin) {
        this.plugin = plugin;
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_SCENE);
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        GameObject dispenser = plugin.getDispenserObject();
        Shape dispenserClickbox = dispenser.getClickbox();
        if (plugin.isLapCompleted()) {
            if (dispenserClickbox != null) {
                graphics.setColor(new Color(0, 255, 0, 255));
                graphics.draw(dispenserClickbox);
                graphics.setColor(new Color(0, 255, 0, 50));
                graphics.fill(dispenserClickbox);
            }
        }
        return null;
    }
}

