/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.hunger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.Time;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.lifecycleEvents.BeforeDeactivateComponent;
import org.terasology.entitySystem.event.EventPriority;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.hunger.component.FoodComponent;
import org.terasology.hunger.component.HungerComponent;
import org.terasology.hunger.event.FoodConsumedEvent;
import org.terasology.logic.common.ActivateEvent;
import org.terasology.logic.console.commandSystem.annotations.Command;
import org.terasology.logic.console.commandSystem.annotations.CommandParam;
import org.terasology.logic.console.commandSystem.annotations.Sender;
import org.terasology.logic.health.BeforeHealEvent;
import org.terasology.logic.health.DoDamageEvent;
import org.terasology.logic.inventory.InventoryManager;
import org.terasology.logic.inventory.InventoryUtils;
import org.terasology.logic.inventory.ItemComponent;
import org.terasology.logic.players.event.OnPlayerSpawnedEvent;
import org.terasology.network.ClientComponent;
import org.terasology.registry.In;

/**
 * The authority system monitoring player hunger levels, related events and commands.
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class HungerAuthoritySystem extends BaseComponentSystem implements UpdateSubscriberSystem {
    /** The logger for debugging to the log files. */
    private static final Logger logger = LoggerFactory.getLogger(HungerAuthoritySystem.class);

    /** Reference to the EntityManager, used for getting all entities who are affected by hunger. */
    @In
    private EntityManager entityManager;

    /** Reference to the InventoryManager, used for removing consumed items from inventory. */
    @In
    private InventoryManager inventoryManager;

    /** Reference to the current time, used for calculating if the food of an entity has to be decreased.*/
    @In
    private Time time;

    /**
     * Chekcs the HungerComponent for all entities and triggers DamageEvents if their hunger level is below the threshold.
     * @param delta - Unused parameter.
     */
    @Override
    public void update(float delta) {
        long gameTime = time.getGameTimeInMs();
        for (EntityRef entity : entityManager.getEntitiesWith(HungerComponent.class)) {
            HungerComponent hunger = entity.getComponent(HungerComponent.class);

            //Check to see if health should be decreased
            if (HungerUtils.getHungerForEntity(entity) < hunger.healthLossThreshold) {
                if (gameTime >= hunger.nextHealthDecreaseTick) {
                    entity.send(new DoDamageEvent(hunger.healthDecreaseAmount));
                    hunger.nextHealthDecreaseTick = gameTime + hunger.healthDecreaseInterval;
                }
            }
        }
    }

    /**
     * Cancels the BeforeHealEvent for an entity if their hunger level is lower than the health regen threshold.
     * @param event The BeforeHealEvent, called before an entity is about to be healed.
     * @param entity The entity which is being healed.
     * @param hunger The HungerComponent object, containing settings for Hunger.
     */
    @ReceiveEvent
    public void onHealthRegen(BeforeHealEvent event, EntityRef entity,
                              HungerComponent hunger) {
        if (event.getInstigator() == entity
            && HungerUtils.getHungerForEntity(entity) < hunger.healthStopRegenThreshold) {
            event.consume();
        }
    }

    /**
     * Set's the players hunger to a maximum when spawning. Set's the last calculation time to the current game time.
     * @param event The OnPlayerSpawnedEvent, called when a player is spawning into the world.
     * @param player The player which is being spawned.
     * @param hunger The HungerComponent object, containing settings for Hunger.
     */
    @ReceiveEvent
    public void onPlayerSpawn(OnPlayerSpawnedEvent event, EntityRef player,
                              HungerComponent hunger) {
        hunger.lastCalculatedFood = hunger.maxFoodCapacity;
        hunger.lastCalculationTime = time.getGameTimeInMs();
        player.saveComponent(hunger);
    }

    /**
     * Saves data for an entity's HungerComponent before it is being deactivated.
     * @param event - The BeforeDeactivateComponent Event, called when a hungercomponent is about to leave the active state.
     * @param entity - The Entity whose hungercomponent is about to leave the active state.
     * @param hunger - The HungerComponent which isa bout to leave it's active state.
     */
    @ReceiveEvent
    public void beforeRemoval(BeforeDeactivateComponent event, EntityRef entity, HungerComponent hunger) {
        hunger.lastCalculatedFood = HungerUtils.getHungerForEntity(entity);
        hunger.lastCalculationTime = time.getGameTimeInMs();
        entity.saveComponent(hunger);
    }

    /**
     * This method registers it when an entity consumes food and adds the food to the entities HungerComponent.
     * @param event The ActivateEvent called when an entity consumes food.
     * @param item The entity which is consuming the food.
     * @param food The Foodcomponent containing data about how much a certain type of food is filling.
     */
    @ReceiveEvent
    public void foodConsumed(ActivateEvent event, EntityRef item, FoodComponent food) {
        float filling = food.filling;
        EntityRef instigator = event.getInstigator();
        HungerComponent hunger = instigator.getComponent(HungerComponent.class);
        if (hunger != null) {
            hunger.lastCalculatedFood = Math.min(hunger.maxFoodCapacity, HungerUtils.getHungerForEntity(instigator) + filling);
            hunger.lastCalculationTime = time.getGameTimeInMs();
            instigator.saveComponent(hunger);
            item.send(new FoodConsumedEvent(event));

            if (!item.exists()) {
                event.consume();
            }
        }
    }

    /**
     * This method deals with removal of food item after it is consumed.
     * @param event The FoodConsumedEvent called when an entity consumes food.
     * @param item The entity which is consuming the food.
     */
    @ReceiveEvent(components = ItemComponent.class, priority = EventPriority.PRIORITY_TRIVIAL)
    public void usedItem(FoodConsumedEvent event, EntityRef item) {
        ItemComponent itemComp = item.getComponent(ItemComponent.class);
        if (itemComp.consumedOnUse) {
            int slot = InventoryUtils.getSlotWithItem(event.getInstigator(), item);
            boolean destroyDrink = false;

            if (itemComp.baseDamage != Integer.MIN_VALUE) {
                itemComp.baseDamage = Integer.MIN_VALUE;
            } else {
                destroyDrink = true;
                inventoryManager.removeItem(event.getInstigator(), event.getInstigator(), slot, destroyDrink, 1);
            }
        }
    }
}
