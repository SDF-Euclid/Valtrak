package com.example.valtrak.Gameplay.Engine;

import com.example.valtrak.Data.CardLibrary.CardLevel;

/**
 * An item card. What {@code power} and {@code count} mean depends on the effect:
 * <ul>
 *   <li>ERA: {@code power} = percent less CHEMICAL damage</li>
 *   <li>ARTILLERY: {@code power} = true damage to each target, {@code count} = how many targets</li>
 *   <li>SEARCH: {@code count} = how many cards, {@code searchKind} = what kind</li>
 *   <li>DRAW: {@code count} = how many cards</li>
 * </ul>
 */
public record ItemSpec(long cardId, String name, CardLevel level, ItemEffect effect,
                       int power, int count, SearchKind searchKind) implements CardSpec {}
