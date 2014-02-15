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
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.In;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.logic.console.Command;
import org.terasology.logic.console.CommandParam;
import org.terasology.logic.health.BeforeHealEvent;
import org.terasology.logic.health.DoDamageEvent;
import org.terasology.logic.health.HealthComponent;
import org.terasology.logic.players.event.OnPlayerSpawnedEvent;
import org.terasology.network.ClientComponent;

/**
 * @author UltimateBudgie <TheUltimateBudgie@gmail.com>
 */

@RegisterSystem(RegisterMode.AUTHORITY)
public class HungerAuthoritySystem extends BaseComponentSystem implements UpdateSubscriberSystem {
    private static final Logger logger = LoggerFactory.getLogger(HungerAuthoritySystem.class);

    @In
    private EntityManager entityManager;

    @In
    private org.terasology.engine.Time time;

    @Override
    public void initialise() {
    }

    @Override
    public void shutdown() {
    }

    @Override
    public void update(float delta) {
        for (EntityRef entity : entityManager.getEntitiesWith(HungerComponent.class)) {
            HungerComponent hunger = entity.getComponent(HungerComponent.class);

            //Decrease food capacity if appropriate
            if (time.getGameTimeInMs() >= hunger.nextFoodDecreaseTick) {
                hunger.currentFoodCapacity -= hunger.foodDecreaseAmount;
                if (hunger.currentFoodCapacity < 0) {
                    hunger.currentFoodCapacity = 0;
                }
                hunger.nextFoodDecreaseTick = time.getGameTimeInMs() + hunger.foodDecreaseInterval;
                entity.saveComponent(hunger);
            }

            //Check to see if health should be decreased
            if (entity.hasComponent(HealthComponent.class)) {
                if (hunger.loseHealth) {
                    if (hunger.currentFoodCapacity <= hunger.healthLossThreshold) {
                        if (time.getGameTimeInMs() >= hunger.nextHealthDecreaseTick) {
                            entity.send(new DoDamageEvent(hunger.healthDecreaseAmount));
                            hunger.nextHealthDecreaseTick = time.getGameTimeInMs() + hunger.healthDecreaseInterval;
                        }
                    }
                }
            }
        }
    }

    @ReceiveEvent(components = {HungerComponent.class})
    public void onHealthRegen(BeforeHealEvent event, EntityRef entity) {
        HungerComponent hunger = entity.getComponent(HungerComponent.class);
        if (!hunger.loseHealth) {
            return;
        }
        if (hunger.currentFoodCapacity <= hunger.healthLossThreshold) {
            event.consume();
        }
    }

    @ReceiveEvent(components = {HungerComponent.class})
    public void onPlayerRespawn(OnPlayerSpawnedEvent event, EntityRef player) {
        HungerComponent hunger = player.getComponent(HungerComponent.class);
        hunger.currentFoodCapacity = hunger.maxFoodCapacity;
        hunger.nextFoodDecreaseTick = time.getGameTimeInMs() + hunger.foodDecreaseInterval;
        player.saveComponent(hunger);
    }

    @ReceiveEvent
    public void onPlayerFirstSpawn(OnPlayerSpawnedEvent event, EntityRef player) {
        if (!player.hasComponent(HungerComponent.class)) {
            HungerComponent hunger = new HungerComponent();
            hunger.nextFoodDecreaseTick = time.getGameTimeInMs() + hunger.foodDecreaseInterval;
            player.addComponent(hunger);
        }
    }

    @Command(shortDescription = "Temp testing command", runOnServer = true)
    public void test(EntityRef client) {
        logger.info("ID:" + client.getComponent(ClientComponent.class).character.getId());
        for (Component c : client.getComponent(ClientComponent.class).character.iterateComponents()) {
            logger.info(c.toString());
        }
    }

    @Command(shortDescription = "Checks your hunger/food level.", runOnServer = true)
    public String hungerCheck(EntityRef client) {
        EntityRef character = client.getComponent(ClientComponent.class).character;
        if (character.hasComponent(HungerComponent.class)) {
            HungerComponent hunger = character.getComponent(HungerComponent.class);
            return "Current Food Level: " + hunger.currentFoodCapacity + "/" + hunger.maxFoodCapacity;
        } else {
            return "You don't have a hunger level.";
        }
    }

    @Command(shortDescription = "Sets your current hunger level.", runOnServer = true)
    public String hungerSet(@CommandParam(value = "FoodLevel") float newFood, EntityRef client) {
        EntityRef character = client.getComponent(ClientComponent.class).character;
        if (!character.hasComponent(HungerComponent.class)) {
            return "You don't have a hunger level.";
        }
        HungerComponent hunger = character.getComponent(HungerComponent.class);
        if (newFood < 0) {
            hunger.currentFoodCapacity = 0;
            character.saveComponent(hunger);
            return "Food level cannot be below 0. Setting to 0.";
        }
        if (newFood > hunger.maxFoodCapacity) {
            hunger.currentFoodCapacity = hunger.maxFoodCapacity;
            character.saveComponent(hunger);
            return "Food level cannot be above Max Food Capacity. Setting to Max(" + hunger.maxFoodCapacity + ")";
        }
        hunger.currentFoodCapacity = newFood;
        character.saveComponent(hunger);
        return "Food level successfully set to: " + newFood;
    }

    @Command(shortDescription = "Sets your max food level.", runOnServer = true)
    public String hungerSetMax(@CommandParam(value = "MaxFoodLevel") float newMax, EntityRef client) {
        EntityRef character = client.getComponent(ClientComponent.class).character;
        if (!character.hasComponent(HungerComponent.class)) {
            return "You don't have a hunger level.";
        }
        HungerComponent hunger = character.getComponent(HungerComponent.class);
        if (newMax <= 0) {
            hunger.maxFoodCapacity = 100;
            character.saveComponent(hunger);
            return "Max Food Level cannot be below or equal to 0. Setting to default (100)";
        }
        hunger.maxFoodCapacity = newMax;
        character.saveComponent(hunger);
        return "Max Food Level successfully set to: " + newMax;
    }
}
