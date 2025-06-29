package com.ticketReminder;

import lombok.Getter;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.api.gameval.ObjectID;
import net.runelite.api.gameval.ItemID;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.callback.ClientThread;
import javax.inject.Inject;

@PluginDescriptor(
		name = "Wilderness Course Ticket Reminder",
		description = "Reminds player to claim your ticket after each lap in the Wilderness Agility Course.",
		tags = {"agility", "wildy", "highlight", "dispenser", "pillar"}
)
public class TicketReminder extends Plugin {

	@Inject
	private Client client;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private TicketReminderOverlay overlay;

	@Inject
	private ClientThread clientThread;

	@Getter
    private boolean lapCompleted = false;
	
	@Getter
	private GameObject dispenserObject;
	
	private int lastKnownAgilityXp;
	private int lastTicketCount;

	// Constants
	private static final int EXPECTED_XP_FOR_LAP = 499; // Xp drop for completing a lap


	@Override
	protected void startUp() {
		overlayManager.add(overlay);
		lastKnownAgilityXp = client.getSkillExperience(Skill.AGILITY);
		clientThread.invokeLater(() -> lastTicketCount = getTicketQuantity());
	}

	@Override
	protected void shutDown() {
		overlayManager.remove(overlay);
	}

	// Sets lapCompleted to true if the player gains the expected amount of XP for completing a lap
	@Subscribe
	public void onStatChanged(StatChanged event) {
		if (event.getSkill() == Skill.AGILITY) {
			int currentAgilityXp = client.getSkillExperience(Skill.AGILITY);
			int xpGained = currentAgilityXp - lastKnownAgilityXp;
			lastKnownAgilityXp = currentAgilityXp;

			// If the XP gain matches the amount for completing a lap, mark lap as completed
			if (xpGained == EXPECTED_XP_FOR_LAP || xpGained == EXPECTED_XP_FOR_LAP - 1) {
				lapCompleted = true;
			}
		}
	}

	// Resets lapCompleted to false if the player receives a ticket
	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged event) {
		clientThread.invokeLater(() -> {
			if (event.getContainerId() == InventoryID.INVENTORY.getId()) {
				int currentTicketCount = getTicketQuantity();
				if (currentTicketCount > lastTicketCount) {
					lapCompleted = false;
				}
				lastTicketCount = currentTicketCount;
			}
		});
	}

	// Prevents the player from interacting with the pipe obstacle after completing a lap
	@Subscribe
	public void onMenuEntryAdded(MenuEntryAdded event) {
		if (lapCompleted) {
			// Check if the menu entry is for the pipe obstacle
			if (event.getOption().equals("Squeeze-through") && event.getIdentifier() == ObjectID.OBSTICAL_PIPE2) {
				event.getMenuEntry().setDeprioritized(true);
			}
		}
	}

	// Sets the dispenserObject to the GameObject dispenser
	@Subscribe
	public void onGameObjectSpawned(GameObjectSpawned event) {
		GameObject object = event.getGameObject();
		if (object.getId() == ObjectID.WILDY_AGILITY_PILLAR) {
			dispenserObject = object;
		}
	}

	// Helper method to get the quantity of the ticket item in the inventory
	private int getTicketQuantity() {
		// Retrieve the inventory container
		ItemContainer inventoryContainer = client.getItemContainer(InventoryID.INVENTORY);

		if (inventoryContainer == null) {
			return 0;
		}

		Item[] items = inventoryContainer.getItems();

		// Find the ticket item and return its quantity
		for (Item item : items) {
			if (item.getId() == ItemID.WILDY_AGILITY_TOKEN) {
				return item.getQuantity();
			}
		}
		return 0;
	}
}
