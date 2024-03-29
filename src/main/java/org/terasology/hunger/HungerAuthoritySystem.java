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
import org.terasology.engine.core.Time;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.EventPriority;
import org.terasology.engine.entitySystem.event.Priority;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.entitySystem.prefab.PrefabManager;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.characters.AliveCharacterComponent;
import org.terasology.engine.logic.common.ActivateEvent;
import org.terasology.engine.logic.delay.DelayManager;
import org.terasology.engine.logic.delay.PeriodicActionTriggeredEvent;
import org.terasology.engine.logic.inventory.ItemComponent;
import org.terasology.engine.logic.players.event.OnPlayerRespawnedEvent;
import org.terasology.engine.logic.players.event.OnPlayerSpawnedEvent;
import org.terasology.engine.registry.In;
import org.terasology.engine.world.WorldComponent;
import org.terasology.gestalt.entitysystem.event.ReceiveEvent;
import org.terasology.hunger.component.FoodComponent;
import org.terasology.hunger.component.HungerComponent;
import org.terasology.hunger.event.AffectHungerEvent;
import org.terasology.hunger.event.FoodConsumedEvent;
import org.terasology.module.health.events.BeforeRegenEvent;
import org.terasology.module.health.events.DoDamageEvent;
import org.terasology.module.inventory.systems.InventoryManager;
import org.terasology.module.inventory.systems.InventoryUtils;

import static org.terasology.module.health.core.BaseRegenAuthoritySystem.BASE_REGEN;

/**
 * The authority system monitoring player hunger levels, related events and commands.
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class HungerAuthoritySystem extends BaseComponentSystem {
    /**
     * The logger for debugging to the log files.
     */
    private static final Logger logger = LoggerFactory.getLogger(HungerAuthoritySystem.class);

    /**
     * Reference to the EntityManager, used for getting all entities who are affected by hunger.
     */
    @In
    private EntityManager entityManager;

    /**
     * Reference to the InventoryManager, used for removing consumed items from inventory.
     */
    @In
    private InventoryManager inventoryManager;

    /**
     * Reference to the PrefabManager, used for getting damageType prefab.
     */
    @In
    private PrefabManager prefabManager;

    /**
     * Reference to the current time, used for calculating if the food of an entity has to be decreased.
     */
    @In
    private Time time;

    /**
     * Reference to DelayManager, used for addPeriodicAction.
     */
    @In
    private DelayManager delayManager;
    /**
     * The interval (in milliseconds) at which healthDecreaseAmount (above) is applied to the component.
     */
    public int healthDecreaseInterval = 3000;

    public static final String HUNGER_DAMAGE_ACTION_ID = "Hunger Damage";
    private boolean destroyDrink = false;

    public void postBegin() {
        boolean processedOnce = false;
        for (EntityRef entity : entityManager.getEntitiesWith(WorldComponent.class)) {
            if (!processedOnce) {
                delayManager.addPeriodicAction(entity, HUNGER_DAMAGE_ACTION_ID, 0, healthDecreaseInterval);
                processedOnce = true;
            } else {
                logger.warn("More than one entity with WorldComponent found");
            }
        }
    }

    /**
     * Deals a unit of hunger damage to the character.
     */
    @ReceiveEvent
    public void onPeriodicActionTriggered(PeriodicActionTriggeredEvent event, EntityRef entityUnused) {
        if (event.getActionId().equals(HUNGER_DAMAGE_ACTION_ID)) {
            for (EntityRef entity : entityManager.getEntitiesWith(HungerComponent.class,
                    AliveCharacterComponent.class)) {
                HungerComponent hunger = entity.getComponent(HungerComponent.class);
                final float expectedDecay = (healthDecreaseInterval * hunger.foodDecayPerSecond) / 1000;
                // Send event to allow for other systems to modify hunger decay.
                AffectHungerEvent affectHungerEvent = new AffectHungerEvent(expectedDecay);
                entity.send(affectHungerEvent);
                hunger.lastCalculatedFood = Math.max(0, hunger.lastCalculatedFood - affectHungerEvent.getResultValue());
                hunger.lastCalculationTime = time.getGameTimeInMs();
                entity.saveComponent(hunger);

                // Check to see if health should be decreased
                if (HungerUtils.getHungerForEntity(entity) < hunger.healthLossThreshold) {
                    Prefab starvationDamagePrefab = prefabManager.getPrefab("hunger:starvationDamage");
                    entity.send(new DoDamageEvent(hunger.healthDecreaseAmount, starvationDamagePrefab));
                    entity.saveComponent(hunger);
                }
            }
        }
    }

    /**
     * Cancels the base regeneration for an entity if their hunger level is lower than the health regen threshold. This
     * only affects the base regeneration action. All other registered regeneration actions are ignored.
     *
     * @param event The collector event for regeneration actions, called before an entity's health is about to
     *         be regenerated.
     * @param entity The entity whose health is about to be regenerated.
     * @param hunger The entity's hunger configuration.
     */
    @ReceiveEvent
    public void beforeBaseRegen(BeforeRegenEvent event, EntityRef entity, HungerComponent hunger) {
        if (event.getId().equals(BASE_REGEN)) {
            if (HungerUtils.getHungerForEntity(entity) < hunger.healthStopRegenThreshold) {
                event.consume();
            }
        }
    }

    /**
     * Set's the players hunger to a maximum when spawning. Set's the last calculation time to the current game time.
     *
     * @param event The OnPlayerSpawnedEvent, called when a player is spawning into the world.
     * @param player The player which is being spawned.
     * @param hunger The HungerComponent object, containing settings for Hunger.
     */
    @ReceiveEvent
    public void onPlayerSpawn(OnPlayerSpawnedEvent event, EntityRef player,
                              HungerComponent hunger) {
        resetHunger(player, hunger);
    }

    /**
     * Set's the players hunger to a maximum when respawning. Set's the last calculation time to the current game time.
     *
     * @param event The OnPlayerRespawnedEvent, called when a player is respawning into the world.
     * @param player The player which is being respawned.
     * @param hunger The HungerComponent object, containing settings for Hunger.
     */
    @ReceiveEvent
    public void onPlayerRespawn(OnPlayerRespawnedEvent event, EntityRef player,
                                HungerComponent hunger) {
        resetHunger(player, hunger);
    }

    private void resetHunger(EntityRef player, HungerComponent hunger) {
        hunger.lastCalculatedFood = hunger.maxFoodCapacity;
        hunger.lastCalculationTime = time.getGameTimeInMs();
        player.saveComponent(hunger);
    }

    /**
     * This method registers it when an entity consumes food and adds the food to the entities HungerComponent.
     *
     * @param event The ActivateEvent called when an entity consumes food.
     * @param item The entity which is consuming the food.
     * @param food The Foodcomponent containing data about how much a certain type of food is filling.
     */
    @ReceiveEvent
    public void foodConsumed(ActivateEvent event, EntityRef item, FoodComponent food) {
        float filling = food.filling.getValue();
        EntityRef instigator = event.getInstigator();
        HungerComponent hunger = instigator.getComponent(HungerComponent.class);
        if (hunger != null) {
            hunger.lastCalculatedFood = Math.min(hunger.maxFoodCapacity,
                    HungerUtils.getHungerForEntity(instigator) + filling);
            hunger.lastCalculationTime = time.getGameTimeInMs();
            instigator.saveComponent(hunger);
            item.send(new FoodConsumedEvent(event));

            if (destroyDrink) {
                event.consume();
                destroyDrink = false;
            }
        }
    }

    /**
     * This method deals with removal of food item after it is consumed.
     *
     * @param event The FoodConsumedEvent called when an entity consumes food.
     * @param item The entity which is consuming the food.
     */
    @Priority(EventPriority.PRIORITY_TRIVIAL)
    @ReceiveEvent(components = ItemComponent.class)
    public void usedItem(FoodConsumedEvent event, EntityRef item) {
        ItemComponent itemComp = item.getComponent(ItemComponent.class);
        if (itemComp.consumedOnUse) {
            int slot = InventoryUtils.getSlotWithItem(event.getInstigator(), item);
            destroyDrink = false;

            if (itemComp.baseDamage != Integer.MIN_VALUE) {
                itemComp.baseDamage = Integer.MIN_VALUE;
            } else {
                destroyDrink = true;
                itemComp.baseDamage = 1;
                inventoryManager.removeItem(event.getInstigator(), event.getInstigator(), item, true, 1);
            }
        }
    }
}
