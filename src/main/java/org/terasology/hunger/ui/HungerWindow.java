/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.hunger.ui;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.hunger.HungerUtils;
import org.terasology.hunger.component.HungerComponent;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.databinding.ReadOnlyBinding;
import org.terasology.rendering.nui.layers.hud.CoreHudWidget;
import org.terasology.rendering.nui.widgets.UILoadBar;


public class HungerWindow extends CoreHudWidget {
    /** This method initialises the hunger bar UI for a player and updates it based on current hunger. */
    @Override
    public void initialise() {
        UILoadBar hunger = find("hunger", UILoadBar.class);
        hunger.bindVisible(new ReadOnlyBinding<Boolean>() {
            @Override
            public Boolean get() {
                EntityRef character = CoreRegistry.get(LocalPlayer.class).getCharacterEntity();
                return character != null && character.hasComponent(HungerComponent.class);
            }
        });
        hunger.bindValue(
                new Binding<Float>() {
                    @Override
                    public Float get() {
                        EntityRef character = CoreRegistry.get(LocalPlayer.class).getCharacterEntity();
                        if (character == null || !character.hasComponent(HungerComponent.class)) {
                            return 0.0f;
                        }

                        HungerComponent hunger = character.getComponent(HungerComponent.class);
                        return HungerUtils.getHungerForEntity(character) / hunger.maxFoodCapacity;
                    }

                    @Override
                    public void set(Float value) {
                    }
                });
    }
}
