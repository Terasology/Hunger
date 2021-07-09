// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.hunger.component;

import org.terasology.engine.utilities.modifiable.ModifiableValue;
import org.terasology.gestalt.entitysystem.component.Component;

/**
 * This component uses the ModifiableValue type Any modifications to the data members of this component must be via
 * ModifiableValue modifiers ModifiableValue.getValue can be used to get the value of the data members
 */
public class FoodComponent implements Component<FoodComponent> {
    /**
     * The amount of hunger this food restores when used.
     */
    public ModifiableValue filling;

    @Override
    public void copy(FoodComponent other) {
        ModifiableValue newFilling = new ModifiableValue(other.filling.getBaseValue());
        newFilling.setMultiplier(other.filling.getMultiplier());
        newFilling.setPreModifier(other.filling.getPreModifier());
        newFilling.setPostModifier(other.filling.getPostModifier());
    }
}
