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
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.In;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.logic.console.Command;
import org.terasology.logic.console.Console;
import org.terasology.logic.health.HealthComponent;
import org.terasology.logic.players.event.OnPlayerSpawnedEvent;

/**
 * @author UltimateBudgie
 */

@RegisterSystem(RegisterMode.AUTHORITY)
public class HungerSystem implements UpdateSubscriberSystem {
    private static final Logger logger = LoggerFactory.getLogger(HungerSystem.class);

    @In
    private EntityManager entityManager;

    @In
    private org.terasology.engine.Time time;

    /*@In
    private Console console;*/

    @Override
    public void initialise() {
        logger.debug("Hunger intialising.");
        //console.registerCommandProvider(new HungerCommands());
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
            }

            //Check to see if health should be decreased
            if (entity.hasComponent(HealthComponent.class)) {
                if (hunger.loseHealth) {
                    if (hunger.currentFoodCapacity <= hunger.healthLossThreshold) {
                        if (time.getGameTimeInMs() >= hunger.nextHealthDecreaseTick) {
                            HealthComponent health = entity.getComponent(HealthComponent.class);
                            health.currentHealth -= hunger.healthDecreaseAmount;
                            hunger.nextHealthDecreaseTick = time.getGameTimeInMs() + hunger.healthDecreaseInterval;
                        }
                    }
                }
            }
        }
    }

    @ReceiveEvent
    public void onPlayerSpawn(OnPlayerSpawnedEvent event, EntityRef player) {
        logger.debug("Hunger Component added");
        player.addComponent(new HungerComponent());
    }

    @Command(shortDescription = "Check's your hunger/food level.", runOnServer = true)
    public void test(EntityRef client) {
        logger.info("TEST");
    }

    @Command(shortDescription = "Check's your hunger/food level.", runOnServer = true)
    public String checkHunger(EntityRef client) {
        logger.debug("checkHunger");
        if (client.hasComponent(HungerComponent.class)) {
            HungerComponent hunger = client.getComponent(HungerComponent.class);
            return "Current Food Level: " + hunger.currentFoodCapacity + "\n" + "Max Food Capacity: "
                    + hunger.maxFoodCapacity;
        } else {
            return "You don't have a hunger level!!!";
        }
    }
}
